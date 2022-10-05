package forex.services.time

import forex.domain.Timestamp

trait ServiceTime[F[_]] {
  def now: F[Timestamp]
}

object ServiceTime {
  def apply[F[_]: ServiceTime]: ServiceTime[F] = implicitly[ServiceTime[F]]
}
