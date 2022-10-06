package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: List[Rate.Pair]): F[Error Either List[Rate]]
}
