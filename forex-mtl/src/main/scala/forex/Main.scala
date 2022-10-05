package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.client3.SttpBackend
import sttp.client3.armeria.cats.ArmeriaCatsBackend

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    ArmeriaCatsBackend.resource[IO]().use {
      httpClient =>
        new Application[IO].stream(executionContext, httpClient).compile.drain.as(ExitCode.Success)
    }

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext, backend: SttpBackend[F, _]): Stream[F, Unit] = {
     for {
       config <- Config.stream("app")
       (module, _) <- Stream.bracket(Module.create[F](config, backend)) {
         case (_, cacheUpdate) => cacheUpdate.cancel
       }
       _ <- BlazeServerBuilder[F](ec)
         .bindHttp(config.http.port, config.http.host)
         .withHttpApp(module.httpApp)
         .serve
     } yield ()
  }

}
