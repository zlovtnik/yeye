package com.yeye.backend.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.yeye.backend.model.Sale
import com.yeye.backend.repository.SaleRepository
import doobie.{Transactor, Update0}
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate
import java.util.Properties
import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource

class SaleRepositorySpec extends AnyFunSuite with Matchers {
  // Test database configuration
  private val dataSource: DataSource = {
    val ds = new HikariDataSource()
    ds.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb")
    ds.setUsername("postgres")
    ds.setPassword("postgres")
    ds
  }

  private val repository = SaleRepository.live(dataSource)

  test("findAll should return all sales") {
    val result = repository.findAll.unsafeRunSync()
    result should not be empty
  }

  test("findByDateRange should return sales within the specified date range") {
    val startDate = LocalDate.of(2023, 1, 1)
    val endDate = LocalDate.of(2023, 12, 31)
    val result = repository.findByDateRange(startDate, endDate).unsafeRunSync()
    result should not be empty
  }

  test("findByProductId should return sales for the specified product") {
    val productId = 1L
    val result = repository.findByProductId(productId).unsafeRunSync()
    result should not be empty
  }

  test("findByCustomerId should return sales for the specified customer") {
    val customerId = 1L
    val result = repository.findByCustomerId(customerId).unsafeRunSync()
    result should not be empty
  }

  test("getTotalSalesByDateRange should return the total sales amount") {
    val startDate = LocalDate.of(2023, 1, 1)
    val endDate = LocalDate.of(2023, 12, 31)
    val result =
      repository.getTotalSalesByDateRange(startDate, endDate).unsafeRunSync()
    result should be > BigDecimal(0)
  }

  test("getSalesStatsByChannel should return sales statistics by channel") {
    val result = repository.getSalesStatsByChannel.unsafeRunSync()
    result should not be empty
  }

  test("getSalesTrendByDateRange should return sales trend data") {
    val startDate = LocalDate.of(2023, 1, 1)
    val endDate = LocalDate.of(2023, 12, 31)
    val result =
      repository.getSalesTrendByDateRange(startDate, endDate).unsafeRunSync()
    result should not be empty
  }
}
