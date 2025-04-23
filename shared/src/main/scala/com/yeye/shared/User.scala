package com.yeye.shared

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}

case class User(
    id: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    created: Long,
    lastUpdated: Long
)

object User {
  given Encoder[User] = deriveEncoder[User]
  given Decoder[User] = deriveDecoder[User]

  def empty: User = User(
    id = "",
    email = "",
    firstName = "",
    lastName = "",
    status = "",
    created = 0,
    lastUpdated = 0
  )
}
