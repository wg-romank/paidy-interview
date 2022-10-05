package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.implicits._
import forex.domain.{Price, Rate}
import forex.services.rates.errors._
import forex.services.time.ServiceTime

class OneFrameDummy[F[_]: Applicative: ServiceTime] extends Algebra[F] {

  override def get(pairs: List[Rate.Pair]): F[Error Either List[Rate]] =
    for {
      now <- ServiceTime[F].now
    } yield pairs.map {
      pair => Rate(pair, Price(BigDecimal(100)), now)
    }.asRight[Error]

}
