package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.implicits._
import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.{Algebra, errors}
import io.circe.parser.decode
import sttp.client3._
import sttp.client3.basicRequest

case class ResponseRate(
  from: Currency,
  to: Currency,
  bid: BigDecimal,
  ask: BigDecimal,
  price: BigDecimal,
  timeStamp: OffsetDateTime) {
  def toRate: Rate = Rate(Pair(from, to), Price(price), Timestamp(timeStamp))
}

object ResponseRate {
  type Response = List[ResponseRate]

  import io.circe._
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.emap(Currency.fromString)
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[ResponseRate] = deriveConfiguredDecoder[ResponseRate]
}

class OneFrameSttp[F[_]: Sync](backend: SttpBackend[F, _], oneFrameConfig: OneFrameConfig) extends Algebra[F] {
  private def mkRequest(pairs: List[Pair]) = {
    val uri = pairs.foldLeft(uri"http://${oneFrameConfig.address}/rates") {
      case (acc, p) => acc.addParam("pair", p.show)
    }

    basicRequest
      .header("token", oneFrameConfig.token)
      .get(uri)
  }

  override def get(pairs: List[Rate.Pair]): F[errors.Error Either List[Rate]] = {
    for {
      raw <- backend.send(mkRequest(pairs))
    } yield for {
      bodyRaw <- raw.body.left
        .map(errors.Error.OneFrameLookupFailed.apply)
      decoded <- decode[List[ResponseRate]](bodyRaw).left
        .map(errors.Error.OneFrameMalformedResponse.apply)
    } yield decoded.map(_.toRate)
  }
}
