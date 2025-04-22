package com.yeye.backend.model

import zio.json.*

case class User(
    id: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    created: Long,
    lastUpdated: Long
)

object User:
  given JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]
