package com.yeye.backend.repository

import cats.effect.IO
import com.yeye.backend.model.{Sale, SalesTrend}
import javax.sql.DataSource
import java.sql.{Connection, ResultSet}
import java.time.LocalDate
import scala.util.Try
import scala.math.BigDecimal

/** Repository trait for sale data access operations.
  */
trait SaleRepository {

  /** Retrieves all sales from the database.
    *
    * @return
    *   A list of all sales
    */
  def findAll: IO[List[Sale]]

  /** Retrieves sales within a date range.
    *
    * @param startDate
    *   The start date
    * @param endDate
    *   The end date
    * @return
    *   A list of sales within the date range
    */
  def findByDateRange(startDate: LocalDate, endDate: LocalDate): IO[List[Sale]]

  /** Retrieves sales by product ID.
    *
    * @param productId
    *   The product ID
    * @return
    *   A list of sales for the product
    */
  def findByProductId(productId: Long): IO[List[Sale]]

  /** Retrieves sales by customer ID.
    *
    * @param customerId
    *   The customer ID
    * @return
    *   A list of sales for the customer
    */
  def findByCustomerId(customerId: Long): IO[List[Sale]]

  /** Retrieves total sales amount within a date range.
    *
    * @param startDate
    *   The start date
    * @param endDate
    *   The end date
    * @return
    *   The total sales amount
    */
  def getTotalSalesByDateRange(
      startDate: LocalDate,
      endDate: LocalDate
  ): IO[BigDecimal]

  /** Retrieves sales statistics by channel.
    *
    * @return
    *   A list of channel IDs with their total sales and quantities
    */
  def getSalesStatsByChannel: IO[List[(Int, BigDecimal, Int)]]

  /** Retrieves sales trend by date range.
    *
    * @param startDate
    *   The start date
    * @param endDate
    *   The end date
    * @return
    *   A list of dates with their total sales
    */
  def getSalesTrendByDateRange(
      startDate: LocalDate,
      endDate: LocalDate
  ): IO[List[SalesTrend]]
}

/** Companion object for SaleRepository providing the live implementation.
  */
object SaleRepository {

  /** Live implementation of SaleRepository using plain JDBC with Cats.
    */
  def live(dataSource: DataSource): SaleRepository =
    new SaleRepository {
      private def withConnection[A](f: Connection => A): IO[A] = {
        IO.fromTry(Try {
          val conn = dataSource.getConnection
          try f(conn)
          finally conn.close()
        })
      }

      private def executeQuery[A](sql: String, params: List[Any] = Nil)(
          f: ResultSet => A
      ): IO[A] = {
        withConnection { conn =>
          val stmt = conn.prepareStatement(sql)
          try {
            params.zipWithIndex.foreach { case (param, idx) =>
              stmt.setObject(idx + 1, param)
            }
            val rs = stmt.executeQuery()
            try f(rs)
            finally rs.close()
          } finally stmt.close()
        }
      }

      private def mapSale(rs: ResultSet): Sale = {
        Sale(
          productId = rs.getLong("PRODUCT_ID"),
          customerId = rs.getLong("CUSTOMER_ID"),
          timeId = rs.getDate("TIME_ID").toLocalDate(),
          channelId = rs.getLong("CHANNEL_ID"),
          promoId = Option(rs.getLong("PROMO_ID")),
          quantitySold = rs.getInt("QUANTITY_SOLD"),
          amountSold = rs.getDouble("AMOUNT_SOLD")
        )
      }

      private def mapSalesTrend(rs: ResultSet): SalesTrend = {
        SalesTrend(
          date = rs.getDate("TIME_ID").toLocalDate,
          amount = rs.getBigDecimal("daily_amount"),
          quantity = rs.getInt("daily_quantity")
        )
      }

      override def findAll: IO[List[Sale]] = {
        executeQuery(
          """
            SELECT PROD_ID, CUST_ID, TIME_ID, CHANNEL_ID, PROMO_ID, QUANTITY_SOLD, AMOUNT_SOLD
            FROM SALES
          """
        ) { rs =>
          Iterator.continually(rs).takeWhile(_.next()).map(mapSale).toList
        }
      }

      override def findByDateRange(
          startDate: LocalDate,
          endDate: LocalDate
      ): IO[List[Sale]] = {
        executeQuery(
          """
            SELECT PROD_ID, CUST_ID, TIME_ID, CHANNEL_ID, PROMO_ID, QUANTITY_SOLD, AMOUNT_SOLD
            FROM SALES
            WHERE TIME_ID BETWEEN ? AND ?
          """,
          List(startDate, endDate)
        ) { rs =>
          Iterator.continually(rs).takeWhile(_.next()).map(mapSale).toList
        }
      }

      override def findByProductId(productId: Long): IO[List[Sale]] = {
        executeQuery(
          """
            SELECT PROD_ID, CUST_ID, TIME_ID, CHANNEL_ID, PROMO_ID, QUANTITY_SOLD, AMOUNT_SOLD
            FROM SALES
            WHERE PROD_ID = ?
          """,
          List(productId)
        ) { rs =>
          Iterator.continually(rs).takeWhile(_.next()).map(mapSale).toList
        }
      }

      override def findByCustomerId(customerId: Long): IO[List[Sale]] = {
        executeQuery(
          """
            SELECT PROD_ID, CUST_ID, TIME_ID, CHANNEL_ID, PROMO_ID, QUANTITY_SOLD, AMOUNT_SOLD
            FROM SALES
            WHERE CUST_ID = ?
          """,
          List(customerId)
        ) { rs =>
          Iterator.continually(rs).takeWhile(_.next()).map(mapSale).toList
        }
      }

      override def getTotalSalesByDateRange(
          startDate: LocalDate,
          endDate: LocalDate
      ): IO[BigDecimal] = {
        executeQuery(
          """
            SELECT SUM(AMOUNT_SOLD)
            FROM SALES
            WHERE TIME_ID BETWEEN ? AND ?
          """,
          List(startDate, endDate)
        ) { rs =>
          if (rs.next()) rs.getBigDecimal(1) else BigDecimal(0)
        }
      }

      override def getSalesStatsByChannel: IO[List[(Int, BigDecimal, Int)]] = {
        executeQuery(
          """
            SELECT CHANNEL_ID, SUM(AMOUNT_SOLD) as total_amount, SUM(QUANTITY_SOLD) as total_quantity
            FROM SALES
            GROUP BY CHANNEL_ID
            ORDER BY total_amount DESC
          """
        ) { rs =>
          Iterator
            .continually(rs)
            .takeWhile(_.next())
            .map(rs =>
              (rs.getInt(1), BigDecimal(rs.getBigDecimal(2)), rs.getInt(3))
            )
            .toList
        }
      }

      override def getSalesTrendByDateRange(
          startDate: LocalDate,
          endDate: LocalDate
      ): IO[List[SalesTrend]] = {
        executeQuery(
          """
            SELECT TIME_ID, SUM(AMOUNT_SOLD) as daily_amount, SUM(QUANTITY_SOLD) as daily_quantity
            FROM SALES
            WHERE TIME_ID BETWEEN ? AND ?
            GROUP BY TIME_ID
            ORDER BY TIME_ID
          """,
          List(startDate, endDate)
        ) { rs =>
          Iterator.continually(rs).takeWhile(_.next()).map(mapSalesTrend).toList
        }
      }
    }
}
