package com.yeye.frontend.services

import com.yeye.frontend.api.ApiClient
import com.yeye.frontend.types.{CreateUserRequest, User}
import scala.concurrent.Future

object ApiService {
  def getUsers: Future[List[User]] = ApiClient.getUsers

  def createUser(name: String, email: String, age: Int): Future[User] = {
    val request = CreateUserRequest(
      name = name,
      email = email,
      age = age
    )
    ApiClient.createUser(request)
  }

  def getUser(id: String): Future[User] = ApiClient.getUser(id)
}
