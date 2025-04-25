package com.yeye.backend

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.yeye.backend.model.{Sale, SaleCodecs}
import com.yeye.backend.repository.SaleRepository
import doobie.implicits.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate
import java.util.Properties
import io.circe.parser.*
import io.circe.syntax.*
import com.yeye.backend.model.SaleCodecs.given
import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource

class SaleSpec extends AnyFunSuite with Matchers {
  // Test data
  val testSale = Sale(
    productId = 1,
    customerId = 1,
    timeId = LocalDate.now(),
    channelId = 1,
    promoId = Some(1),
    quantitySold = 5,
    amountSold = 100.0
  )

  // Test database configuration
  private val dataSource: DataSource = {
    val ds = new HikariDataSource()
    ds.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    ds.setUsername("sa")
    ds.setPassword("")
    ds
  }

  private val repository = SaleRepository.live(dataSource)

  test("Sale model should encode/decode to/from JSON correctly") {
    val json = testSale.asJson.noSpaces
    val decoded = decode[Sale](json)

    decoded shouldBe Right(testSale)
  }

  test("SaleRepository should find all sales") {
    val result = repository.findAll.unsafeRunSync()
    result should not be empty
  }

  test("SaleRepository should find sales by date range") {
    val startDate = LocalDate.now().minusDays(1)
    val endDate = LocalDate.now().plusDays(1)
    val result = repository.findByDateRange(startDate, endDate).unsafeRunSync()
    result should not be empty
  }

  test("SaleRepository should find sales by product ID") {
    val result = repository.findByProductId(testSale.productId).unsafeRunSync()
    result should not be empty
  }

  test("SaleRepository should find sales by customer ID") {
    val result =
      repository.findByCustomerId(testSale.customerId).unsafeRunSync()
    result should not be empty
  }

  test("SaleRepository should calculate total sales by date range") {
    val startDate = LocalDate.now().minusDays(1)
    val endDate = LocalDate.now().plusDays(1)
    val result =
      repository.getTotalSalesByDateRange(startDate, endDate).unsafeRunSync()
    result should be > BigDecimal(0)
  }
}
