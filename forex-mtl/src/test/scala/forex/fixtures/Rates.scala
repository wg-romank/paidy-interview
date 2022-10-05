package forex.fixtures

import java.time.OffsetDateTime

import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}

object Rates {
  val now: Timestamp = Timestamp(OffsetDateTime.now())
  val pair: Pair = Pair(Currency.USD, Currency.JPY)
  val rate: Rate = Rate(pair, Price(100.0), now)
}
