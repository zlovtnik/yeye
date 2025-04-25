package com.yeye.frontend.types

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/** Represents a file in the system
  *
  * This case class models file metadata for display and manipulation in the UI.
  *
  * @param id
  *   Unique identifier for the file
  * @param name
  *   Display name of the file
  * @param size
  *   Size of the file in bytes
  * @param fileType
  *   MIME type or extension of the file
  * @param lastModified
  *   Timestamp of last modification (milliseconds since epoch)
  */
case class File(
    id: String,
    name: String,
    size: Long,
    fileType: String,
    lastModified: Long
)

/** Companion object for File providing JSON encoding/decoding */
object File {

  /** Circe decoder for File */
  implicit val decoder: Decoder[File] = deriveDecoder[File]

  /** Circe encoder for File */
  implicit val encoder: Encoder[File] = deriveEncoder[File]
}

/** Represents a user in the system
  *
  * This case class models user data for the application.
  *
  * @param id
  *   Unique identifier for the user
  * @param name
  *   Full name of the user
  * @param email
  *   Email address of the user
  * @param age
  *   Age of the user in years
  */
case class User(
    id: String,
    name: String,
    email: String,
    age: Int
)

/** Companion object for User providing JSON encoding/decoding */
object User {

  /** Circe decoder for User */
  implicit val decoder: Decoder[User] = deriveDecoder[User]

  /** Circe encoder for User */
  implicit val encoder: Encoder[User] = deriveEncoder[User]
}

/** Request object for creating a new user
  *
  * This case class contains the data needed to create a new user via the API.
  * It omits the ID since it will be assigned by the backend.
  *
  * @param name
  *   Full name of the user to create
  * @param email
  *   Email address of the user to create
  * @param age
  *   Age of the user in years
  */
case class CreateUserRequest(
    name: String,
    email: String,
    age: Int
)

/** Companion object for CreateUserRequest providing JSON encoding/decoding */
object CreateUserRequest {

  /** Circe decoder for CreateUserRequest */
  implicit val decoder: Decoder[CreateUserRequest] =
    deriveDecoder[CreateUserRequest]

  /** Circe encoder for CreateUserRequest */
  implicit val encoder: Encoder[CreateUserRequest] =
    deriveEncoder[CreateUserRequest]
}
