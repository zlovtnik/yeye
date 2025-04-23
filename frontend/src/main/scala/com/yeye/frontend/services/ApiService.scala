package com.yeye.frontend.services

import com.yeye.frontend.types.File
import com.yeye.shared.User
import zio.json.*
import org.scalajs.dom
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import org.scalajs.dom.HttpMethod
import scalajs.js.DynamicImplicits.truthValue

object ApiService:
  /** Base URL for the backend API */
  val apiBaseUrl = "http://localhost:8080"

  /** Query to fetch all users from the backend */
  val usersQuery = """
    query {
      users {
        id
        email
        firstName
        lastName
        status
        created
        lastUpdated
      }
    }
  """

  /** Query to fetch all files from the backend */
  val filesQuery = """
    query {
      files {
        id
        name
        size
        fileType
        lastModified
      }
    }
  """

  /** Mutation to create a new user */
  val createUserMutation = """
    mutation CreateUser($email: String!, $firstName: String!, $lastName: String!, $status: String!) {
      createUser(input: { email: $email, firstName: $firstName, lastName: $lastName, status: $status }) {
        id
        email
        firstName
        lastName
        status
        created
        lastUpdated
      }
    }
  """

  /** Mutation to update an existing user */
  val updateUserMutation = """
    mutation UpdateUser($id: String!, $email: String!, $firstName: String!, $lastName: String!, $status: String!) {
      updateUser(input: { id: $id, user: { id: $id, email: $email, firstName: $firstName, lastName: $lastName, status: $status, created: 0, lastUpdated: 0 } }) {
        id
        email
        firstName
        lastName
        status
        created
        lastUpdated
      }
    }
  """

  /** Mutation to delete a user */
  val deleteUserMutation = """
    mutation DeleteUser($id: String!) {
      deleteUser(id: $id)
    }
  """

  /** Executes a GraphQL query against the backend API */
  def executeGraphQL(
      query: String,
      variables: String = "{}"
  ): Future[String] =
    dom
      .fetch(
        s"$apiBaseUrl/api/graphql",
        new dom.RequestInit {
          method = HttpMethod.POST
          headers = js.Dictionary(
            "Content-Type" -> "application/json",
            "Accept" -> "application/json"
          )
          body = s"""{"query": ${query.toJson}, "variables": $variables}"""
        }
      )
      .flatMap { response =>
        if (!response.ok) {
          println(s"HTTP error: ${response.status} ${response.statusText}")
          response
            .text()
            .flatMap(text =>
              Future.failed(
                new Exception(
                  s"HTTP error: ${response.status} ${response.statusText}\nResponse: $text"
                )
              )
            )
        } else {
          response.text()
        }
      }
      .flatMap { responseText =>
        println(s"GraphQL response: $responseText")
        val json = js.JSON.parse(responseText)
        if (json.hasOwnProperty("errors")) {
          val errors = json.errors.asInstanceOf[js.Array[js.Dynamic]]
          val errorMessages =
            errors.map(_.message.asInstanceOf[String]).mkString(", ")
          Future.failed(new Exception(s"GraphQL errors: $errorMessages"))
        } else {
          Future.successful(responseText)
        }
      }
