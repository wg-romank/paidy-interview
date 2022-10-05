package forex.http
package rates

import cats.effect.Sync
import cats.implicits._
import forex.domain.Rate.Pair
import forex.programs.RatesProgram
import forex.services.time.ServiceTime
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync: ServiceTime](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      for {
        now <- ServiceTime[F].now
        r <- rates.get(now, Pair(from, to))
        rate <- Sync[F].fromEither(r)
        response <- Ok(rate.asGetApiResponse)
      } yield response
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
