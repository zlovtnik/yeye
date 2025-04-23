package com.yeye.backend.routes

import cats.effect.IO
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder._ // Needed for Json decoding
import org.http4s.implicits.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.effect.testing.scalatest.AsyncIOSpec // For IO testing
import io.circe.Json // For JSON comparison

class HealthRoutesSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  // Helper to create a Request object for testing
  private def request(method: Method, uri: Uri): Request[IO] =
    Request[IO](method = method, uri = uri)

  // Helper to run the request against the routes and check the response
  private def check[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A]
  )(implicit ev: EntityDecoder[IO, A]): IO[Unit] =
    actual.flatMap { response =>
      val statusCheck =
        IO(response.status shouldBe expectedStatus) // Assert status code
      val bodyCheck = expectedBody match {
        case Some(expected) =>
          response
            .as[A]
            .asserting(_ shouldBe expected) // Assert body if expected
        case None =>
          response.body.compile.toVector.asserting(_ shouldBe empty)
      }
      statusCheck *> bodyCheck.void // Combine and map to IO[Unit]
    }

  // Helper to check response status and that body contains a substring
  private def checkStringBody(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBodySubstring: String
  ): IO[Unit] =
    actual.flatMap { response =>
      // Check status
      val statusCheck = IO(response.status shouldBe expectedStatus)
      // Check body contains substring by explicitly reading as text
      val bodyCheck = response.bodyText.compile.string
        .asserting(_ should include(expectedBodySubstring))
      statusCheck *> bodyCheck.void // Combine checks and map to IO[Unit]
    }

  "HealthRoutes" should { // Use `should` for test description
    "return 200 OK with status ok when GET / is requested (relative to the service)" in {
      val healthRoutes = HealthRoutes.routes[IO] // Get the routes directly
      val response = healthRoutes.orNotFound.run(
        request(
          Method.GET,
          uri"/"
        ) // Send a GET request to the Root of this specific route service
      )

      // Define the expected JSON body
      val expectedJson = Json.obj("status" -> Json.fromString("ok"))

      // Check the response status and body
      check(response, Status.Ok, Some(expectedJson))
    }

    "return 404 Not Found for other paths within its own service" in {
      val healthRoutes = HealthRoutes.routes[IO]
      val response = healthRoutes.orNotFound.run(
        request(
          Method.GET,
          uri"/other"
        ) // Send a GET request to a path not defined in HealthRoutes
      )

      // Use checkStringBody to assert the status and part of the body message
      checkStringBody(response, Status.NotFound, "Not found")
    }
  }
}
