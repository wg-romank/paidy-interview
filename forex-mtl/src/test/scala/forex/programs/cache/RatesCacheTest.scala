package forex.programs.cache

import cats.effect.IO
import forex.domain.{Rate, Timestamp}
import forex.domain.Rate.Pair
import forex.services.RatesService
import forex.services.rates.errors
import org.scalatest.funsuite.AnyFunSuite

class RatesCacheTest extends AnyFunSuite {
  import forex.fixtures.Rates._

  def mkService(rates: List[Rate]): RatesService[IO] =
    (_: List[Rate.Pair]) => IO.pure(Right(rates))

  test("should set fetched values") {
    val cache = new RatesCache[IO](mkService(List(rate)))
    val result = cache.fetch(Map.empty).unsafeRunSync()

    assertResult(result, pair, rate)
  }

  test("should keep old values if no update is fetched") {
    val cache = new RatesCache[IO](mkService(List.empty))
    val result = cache.fetch(Map(pair -> rate)).unsafeRunSync()

    assertResult(result, pair, rate)
  }

  test("should keep old values in case of fetch error") {
    val service = new RatesService[IO] {
      override def get(pair: List[Pair]): IO[Either[errors.Error, List[Rate]]] =
        IO.pure(Left(errors.Error.OneFrameLookupFailed("boom")))
    }
    val cache = new RatesCache[IO](service)
    val result = cache.fetch(Map(pair -> rate)).unsafeRunSync()

    assertResult(result, pair, rate)
  }

  test("should keep old values if they have more recent timestamp") {
    val cache = new RatesCache[IO](
      mkService(List(rate.copy(timestamp = Timestamp(rate.timestamp.value.minusDays(1)))))
    )
    val result = cache.fetch(Map(pair -> rate)).unsafeRunSync()

    assertResult(result, pair, rate)
  }

  private def assertResult(result: Map[Pair, Rate], pair: Pair, rate: Rate) = {
    assert(result.get(pair) match {
      case Some(r) => r == rate
      case None => false
    })
  }

}
