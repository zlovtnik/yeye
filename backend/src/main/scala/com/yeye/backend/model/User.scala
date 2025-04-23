package com.yeye.backend.model

import io.circe.generic.auto._ // Automatic codec derivation
import io.circe.{Encoder, Decoder} // Explicit Encoder/Decoder if needed later

case class User(
    id: Long,
    name: String,
    email: String
)

// Request model for creating a user (without ID)
case class CreateUserRequest(
    name: String,
    email: String
)

// Companion object to hold potential explicit codecs if auto-derivation isn't sufficient
// or if specific customizations are needed. Auto should work for these simple cases.
object UserCodecs {
  // Implicit codecs will be derived by circe.generic.auto._
  // Example of explicit derivation if needed:
  // implicit val userEncoder: Encoder[User] = io.circe.generic.semiauto.deriveEncoder
  // implicit val userDecoder: Decoder[User] = io.circe.generic.semiauto.deriveDecoder
  // implicit val createUserRequestEncoder: Encoder[CreateUserRequest] = io.circe.generic.semiauto.deriveEncoder
  // implicit val createUserRequestDecoder: Decoder[CreateUserRequest] = io.circe.generic.semiauto.deriveDecoder
}
