package forex.services.cache

trait Cached[F[_], A] {
  def get: F[A]
}
