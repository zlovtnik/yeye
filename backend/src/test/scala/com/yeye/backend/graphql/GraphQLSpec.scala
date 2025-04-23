package com.yeye.backend.graphql

import zio.*
import zio.test._
import zio.test.Assertion.*
import zio.http.*
import com.yeye.backend.repository.UserRepository
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import com.yeye.backend.Server
import com.yeye.shared.User
import io.circe.parser.parse
import io.circe.generic.auto.*

object GraphQLSpec extends ZIOSpecDefault:
  def spec = suite("GraphQL API")(
    test("query users") {
      for {
        response <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString("""
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
            """)
          )
        )
        body <- response.body.asString
        parsed <- ZIO.fromEither(
          parse(body).flatMap(_.as[Map[String, List[User]]])
        )
      } yield assert(parsed.get("users"))(isSome(isNonEmpty))
    },
    test("create user") {
      for {
        response <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString("""
              mutation {
                createUser(input: {
                  email: "test@example.com"
                  firstName: "Test"
                  lastName: "User"
                  status: "ACTIVE"
                }) {
                  id
                  email
                  firstName
                  lastName
                  status
                }
              }
            """)
          )
        )
        body <- response.body.asString
        parsed <- ZIO.fromEither(parse(body).flatMap(_.as[Map[String, User]]))
      } yield assert(parsed.get("createUser").map(_.email))(
        isSome(equalTo("test@example.com"))
      )
    },
    test("update user") {
      for {
        createResponse <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString("""
              mutation {
                createUser(input: {
                  email: "test@example.com"
                  firstName: "Test"
                  lastName: "User"
                  status: "ACTIVE"
                }) {
                  id
                  email
                  firstName
                  lastName
                  status
                }
              }
            """)
          )
        )
        createBody <- createResponse.body.asString
        createParsed <- ZIO.fromEither(
          parse(createBody).flatMap(_.as[Map[String, User]])
        )
        userId = createParsed.get("createUser").map(_.id).getOrElse("")
        updateResponse <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString(s"""
              mutation {
                updateUser(input: {
                  id: "$userId",
                  user: {
                    id: "$userId",
                    email: "updated@example.com"
                    firstName: "Updated"
                    lastName: "User"
                    status: "ACTIVE"
                    created: 0
                    lastUpdated: 0
                  }
                }) {
                  id
                  email
                  firstName
                  lastName
                  status
                }
              }
            """)
          )
        )
        updateBody <- updateResponse.body.asString
        updateParsed <- ZIO.fromEither(
          parse(updateBody).flatMap(_.as[Map[String, User]])
        )
      } yield assert(updateParsed.get("updateUser").map(_.email))(
        isSome(equalTo("updated@example.com"))
      )
    },
    test("delete user") {
      for {
        // First create a user
        createResponse <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString("""
              mutation {
                createUser(input: {
                  email: "delete@example.com"
                  firstName: "Delete"
                  lastName: "User"
                  status: "ACTIVE"
                }) {
                  id
                }
              }
            """)
          )
        )
        createBody <- createResponse.body.asString
        createParsed <- ZIO.fromEither(
          parse(createBody).flatMap(_.as[Map[String, User]])
        )
        userId = createParsed.get("createUser").map(_.id).getOrElse("")
        // Then delete the user
        deleteResponse <- Server.app.toHttpApp.runZIO(
          Request.post(
            URL.root / "graphql",
            Body.fromString(s"""
              mutation {
                deleteUser(id: "$userId")
              }
            """)
          )
        )
        deleteBody <- deleteResponse.body.asString
        deleteParsed <- ZIO.fromEither(
          parse(deleteBody).flatMap(_.as[Map[String, Boolean]])
        )
      } yield assert(deleteParsed.get("deleteUser"))(isSome(isTrue))
    }
  ).provideLayerShared(UserRepository.live ++ dataSourceLayer)
