package com.yeye.backend

import zio.*
import zio.http.*
import zio.json.*
import com.yeye.shared.User
import com.yeye.backend.repository.UserRepository
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import com.yeye.backend.http.GraphQLServer
import com.yeye.backend.graphql.Schema
import io.getquill.*
import javax.sql.DataSource
import java.io.File

object Server extends ZIOAppDefault:
  given JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  // Static file handler
  val staticHandler
      : Handler[DataSource & UserRepository, Response, Any, Response] =
    Handler
      .fromFile(File("frontend/dist"))
      .mapError(_ => Response.status(Status.NotFound))

  // Health check handler with database verification
  private val healthCheckHandler = Handler.fromFunctionZIO { (req: Request) =>
    for
      // Verify database connection
      dbStatus <- ZIO.serviceWithZIO[DataSource] { ds =>
        ZIO
          .attemptBlocking {
            val conn = ds.getConnection
            try {
              conn.isValid(5) // 5 second timeout
            } finally {
              conn.close()
            }
          }
          .catchAll(_ => ZIO.succeed(false))
      }
      response <-
        if dbStatus then ZIO.succeed(Response.ok)
        else ZIO.succeed(Response.status(Status.ServiceUnavailable))
    yield response
  }

  // Static file routes with proper fallback
  val staticRoutes = Routes(
    Method.GET / "main.js" -> staticHandler,
    Method.GET / "favicon.ico" -> staticHandler,
    Method.GET / "index.html" -> staticHandler,
    Method.GET / "" -> staticHandler,
    Method.GET / "assets" / "**" -> staticHandler
  )

  // GraphQL handler
  val graphQLHandler = Handler.fromFunctionZIO { (request: Request) =>
    (for
      body <- request.body.asString
      interpreter <- Schema.api.interpreter
      result <- interpreter.execute(body)
    yield Response.json(result.toString)).catchAll { error =>
      ZIO.succeed(
        Response.text(error.getMessage).status(Status.InternalServerError)
      )
    }
  }

  val app = Routes(
    // Health check endpoint
    Method.GET / "internal" / "health" -> healthCheckHandler,
    // GraphQL route
    Method.POST / "api" / "graphql" -> graphQLHandler
  ) ++ staticRoutes

  def run =
    zio.http.Server
      .serve(app.toHttpApp)
      .provide(
        zio.http.Server.defaultWithPort(8081),
        UserRepository.live,
        dataSourceLayer
      )
