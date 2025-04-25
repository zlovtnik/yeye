package com.yeye.backend.model

import java.time.LocalDate
import io.circe.generic.auto._
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}

/** Model representing a sale record from the SALES table
  *
  * @param productId
  *   The product identifier
  * @param customerId
  *   The customer identifier
  * @param timeId
  *   The date of the sale
  * @param channelId
  *   The sales channel identifier
  * @param promoId
  *   The promotion identifier
  * @param quantitySold
  *   The quantity of products sold
  * @param amountSold
  *   The total amount of the sale
  */
case class Sale(
    productId: Long,
    customerId: Long,
    timeId: LocalDate,
    channelId: Long,
    promoId: Option[Long],
    quantitySold: Int,
    amountSold: Double
)

// Request model for creating a sale
case class CreateSaleRequest(
    productId: Long,
    customerId: Long,
    timeId: LocalDate,
    channelId: Int,
    promoId: Long,
    quantitySold: Int,
    amountSold: BigDecimal
)

// Response model for sales statistics
case class SalesStats(
    totalAmount: BigDecimal,
    totalQuantity: Int,
    averageAmount: BigDecimal
)

// Response model for sales trend
case class SalesTrend(
    date: LocalDate,
    amount: BigDecimal,
    quantity: Int
)

// Companion object for codecs
object SaleCodecs {
  implicit val saleEncoder: Encoder[Sale] = deriveEncoder[Sale]
  implicit val saleDecoder: Decoder[Sale] = deriveDecoder[Sale]

  implicit val createSaleRequestEncoder: Encoder[CreateSaleRequest] =
    deriveEncoder[CreateSaleRequest]
  implicit val createSaleRequestDecoder: Decoder[CreateSaleRequest] =
    deriveDecoder[CreateSaleRequest]

  implicit val salesStatsEncoder: Encoder[SalesStats] =
    deriveEncoder[SalesStats]
  implicit val salesStatsDecoder: Decoder[SalesStats] =
    deriveDecoder[SalesStats]

  implicit val salesTrendEncoder: Encoder[SalesTrend] =
    deriveEncoder[SalesTrend]
  implicit val salesTrendDecoder: Decoder[SalesTrend] =
    deriveDecoder[SalesTrend]
}
