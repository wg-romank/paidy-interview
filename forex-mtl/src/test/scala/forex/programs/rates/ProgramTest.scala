package forex.programs.rates

import cats.effect.IO
import forex.programs.rates.errors.Error._
import forex.domain.{Rate, Timestamp}
import forex.programs.rates.Program.CachedRates
import forex.programs.rates.errors.Error.RateExpired
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.duration.FiniteDuration

class ProgramTest extends AnyFunSuite {
  import forex.fixtures.Rates._

  def mkCachedRates(rates: List[Rate]): CachedRates[IO] = new CachedRates[IO] {
    override def get: IO[Map[Rate.Pair, Rate]] = IO.pure(rates.map(r => r.pair -> r).toMap)
  }

  private val rateTtl = FiniteDuration.apply(1, "minute")

  test("should return rates") {
    val program = new Program[IO](rateTtl, mkCachedRates(List(rate)))

    val result = program.get(now, pair).unsafeRunSync()

    assert(result.contains(rate))
  }

  test("should return error if rate is expired") {
    val program = new Program[IO](rateTtl, mkCachedRates(List(rate)))

    val result = program.get(Timestamp(now.value.plusDays(1)), pair).unsafeRunSync()

    assert(result match {
      case Left(RateExpired(_, _)) => true
      case _ => false
    })
  }

  test("shoud return error if rate is not in cache") {
    val program = new Program[IO](rateTtl, mkCachedRates(List.empty))

    val result = program.get(now, pair).unsafeRunSync()

    assert(result match {
      case Left(RateNotInCache(_, _)) => true
      case _ => false
    })
  }

}
