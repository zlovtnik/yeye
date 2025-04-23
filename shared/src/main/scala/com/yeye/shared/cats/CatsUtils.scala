package com.yeye.shared.cats

import cats.data.EitherT
import cats.data.OptionT
import cats.data.Validated
import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import io.circe.syntax.*

object CatsUtils {
  // Type aliases for common cats types
  type EitherT[F[_], A, B] = cats.data.EitherT[F, A, B]
  type OptionT[F[_], A] = cats.data.OptionT[F, A]

  // Common typeclass instances
  implicit class EitherOps[E, A](val e: Either[E, A]) extends AnyVal {
    def toEitherT[F[_]: cats.Applicative]: EitherT[F, E, A] =
      EitherT.fromEither[F](e)
  }

  implicit class OptionOps[A](val o: Option[A]) extends AnyVal {
    def toOptionT[F[_]: cats.Applicative]: OptionT[F, A] =
      OptionT.fromOption[F](o)
  }

  // Example of using cats for validation
  case class User(name: String, age: Int)

  def validateUser(name: String, age: Int): Validated[String, User] = (
    Validated.cond(name.nonEmpty, name, "Name cannot be empty"),
    Validated.cond(age > 0, age, "Age must be positive")
  ).mapN(User.apply)

  // Example of using cats for error handling with EitherT
  def processUser[F[_]: cats.Monad](id: String): EitherT[F, String, User] =
    for {
      name <- EitherT.fromOptionF[F, String, String](
        cats.Monad[F].pure(None),
        "User not found"
      )
      age <- EitherT.rightT[F, String](25)
    } yield User(name, age)

  // Example of using OptionT for chaining optional operations
  def findUser[F[_]: cats.Monad](id: String): OptionT[F, User] = for {
    name <- OptionT.fromOption[F](Some("John"))
    age <- OptionT.fromOption[F](Some(30))
  } yield User(name, age)

  // Example of using circe for JSON handling
  implicit val userEncoder: Encoder[User] =
    Encoder.forProduct2("name", "age")(u => (u.name, u.age))
  implicit val userDecoder: Decoder[User] =
    Decoder.forProduct2("name", "age")(User.apply)

  def userToJson(user: User): String = user.asJson.noSpaces
  def jsonToUser(json: String): Either[String, User] =
    io.circe.parser.decode[User](json).leftMap(_.getMessage)
}
