package com.yeye.backend.routes

import cats.effect.IO
import cats.effect.Ref // Import Ref
import cats.syntax.all.* // Provides .void
import com.yeye.backend.model.{User, CreateUserRequest}
import com.yeye.backend.repository.{
  UserRepository,
  UserNotFoundException
} // Import custom exception
import io.circe.Json
import io.circe.syntax.* // Provides .asJson
import org.http4s.* // Core types
import org.http4s.circe.* // Circe integration
import org.http4s.dsl.io.* // Http4sDsl methods like Ok, NotFound
import org.http4s.implicits.* // Provides uri"..." syntax
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.effect.testing.scalatest.AsyncIOSpec // Base for IO tests
import io.circe.generic.auto._ // Automatic codec derivation

class UserRoutesSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  // --- Test Helpers ---
  private def request(method: Method, uri: Uri): Request[IO] =
    Request[IO](method = method, uri = uri)
  private def requestWithBody[A](method: Method, uri: Uri, body: A)(implicit
      encoder: EntityEncoder[IO, A]
  ): Request[IO] = request(method, uri).withEntity(body)
  private def check[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A] = None
  )(implicit ev: EntityDecoder[IO, A]): IO[Unit] = actual.flatMap { response =>
    {
      val statusCheck = IO(response.status shouldBe expectedStatus);
      val bodyCheck = expectedBody match {
        case Some(expected) => response.as[A].asserting(_ shouldBe expected);
        case None => response.body.compile.toVector.asserting(_ shouldBe empty)
      }; statusCheck *> bodyCheck.void
    }
  }
  private def checkStringBody(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBodySubstring: String
  ): IO[Unit] = actual.flatMap { response =>
    {
      val statusCheck = IO(response.status shouldBe expectedStatus);
      val bodyCheck =
        response.as[String].asserting(_ should include(expectedBodySubstring));
      statusCheck *> bodyCheck.void
    }
  }

  // --- Define necessary codecs ---
  implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]
  implicit val usersDecoder: EntityDecoder[IO, List[User]] =
    jsonOf[IO, List[User]]
  implicit val createUserRequestEncoder: EntityEncoder[IO, CreateUserRequest] =
    jsonEncoderOf[IO, CreateUserRequest]
  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]

  // --- Mock UserRepository using Ref ---
  private def createMockUserRepository(
      initialUsers: Map[Long, User] = Map.empty,
      failOn: Option[String] = None
  ): IO[UserRepository] =
    for {
      usersRef <- Ref[IO].of(initialUsers)
      nextIdRef <- Ref[IO].of(initialUsers.keys.maxOption.getOrElse(0L) + 1)
    } yield new UserRepository {

      override def findById(id: Long): IO[Option[User]] =
        if (failOn.contains("findById"))
          IO.raiseError(new RuntimeException("Mock findById failed"))
        else usersRef.get.map(_.get(id))

      override def findAll: IO[List[User]] =
        if (failOn.contains("findAll"))
          IO.raiseError(new RuntimeException("Mock findAll failed"))
        else usersRef.get.map(_.values.toList.sortBy(_.id))

      override def create(user: User): IO[User] =
        if (failOn.contains("create"))
          IO.raiseError(new RuntimeException("Mock create failed"))
        else
          for {
            newId <- nextIdRef.getAndUpdate(_ + 1)
            newUser = user.copy(id = newId)
            _ <- usersRef.update(_ + (newId -> newUser))
          } yield newUser

      override def update(user: User): IO[User] =
        if (failOn.contains("update"))
          IO.raiseError(new RuntimeException("Mock update failed"))
        else
          usersRef.modify { currentUsers =>
            if (currentUsers.contains(user.id)) {
              (currentUsers.updated(user.id, user), IO.pure(user))
            } else {
              (
                currentUsers,
                IO.raiseError(UserNotFoundException(user.id, "update"))
              )
            }
          }.flatten

      override def delete(id: Long): IO[Unit] =
        if (failOn.contains("delete"))
          IO.raiseError(new RuntimeException("Mock delete failed"))
        else
          usersRef.modify { currentUsers =>
            if (currentUsers.contains(id)) {
              (currentUsers - id, IO.unit)
            } else {
              (currentUsers, IO.raiseError(UserNotFoundException(id, "delete")))
            }
          }.flatten
    }

  // --- Test Suites ---
  "UserRoutes" when {

    // --- POST /users ---
    "handling POST /users" should {
      "create a user and return 201 Created with the new user" in { // Use standard 'in'
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val createUserReq =
            CreateUserRequest(name = "Test User", email = "test@example.com")
          val expectedUser =
            User(id = 1L, name = "Test User", email = "test@example.com")
          val responseIO =
            userRoutes.run(requestWithBody(Method.POST, uri"/", createUserReq))
          check(responseIO, Status.Created, Some(expectedUser))
        }
      }

      "return 400 Bad Request for invalid JSON syntax" in { // Test syntax error
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val invalidJsonString = "{ \"name\": " // Missing closing quote
          val responseIO = userRoutes.run(
            request(Method.POST, uri"/").withEntity(invalidJsonString)
          )
          checkStringBody(responseIO, Status.BadRequest, "Invalid JSON syntax")
        }
      }

      "return 400 Bad Request for invalid JSON structure" in { // Test structure error
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val invalidJsonString = "{ \"name\": 123 }" // Name should be string
          val responseIO = userRoutes.run(
            request(Method.POST, uri"/").withEntity(invalidJsonString)
          )
          checkStringBody(
            responseIO,
            Status.BadRequest,
            "Invalid JSON structure"
          )
        }
      }

      "return 500 Internal Server Error if repository create fails" in { // Test repo failure
        createMockUserRepository(failOn = Some("create")).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val createUserReq =
            CreateUserRequest(name = "Test User", email = "test@example.com")
          val responseIO =
            userRoutes.run(requestWithBody(Method.POST, uri"/", createUserReq))
          checkStringBody(
            responseIO,
            Status.InternalServerError,
            "Failed to create user: Mock create failed"
          )
        }
      }
    }

    // --- GET /users ---
    "handling GET /users" should {
      "return 200 OK with an empty list if no users exist" in {
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.GET, uri"/"))
          check(responseIO, Status.Ok, Some(List.empty[User]))
        }
      }

      "return 200 OK with a list of users" in {
        val user1 = User(1L, "User One", "one@example.com")
        val user2 = User(2L, "User Two", "two@example.com")
        createMockUserRepository(Map(1L -> user1, 2L -> user2)).flatMap {
          mockRepo =>
            val userRoutes = UserRoutes.routes(mockRepo).orNotFound
            val responseIO = userRoutes.run(request(Method.GET, uri"/"))
            check(responseIO, Status.Ok, Some(List(user1, user2)))
        }
      }

      "return 500 Internal Server Error if repository findAll fails" in {
        createMockUserRepository(failOn = Some("findAll")).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.GET, uri"/"))
          checkStringBody(
            responseIO,
            Status.InternalServerError,
            "Failed to retrieve users: Mock findAll failed"
          )
        }
      }
    }

    // --- GET /users/{id} ---
    "handling GET /users/{id}" should {
      "return 200 OK with the user if found" in {
        val user1 = User(1L, "User One", "one@example.com")
        createMockUserRepository(Map(1L -> user1)).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.GET, uri"/1"))
          check(responseIO, Status.Ok, Some(user1))
        }
      }

      "return 404 Not Found if user does not exist" in {
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.GET, uri"/99"))
          checkStringBody(
            responseIO,
            Status.NotFound,
            "User with ID 99 not found"
          )
        }
      }

      "return 500 Internal Server Error if repository findById fails" in {
        createMockUserRepository(failOn = Some("findById")).flatMap {
          mockRepo =>
            val userRoutes = UserRoutes.routes(mockRepo).orNotFound
            checkStringBody(
              userRoutes.run(request(Method.GET, uri"/1")),
              Status.InternalServerError,
              "Failed to retrieve user 1: Mock findById failed"
            )
        }
      }
    }

    // --- PUT /users/{id} ---
    "handling PUT /users/{id}" should {
      "update an existing user and return 200 OK with the updated user" in {
        val user1 = User(1L, "User One", "one@example.com")
        val updatedUser = User(1L, "User One Updated", "updated@example.com")
        createMockUserRepository(Map(1L -> user1)).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO =
            userRoutes.run(requestWithBody(Method.PUT, uri"/1", updatedUser))
          check(responseIO, Status.Ok, Some(updatedUser)) *> mockRepo
            .findById(1L)
            .asserting(_ shouldBe Some(updatedUser))
        }
      }

      "return 404 Not Found if user to update does not exist" in {
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val nonExistentUserUpdate =
            User(99L, "Non Existent", "none@example.com")
          val responseIO = userRoutes.run(
            requestWithBody(Method.PUT, uri"/99", nonExistentUserUpdate)
          )
          checkStringBody(
            responseIO,
            Status.NotFound,
            "User with ID 99 not found for update"
          )
        }
      }

      "return 400 Bad Request if ID in path does not match ID in body" in {
        val user1 = User(1L, "User One", "one@example.com")
        createMockUserRepository(Map(1L -> user1)).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val mismatchedUpdate = User(2L, "Mismatch", "mismatch@example.com")
          val responseIO = userRoutes.run(
            requestWithBody(Method.PUT, uri"/1", mismatchedUpdate)
          )
          checkStringBody(
            responseIO,
            Status.BadRequest,
            "ID in URL path (1) does not match ID in request body (2)"
          )
        }
      }

      "return 400 Bad Request for invalid JSON structure" in {
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val invalidJsonString =
            "{\"id\": 1, \"name\": 123 }" // name is wrong type
          userRoutes
            .run(request(Method.PUT, uri"/1").withEntity(invalidJsonString))
            .flatMap { response =>
              val statusCheck = IO(response.status shouldBe Status.BadRequest)
              val bodyCheck = response
                .as[String]
                .asserting(_ should include("Invalid JSON structure"))
              statusCheck *> bodyCheck.void
            }
        }
      }

      "return 500 Internal Server Error if repository update fails internally" in {
        val user1 = User(1L, "User One", "one@example.com")
        createMockUserRepository(Map(1L -> user1), failOn = Some("update"))
          .flatMap { mockRepo =>
            val userRoutes = UserRoutes.routes(mockRepo).orNotFound
            val updatedUser =
              User(1L, "User One Updated", "updated@example.com")
            val responseIO =
              userRoutes.run(requestWithBody(Method.PUT, uri"/1", updatedUser))
            checkStringBody(
              responseIO,
              Status.InternalServerError,
              "Failed to update user 1: Mock update failed"
            )
          }
      }
    }

    // --- DELETE /users/{id} ---
    "handling DELETE /users/{id}" should {
      "delete an existing user and return 204 No Content" in {
        val user1 = User(1L, "User One", "one@example.com")
        createMockUserRepository(Map(1L -> user1)).flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.DELETE, uri"/1"))
          check(responseIO, Status.NoContent, Option.empty[String]) *> mockRepo
            .findById(1L)
            .asserting(_ shouldBe None)
        }
      }

      "return 404 Not Found if user to delete does not exist" in {
        createMockUserRepository().flatMap { mockRepo =>
          val userRoutes = UserRoutes.routes(mockRepo).orNotFound
          val responseIO = userRoutes.run(request(Method.DELETE, uri"/99"))
          checkStringBody(
            responseIO,
            Status.NotFound,
            "User with ID 99 not found for deletion"
          )
        }
      }

      "return 500 Internal Server Error if repository delete fails internally" in {
        val user1 = User(1L, "User One", "one@example.com")
        createMockUserRepository(Map(1L -> user1), failOn = Some("delete"))
          .flatMap { mockRepo =>
            val userRoutes = UserRoutes.routes(mockRepo).orNotFound
            val responseIO = userRoutes.run(request(Method.DELETE, uri"/1"))
            checkStringBody(
              responseIO,
              Status.InternalServerError,
              "Failed to delete user 1: Mock delete failed"
            )
          }
      }
    }
  }
}
