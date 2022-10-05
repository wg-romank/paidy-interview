package forex.programs.rates

import cats.Functor
import cats.implicits._
import errors._
import forex.domain.Rate.Pair
import forex.domain._
import forex.programs.rates.Program.CachedRates
import forex.services.cache.Cached

import scala.concurrent.duration.FiniteDuration

class Program[F[_]: Functor](
    rateTtl: FiniteDuration,
    ratesCache: CachedRates[F]
) extends Algebra[F] {

  override def get(now: Timestamp, pair: Pair): F[Error Either Rate] =
    ratesCache.get.map {
      rates =>
        rates.get(pair) match {
          case Some(r) if r.isNotExpired(now, rateTtl) => Right(r)
          case Some(r) => Left(Error.RateExpired(r.timestamp, now))
          // todo: as a fallback trigger fetch
          case None => Left(Error.RateNotInCache(pair, now))
        }
    }

}

object Program {
  type CachedRates[F[_]] = Cached[F, Map[Pair, Rate]]

  def apply[F[_]: Functor](
      rateTtl: FiniteDuration,
      cachedRate: CachedRates[F]
  ): Algebra[F] = new Program[F](rateTtl, cachedRate)

}
