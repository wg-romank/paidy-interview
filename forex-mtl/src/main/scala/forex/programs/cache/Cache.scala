package forex.programs.cache

trait Cache[F[_], A] {
  def fetch(previousValue: A): F[A]
}

