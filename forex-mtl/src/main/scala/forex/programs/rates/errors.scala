package forex.programs.rates

import forex.domain.Rate.Pair
import forex.domain.Timestamp

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateNotInCache(pair: Pair, now: Timestamp) extends Error
    final case class RateExpired(lastUpdated: Timestamp, now: Timestamp) extends Error
  }

}
