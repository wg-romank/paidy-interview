package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.services.time.ServiceTime
import interpreters._
import sttp.client3.SttpBackend

object Interpreters {
  def dummy[F[_]: Applicative: ServiceTime]: Algebra[F] = new OneFrameDummy[F]()

  def sttp[F[_]: Sync](backend: SttpBackend[F, _], oneFrameConfig: OneFrameConfig): Algebra[F] =
    new OneFrameSttp[F](backend, oneFrameConfig)
}
