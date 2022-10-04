package forex.services.rates

import io.circe.{Error => CirceError}

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
    final case class OneFrameMalformedResponse(reason: CirceError) extends Error
  }

}
