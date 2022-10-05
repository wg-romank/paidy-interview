package forex

import cats.effect.{Concurrent, Fiber, Timer}
import cats.implicits._
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.programs.cache.RatesCache
import forex.programs.rates.Program.CachedRates
import forex.services.cache.interpreters.Ce2RefCached
import forex.services.time.ServiceTime
import forex.services.time.interpreters.RealtimeUTC
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import sttp.client3.SttpBackend

class Module[F[_]: Concurrent: Timer](
  config: ApplicationConfig,
  cachedRates: CachedRates[F],
) {
  implicit val serviceTime: ServiceTime[F] = new RealtimeUTC[F]

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](config.rateTtl, cachedRates)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}

object Module {
  def create[F[_]: Concurrent: Timer](
    config: ApplicationConfig,
    backend: SttpBackend[F, _],
  ): F[(Module[F], Fiber[F, Unit])] = {
    val ratesService: RatesService[F] = RatesServices.sttp[F](backend, config.oneframe)
    val ratesCache: RatesCache[F] = new RatesCache[F](ratesService)

    Ce2RefCached.create(Map.empty[Pair, Rate], config.cacheUpdateInterval, ratesCache).map {
      case (ce2cache, cacheUpdate) =>
        (new Module[F](config, ce2cache), cacheUpdate)
    }
  }
}
