package forex.services.time.interpreters

import java.time.{OffsetDateTime, ZoneId}

import cats.effect.Sync
import forex.domain.Timestamp
import forex.services.time.ServiceTime

class RealtimeUTC[F[_]: Sync] extends ServiceTime[F]{
  override def now: F[Timestamp] = {
    val time = OffsetDateTime.now(ZoneId.of("UTC"))
    Sync[F].delay(Timestamp(time))
  }

}
