package com.yeye.backend.routes

import cats.effect.IO
import cats.syntax.all.*
import com.yeye.backend.model.{
  Sale,
  CreateSaleRequest,
  SalesStats,
  SalesTrend,
  SaleCodecs
}
import com.yeye.backend.repository.SaleRepository
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** HTTP routes for sales operations
  *
  * @param saleRepository
  *   The repository for sales operations
  */
class SaleRoutes(saleRepository: SaleRepository) {

  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  val routes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*

    // Define JSON decoders for request entities
    implicit val createSaleRequestDecoder
        : EntityDecoder[IO, CreateSaleRequest] = jsonOf[IO, CreateSaleRequest]
    implicit val saleDecoder: EntityDecoder[IO, Sale] = jsonOf[IO, Sale]

    // Define JSON encoders for response entities
    implicit val saleEncoder: EntityEncoder[IO, Sale] = jsonEncoderOf[IO, Sale]
    implicit val salesEncoder: EntityEncoder[IO, List[Sale]] =
      jsonEncoderOf[IO, List[Sale]]
    implicit val salesStatsEncoder
        : EntityEncoder[IO, List[(Int, BigDecimal, Int)]] =
      jsonEncoderOf[IO, List[(Int, BigDecimal, Int)]]
    implicit val salesTrendEncoder: EntityEncoder[IO, List[SalesTrend]] =
      jsonEncoderOf[IO, List[SalesTrend]]
    implicit val bigDecimalEncoder: EntityEncoder[IO, BigDecimal] =
      jsonEncoderOf[IO, BigDecimal]

    HttpRoutes.of[IO] {
      // Get all sales
      case GET -> Root =>
        saleRepository.findAll
          .flatMap(sales => Ok(sales.asJson))
          .handleErrorWith(e =>
            InternalServerError(s"Failed to retrieve sales: ${e.getMessage}")
          )

      // Get sales by date range
      case GET -> Root / "date-range" / startDate / endDate =>
        (for {
          start <- IO(LocalDate.parse(startDate, dateFormatter))
          end <- IO(LocalDate.parse(endDate, dateFormatter))
          sales <- saleRepository.findByDateRange(start, end)
          response <- Ok(sales.asJson)
        } yield response)
          .handleErrorWith {
            case _: java.time.format.DateTimeParseException =>
              BadRequest("Invalid date format. Use YYYY-MM-DD")
            case e =>
              InternalServerError(s"Failed to retrieve sales: ${e.getMessage}")
          }

      // Get sales by product ID
      case GET -> Root / "product" / LongVar(productId) =>
        saleRepository
          .findByProductId(productId)
          .flatMap(sales => Ok(sales.asJson))
          .handleErrorWith(e =>
            InternalServerError(s"Failed to retrieve sales: ${e.getMessage}")
          )

      // Get sales by customer ID
      case GET -> Root / "customer" / LongVar(customerId) =>
        saleRepository
          .findByCustomerId(customerId)
          .flatMap(sales => Ok(sales.asJson))
          .handleErrorWith(e =>
            InternalServerError(s"Failed to retrieve sales: ${e.getMessage}")
          )

      // Get total sales amount by date range
      case GET -> Root / "total" / startDate / endDate =>
        (for {
          start <- IO(LocalDate.parse(startDate, dateFormatter))
          end <- IO(LocalDate.parse(endDate, dateFormatter))
          total <- saleRepository.getTotalSalesByDateRange(start, end)
          response <- Ok(total.asJson)
        } yield response)
          .handleErrorWith {
            case _: java.time.format.DateTimeParseException =>
              BadRequest("Invalid date format. Use YYYY-MM-DD")
            case e =>
              InternalServerError(
                s"Failed to retrieve total sales: ${e.getMessage}"
              )
          }

      // Get sales statistics by channel
      case GET -> Root / "stats" / "channel" =>
        saleRepository.getSalesStatsByChannel
          .flatMap(stats => Ok(stats.asJson))
          .handleErrorWith(e =>
            InternalServerError(
              s"Failed to retrieve sales stats: ${e.getMessage}"
            )
          )

      // Get sales trend by date range
      case GET -> Root / "trend" / startDate / endDate =>
        (for {
          start <- IO(LocalDate.parse(startDate, dateFormatter))
          end <- IO(LocalDate.parse(endDate, dateFormatter))
          trend <- saleRepository.getSalesTrendByDateRange(start, end)
          response <- Ok(trend.asJson)
        } yield response)
          .handleErrorWith {
            case _: java.time.format.DateTimeParseException =>
              BadRequest("Invalid date format. Use YYYY-MM-DD")
            case e =>
              InternalServerError(
                s"Failed to retrieve sales trend: ${e.getMessage}"
              )
          }
    }
  }
}

object SaleRoutes {
  def apply(saleRepository: SaleRepository): SaleRoutes = new SaleRoutes(
    saleRepository
  )
}
