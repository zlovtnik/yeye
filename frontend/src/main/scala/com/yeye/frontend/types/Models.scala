package com.yeye.frontend.types

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/** Represents a file in the system */
case class File(
    id: String,
    name: String,
    size: Long,
    fileType: String,
    lastModified: Long
)

object File:
  implicit val decoder: Decoder[File] = deriveDecoder[File]
  implicit val encoder: Encoder[File] = deriveEncoder[File]

case class User(
    id: String,
    name: String,
    email: String,
    age: Int
)

object User {
  implicit val decoder: Decoder[User] = deriveDecoder[User]
  implicit val encoder: Encoder[User] = deriveEncoder[User]
}

case class CreateUserRequest(
    name: String,
    email: String,
    age: Int
)

object CreateUserRequest {
  implicit val decoder: Decoder[CreateUserRequest] =
    deriveDecoder[CreateUserRequest]
  implicit val encoder: Encoder[CreateUserRequest] =
    deriveEncoder[CreateUserRequest]
}
