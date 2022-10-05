package forex.services.cache.interpreters

import cats.implicits._
import cats.effect.{Concurrent, Fiber, Timer}
import cats.effect.concurrent.Ref
import forex.programs.cache.Cache
import forex.services.cache.Cached

import scala.concurrent.duration.FiniteDuration

class Ce2RefCached[F[_], A](value: Ref[F, A]) extends Cached[F, A] {
  override def get: F[A] = value.get
}

object Ce2RefCached {
  def create[F[_] : Concurrent : Timer, A]
  (initial: A, updateInterval: FiniteDuration, cache: Cache[F, A]): F[(Ce2RefCached[F, A], Fiber[F, Unit])] =
    for {
      ref <- Ref.of[F, A](initial)
      update <- Concurrent[F].start {
          (for {
            value <- ref.get
            newValue <- cache.fetch(value)
            _ <- ref.set(newValue)
            _ <- Timer[F].sleep(updateInterval)
          } yield ()).foreverM
        }: F[Fiber[F, Unit]]
    } yield (new Ce2RefCached(ref), update)
}
