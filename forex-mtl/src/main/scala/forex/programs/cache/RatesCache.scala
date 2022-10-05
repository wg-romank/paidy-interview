package forex.programs.cache

import cats.Functor
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.RatesService
import cats.implicits._
import scala.math.Ordering.Implicits._

class RatesCache[F[_]: Functor](ratesService: RatesService[F]) extends Cache[F, Map[Pair, Rate]] {
  override def fetch(previousValue: Map[Pair, Rate]): F[Map[Pair, Rate]] =
    ratesService.get(Rate.pairs).map {
      // todo: should log errors here
      case Left(_) => previousValue
      case Right(rates) =>
        rates.foldLeft(previousValue) {
          case (acc, r) => acc.get(r.pair) match {
            case Some(oldRate) if oldRate.timestamp >= r.timestamp => acc
            case _ => acc.updated(r.pair, r)
          }
        }
    }
}
