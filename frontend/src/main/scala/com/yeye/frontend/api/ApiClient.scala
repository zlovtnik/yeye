package com.yeye.frontend.api

import com.yeye.frontend.types.{CreateUserRequest, User}
import io.circe.parser.decode
import io.circe.syntax.*
import org.scalajs.dom.ext.Ajax
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ApiClient {
  private val baseUrl = "http://localhost:8080"

  def getUsers: Future[List[User]] = {
    Ajax
      .get(s"$baseUrl/users")
      .map(_.responseText)
      .flatMap(text => Future.fromTry(decode[List[User]](text).toTry))
  }

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

  def getUser(id: String): Future[User] = {
    Ajax
      .get(s"$baseUrl/users/$id")
      .map(_.responseText)
      .flatMap(text => Future.fromTry(decode[User](text).toTry))
  }
}
