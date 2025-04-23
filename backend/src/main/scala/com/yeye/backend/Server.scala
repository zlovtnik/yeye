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

/** Main server application that handles HTTP requests and serves the
  * application.
  *
  * This server provides:
  *   - GraphQL API endpoints
  *   - Static file serving
  *   - Health check endpoint
  *   - Database connection management
  */
object Server extends ZIOAppDefault:
  /** JSON encoder for User type */
  given JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  /** JSON decoder for User type */
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  /** Handler for serving static files from the frontend distribution directory.
    * Returns 404 if the file is not found.
    */
  val staticHandler
      : Handler[DataSource & UserRepository, Response, Any, Response] =
    Handler
      .fromFile(File("frontend/dist"))
      .mapError(_ => Response.status(Status.NotFound))

  /** Health check handler that verifies the database connection. Returns 200 OK
    * if the database is accessible, 503 Service Unavailable otherwise.
    */
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

  /** Routes for serving static files including:
    *   - JavaScript bundles
    *   - Favicon
    *   - HTML files
    *   - Assets
    */
  val staticRoutes = Routes(
    Method.GET / "main.js" -> staticHandler,
    Method.GET / "favicon.ico" -> staticHandler,
    Method.GET / "index.html" -> staticHandler,
    Method.GET / "" -> staticHandler,
    Method.GET / "assets" / "**" -> staticHandler
  )

  /** GraphQL request handler that:
    *   1. Extracts the request body 2. Creates a GraphQL interpreter 3.
    *      Executes the query 4. Returns the result as JSON Returns 500 Internal
    *      Server Error if execution fails
    */
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

  /** Combined application routes including:
    *   - Health check endpoint
    *   - GraphQL API endpoint
    *   - Static file routes
    */
  val app = Routes(
    // Health check endpoint
    Method.GET / "internal" / "health" -> healthCheckHandler,
    // GraphQL route
    Method.POST / "api" / "graphql" -> graphQLHandler
  ) ++ staticRoutes

  /** Main application entry point that:
    *   1. Starts the HTTP server on port 8081 2. Provides the UserRepository
    *      and database layer 3. Serves the combined application routes
    */
  def run =
    zio.http.Server
      .serve(app.toHttpApp)
      .provide(
        zio.http.Server.defaultWithPort(8081),
        UserRepository.live,
        dataSourceLayer
      )
