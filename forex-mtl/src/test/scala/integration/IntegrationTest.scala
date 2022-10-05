package integration

import java.time.{OffsetDateTime, ZoneId}

import forex.config.ApplicationConfig
import forex.domain.Rate.Pair
import forex.domain.{Price, Rate, Timestamp}
import forex.http.rates.Protocol.GetApiResponse
import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}
import sttp.client3._
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.Decoder
import org.scalatest.Assertion
import pureconfig.generic.auto._

import scala.math.Ordering.Implicits._
import scala.util.Random
import pureconfig.ConfigSource

class IntegrationTest extends AnyFunSuite {
  val client: SttpBackend[Identity, Any] = HttpClientSyncBackend()

  import forex.services.rates.interpreters.ResponseRate.currencyDecoder
  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.decodeOffsetDateTime.map(Timestamp.apply)
  implicit val priceDecoder: Decoder[Price] = Decoder.decodeBigDecimal.map(Price.apply)

  val config = ConfigSource.default.at("app").loadOrThrow[ApplicationConfig]

  test("integration test") {
    (1 to 10000).foreach { _ =>
      val now = Timestamp(OffsetDateTime.now(ZoneId.of("UTC")))
      val pair = Random.shuffle(Rate.pairs).head
      assertRequest(now, pair)
    }
  }

  def assertRequest(now: Timestamp, pair: Pair): Assertion = {
    val request = basicRequest.get(
      uri"http://${config.http.host}:${config.http.port}/rates?from=${pair.from}&to=${pair.to}")

    val result = for {
      raw <- client.send(request).body
      parsed <- decode[GetApiResponse](raw)
    } yield parsed

    result match {
      case Right(rate) =>
        assert(rate.from == pair.from)
        assert(rate.to == pair.to)
        assert(rate.timestamp + config.rateTtl >= now)
      case Left(cause) =>
        assert(false, cause)
    }
  }
}
