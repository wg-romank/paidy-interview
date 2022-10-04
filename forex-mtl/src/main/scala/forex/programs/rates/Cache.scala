package forex.programs.rates

import java.time.Duration

trait Cache[F[_]] {
  def create[A](updateInterval: Duration, fetch: => F[A]): Cached[F, A]
}

trait Cached[F[_], A] {
  def get: F[A]
}
