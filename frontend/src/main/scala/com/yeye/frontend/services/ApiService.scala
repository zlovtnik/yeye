package com.yeye.frontend.services

import com.yeye.frontend.api.ApiClient
import com.yeye.frontend.types.{CreateUserRequest, User}
import scala.concurrent.Future

/** Service layer for API operations
  *
  * This object provides a higher-level service API for interacting with the
  * backend, abstracting away the details of the API client implementation. It
  * serves as a facade for the presentation layer to interact with backend
  * services.
  */
object ApiService {

  /** Retrieves all users from the backend
    *
    * @return
    *   Future containing a list of User objects
    */
  def getUsers: Future[List[User]] = ApiClient.getUsers

  /** Creates a new user with the provided information
    *
    * @param name
    *   The user's full name
    * @param email
    *   The user's email address
    * @param age
    *   The user's age
    * @return
    *   Future containing the created User object
    */
  def createUser(name: String, email: String, age: Int): Future[User] = {
    val request = CreateUserRequest(
      name = name,
      email = email,
      age = age
    )
    ApiClient.createUser(request)
  }

  /** Retrieves a specific user by ID
    *
    * @param id
    *   The ID of the user to retrieve
    * @return
    *   Future containing the User object
    */
  def getUser(id: String): Future[User] = ApiClient.getUser(id)
}
