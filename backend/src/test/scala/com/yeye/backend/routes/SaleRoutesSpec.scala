package com.yeye.backend

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.yeye.backend.model.{Sale, SaleCodecs}
import com.yeye.backend.repository.SaleRepository
import com.yeye.backend.routes.SaleRoutes
import doobie.implicits.*
import doobie.util.transactor.Transactor
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.{
  circeEntityDecoder,
  circeEntityEncoder
}
import org.http4s.syntax.literals.uri
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{mock, when}
import org.mockito.ArgumentMatchers.any
import java.time.LocalDate
import java.util.Properties

class SaleRoutesSpec extends AnyFunSuite with Matchers with MockitoSugar {
  import com.yeye.backend.model.SaleCodecs.*

  // Test data
  val testSale = Sale(
    productId = 1L,
    customerId = 1L,
    timeId = LocalDate.now(),
    channelId = 1L,
    promoId = Some(1L),
    quantitySold = 5,
    amountSold = 100.0
  )

  // Test transactor (using H2 in-memory database)
  val transactor = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    new Properties(),
    None
  )

  val repo = mock[SaleRepository]
  val routes = new SaleRoutes(repo).routes

  test("GET /sales should return all sales") {
    when(repo.findAll).thenReturn(IO.pure(List(testSale)))

    val request = Request[IO](Method.GET, uri"/sales")
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.Ok
    val body = response.as[List[Sale]].unsafeRunSync()
    body should contain(testSale)
  }

  test("GET /sales/date-range should return sales within date range") {
    val startDate = LocalDate.now().minusDays(1)
    val endDate = LocalDate.now().plusDays(1)
    when(repo.findByDateRange(any[LocalDate], any[LocalDate]))
      .thenReturn(IO.pure(List(testSale)))

    val request = Request[IO](
      Method.GET,
      uri"/sales/date-range"
        .withQueryParam("start", startDate.toString)
        .withQueryParam("end", endDate.toString)
    )
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.Ok
    val body = response.as[List[Sale]].unsafeRunSync()
    body should contain(testSale)
  }

  test("GET /sales/product/{id} should return sales for product") {
    when(repo.findByProductId(any[Long])).thenReturn(IO.pure(List(testSale)))

    val request = Request[IO](Method.GET, uri"/sales/product/1")
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.Ok
    val body = response.as[List[Sale]].unsafeRunSync()
    body should contain(testSale)
  }

  test("GET /sales/customer/{id} should return sales for customer") {
    when(repo.findByCustomerId(any[Long])).thenReturn(IO.pure(List(testSale)))

    val request = Request[IO](Method.GET, uri"/sales/customer/1")
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.Ok
    val body = response.as[List[Sale]].unsafeRunSync()
    body should contain(testSale)
  }

  test("GET /sales/total should return total sales amount") {
    val startDate = LocalDate.now().minusDays(1)
    val endDate = LocalDate.now().plusDays(1)
    when(repo.getTotalSalesByDateRange(any[LocalDate], any[LocalDate]))
      .thenReturn(IO.pure(BigDecimal(100.0)))

    val request = Request[IO](
      Method.GET,
      uri"/sales/total"
        .withQueryParam("start", startDate.toString)
        .withQueryParam("end", endDate.toString)
    )
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.Ok
    val body = response.as[Double].unsafeRunSync()
    body shouldBe 100.0
  }

  test("GET /sales/date-range with invalid dates should return BadRequest") {
    val request = Request[IO](
      Method.GET,
      uri"/sales/date-range"
        .withQueryParam("start", "invalid-date")
        .withQueryParam("end", "invalid-date")
    )
    val response = routes.run(request).value.unsafeRunSync().get

    response.status shouldBe Status.BadRequest
  }
}
