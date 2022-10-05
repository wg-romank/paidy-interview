package forex.programs.rates

import forex.domain.{Rate, Timestamp}
import errors._
import forex.domain.Rate.Pair

trait Algebra[F[_]] {
  def get(now: Timestamp, pair: Pair): F[Error Either Rate]
}
