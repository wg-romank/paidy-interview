package forex.domain

import java.time.OffsetDateTime

import scala.concurrent.duration.FiniteDuration

case class Timestamp(value: OffsetDateTime) {
  def +(other: FiniteDuration): Timestamp =
    Timestamp(value.plus(other.length, other.unit.toChronoUnit))
}

object Timestamp {
  implicit val ordering: Ordering[Timestamp] =
    (x: Timestamp, y: Timestamp) => x.value.compareTo(y.value)
}
