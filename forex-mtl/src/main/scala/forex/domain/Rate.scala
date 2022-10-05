package forex.domain

import cats.Show

import scala.math.Ordering.Implicits._
import scala.concurrent.duration.FiniteDuration

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
) {
  def isNotExpired(now: Timestamp, ttl: FiniteDuration): Boolean =
    timestamp + ttl >= now
}

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  )

  val pairs = Currency.all.flatMap(c => Currency.all.filter(_ != c).map(z => Pair(c, z)))

  object Pair {
      implicit val show: Show[Pair] = Show.show(p => s"${p.from}${p.to}")
  }
}
