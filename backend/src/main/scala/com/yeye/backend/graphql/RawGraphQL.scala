package com.yeye.backend.graphql

import zio.*
import zio.json.*
import com.yeye.shared.User
import com.yeye.backend.repository.UserRepository
import javax.sql.DataSource
import java.lang.System

/** Raw GraphQL implementation that handles GraphQL requests directly.
  */
object RawGraphQL {
  case class GraphQLRequest(
      query: String,
      variables: Option[Map[String, String]]
  )
  case class GraphQLResponse(
      data: Option[Map[String, String]],
      errors: Option[List[String]]
  )

  given JsonDecoder[GraphQLRequest] = DeriveJsonDecoder.gen[GraphQLRequest]
  given JsonEncoder[GraphQLResponse] = DeriveJsonEncoder.gen[GraphQLResponse]

  /** Handles a GraphQL request by parsing the query and executing it.
    */
  def handleRequest(
      request: GraphQLRequest
  ): ZIO[UserRepository & DataSource, Throwable, GraphQLResponse] = {
    for {
      repo <- ZIO.service[UserRepository]
      result <- request.query match {
        case q if q.contains("users") =>
          repo.getUsers.map(users =>
            GraphQLResponse(
              Some(Map("users" -> users.map(_.toJson).mkString("[", ",", "]"))),
              None
            )
          )
        case q if q.contains("userById") =>
          val id = extractId(q)
          repo
            .getUserById(id)
            .map {
              case Some(user) =>
                GraphQLResponse(Some(Map("userById" -> user.toJson)), None)
              case None =>
                GraphQLResponse(None, Some(List(s"User with id $id not found")))
            }
        case q if q.contains("createUser") =>
          val input = extractCreateUserInput(q)
          val user = User(
            id = java.util.UUID.randomUUID().toString,
            email = input("email"),
            firstName = input("firstName"),
            lastName = input("lastName"),
            status = input("status"),
            created = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
          )
          repo
            .createUser(user)
            .map(user =>
              GraphQLResponse(Some(Map("createUser" -> user.toJson)), None)
            )
        case q if q.contains("updateUser") =>
          val (id, updates) = extractUpdateUserInput(q)
          repo.getUserById(id).flatMap {
            case Some(existing) =>
              val updated = existing.copy(
                email = updates.getOrElse("email", existing.email),
                firstName = updates.getOrElse("firstName", existing.firstName),
                lastName = updates.getOrElse("lastName", existing.lastName),
                status = updates.getOrElse("status", existing.status),
                lastUpdated = System.currentTimeMillis()
              )
              repo
                .updateUser(id, updated)
                .map {
                  case Some(user) =>
                    GraphQLResponse(
                      Some(Map("updateUser" -> user.toJson)),
                      None
                    )
                  case None =>
                    GraphQLResponse(
                      None,
                      Some(List(s"Failed to update user with id $id"))
                    )
                }
            case None =>
              ZIO.succeed(
                GraphQLResponse(None, Some(List(s"User with id $id not found")))
              )
          }
        case q if q.contains("deleteUser") =>
          val id = extractId(q)
          repo
            .deleteUser(id)
            .map(success =>
              GraphQLResponse(Some(Map("deleteUser" -> success.toString)), None)
            )
        case _ =>
          ZIO.succeed(GraphQLResponse(None, Some(List("Invalid query"))))
      }
    } yield result
  }

  private def extractId(query: String): String = {
    // Simple extraction - in a real implementation, you'd want to use a proper GraphQL parser
    val pattern = """"id"\s*:\s*"([^"]+)"""".r
    pattern.findFirstMatchIn(query).map(_.group(1)).getOrElse("")
  }

  private def extractCreateUserInput(query: String): Map[String, String] = {
    // Simple extraction - in a real implementation, you'd want to use a proper GraphQL parser
    val pattern = """"input"\s*:\s*\{([^}]+)\}""".r
    val input = pattern.findFirstMatchIn(query).map(_.group(1)).getOrElse("")
    input
      .split(",")
      .map { pair =>
        val Array(key, value) = pair.split(":").map(_.trim)
        key
          .stripPrefix("\"")
          .stripSuffix("\"") -> value.stripPrefix("\"").stripSuffix("\"")
      }
      .toMap
  }

  private def extractUpdateUserInput(
      query: String
  ): (String, Map[String, String]) = {
    // Simple extraction - in a real implementation, you'd want to use a proper GraphQL parser
    val pattern = """"input"\s*:\s*\{([^}]+)\}""".r
    val input = pattern.findFirstMatchIn(query).map(_.group(1)).getOrElse("")
    val pairs = input
      .split(",")
      .map { pair =>
        val Array(key, value) = pair.split(":").map(_.trim)
        key
          .stripPrefix("\"")
          .stripSuffix("\"") -> value.stripPrefix("\"").stripSuffix("\"")
      }
      .toMap
    (pairs("id"), pairs - "id")
  }
}
