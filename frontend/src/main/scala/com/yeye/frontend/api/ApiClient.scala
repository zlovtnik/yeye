package com.yeye.frontend.api

import com.yeye.frontend.types.{CreateUserRequest, User}
import io.circe.parser.decode
import io.circe.syntax.*
import org.scalajs.dom.ext.Ajax
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/** HTTP client for backend API communication
  *
  * This object provides methods to interact with the backend REST API. It
  * handles HTTP requests and response parsing for various user-related
  * operations.
  */
object ApiClient {

  /** Base URL for the backend API */
  private val baseUrl = "http://localhost:8080"

  /** Retrieves a list of all users from the API
    *
    * Performs a GET request to the /users endpoint and decodes the JSON
    * response into a list of User objects.
    *
    * @return
    *   Future containing a list of User objects or a failure
    */
  def getUsers: Future[List[User]] = {
    Ajax
      .get(s"$baseUrl/users")
      .map(_.responseText)
      .flatMap(text => Future.fromTry(decode[List[User]](text).toTry))
  }

  /** Creates a new user via the API
    *
    * Performs a POST request to the /users endpoint with the user data. The
    * request body is encoded as JSON from the CreateUserRequest object.
    *
    * @param request
    *   The CreateUserRequest containing user data
    * @return
    *   Future containing the created User object or a failure
    */
  def createUser(request: CreateUserRequest): Future[User] = {
    Ajax
      .post(
        url = s"$baseUrl/users",
        data = request.asJson.noSpaces,
        headers = Map("Content-Type" -> "application/json")
      )
      .map(_.responseText)
      .flatMap(text => Future.fromTry(decode[User](text).toTry))
  }

  /** Retrieves a single user by ID from the API
    *
    * Performs a GET request to the /users/{id} endpoint and decodes the JSON
    * response into a User object.
    *
    * @param id
    *   The ID of the user to retrieve
    * @return
    *   Future containing the User object or a failure
    */
  def getUser(id: String): Future[User] = {
    Ajax
      .get(s"$baseUrl/users/$id")
      .map(_.responseText)
      .flatMap(text => Future.fromTry(decode[User](text).toTry))
  }
}
