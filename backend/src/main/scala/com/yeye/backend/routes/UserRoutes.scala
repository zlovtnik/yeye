package com.yeye.backend.routes

import cats.effect.IO
import cats.syntax.all.* // Required for flatMap, handleErrorWith, etc.
import com.yeye.backend.model.{User, CreateUserRequest} // Import models
import com.yeye.backend.model.UserCodecs // Import codecs if needed (auto should work)
import com.yeye.backend.repository.{
  UserRepository,
  UserNotFoundException
} // Import new exception
import io.circe.syntax.* // for .asJson
import org.http4s.* // Core http4s types
import org.http4s.circe.* // Circe integration for http4s (jsonOf, EntityEncoder/Decoder)
import org.http4s.dsl.Http4sDsl // DSL for HTTP methods (GET, POST, etc.)
import io.circe.generic.auto._ // Ensure automatic codecs are in scope

object UserRoutes {

  // Define the routes, taking the repository as a dependency
  def routes(userRepository: UserRepository): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.* // Import DSL methods (Ok, NotFound, BadRequest, etc.)

    // Define JSON decoders for request entities using circe package object
    implicit val createUserRequestDecoder
        : EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]
    implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]

    // Define JSON encoders for response entities (needed for .asJson)
    implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]
    implicit val usersEncoder: EntityEncoder[IO, List[User]] =
      jsonEncoderOf[IO, List[User]]

    HttpRoutes.of[IO] {

      // POST /users - Create a new user
      case req @ POST -> Root =>
        req
          .as[CreateUserRequest]
          .flatMap { createUserReq =>
            // Create a dummy User object to pass to repository (ID will be ignored/generated)
            val userToCreate = User(
              id = 0L,
              name = createUserReq.name,
              email = createUserReq.email
            )
            userRepository.create(userToCreate).flatMap { createdUser =>
              Created(
                createdUser.asJson
              ) // Respond with 201 Created and the new user object
            }
          }
          .handleErrorWith { // Catch decoding errors or repo errors
            case MalformedMessageBodyFailure(details, _) =>
              BadRequest(s"Invalid JSON syntax: $details")
            case InvalidMessageBodyFailure(details, _) =>
              BadRequest(
                s"Invalid JSON structure: $details"
              ) // Catch structure errors
            case e =>
              InternalServerError(s"Failed to create user: ${e.getMessage}")
          }

      // GET /users - Get all users
      case GET -> Root =>
        userRepository.findAll
          .flatMap { users =>
            Ok(users.asJson)
          }
          .handleErrorWith { e =>
            // Log the error ideally
            InternalServerError(s"Failed to retrieve users: ${e.getMessage}")
          }

      // GET /users/{id} - Get a single user by ID
      case GET -> Root / LongVar(id) =>
        userRepository
          .findById(id)
          .flatMap {
            case Some(user) => Ok(user.asJson)
            case None       => NotFound(s"User with ID $id not found")
          }
          .handleErrorWith { e =>
            // Log the error ideally
            InternalServerError(s"Failed to retrieve user $id: ${e.getMessage}")
          }

      // PUT /users/{id} - Update a user by ID
      case req @ PUT -> Root / LongVar(id) =>
        req
          .as[User]
          .flatMap { userUpdate =>
            // Ensure the ID in the path matches the ID in the body for consistency
            if (userUpdate.id != id) {
              BadRequest(
                s"ID in URL path ($id) does not match ID in request body (${userUpdate.id})."
              )
            } else {
              // Attempt to update the user in the repository
              userRepository
                .update(userUpdate)
                .flatMap { updatedUser =>
                  Ok(
                    updatedUser.asJson
                  ) // Respond with 200 OK and the updated user
                }
                .recoverWith { // Only recover specific UserNotFoundException from repo
                  case UserNotFoundException(_, _) =>
                    NotFound(s"User with ID $id not found for update")
                }
            }
          }
          .handleErrorWith { // Handle decoding errors or other unexpected errors
            case MalformedMessageBodyFailure(details, _) =>
              BadRequest(s"Invalid JSON syntax: $details")
            case InvalidMessageBodyFailure(details, _) =>
              BadRequest(
                s"Invalid JSON structure: $details"
              ) // Catch structure errors
            case e =>
              InternalServerError(s"Failed to update user $id: ${e.getMessage}")
          }

      // DELETE /users/{id} - Delete a user by ID
      case DELETE -> Root / LongVar(id) =>
        userRepository
          .delete(id)
          .flatMap { _ =>
            NoContent() // Respond with 204 No Content on successful deletion
          }
          .recoverWith { // Only recover specific UserNotFoundException from repo
            case UserNotFoundException(_, _) =>
              NotFound(s"User with ID $id not found for deletion")
          }
          .handleErrorWith { e => // Catch any other unexpected errors
            InternalServerError(s"Failed to delete user $id: ${e.getMessage}")
          }
    }
  }
}
