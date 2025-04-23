package com.yeye.backend

import zio.*
import zio.http.*
import zio.http.Middleware.CorsConfig
import zio.http.Middleware.cors
import zio.json.*
import com.yeye.shared.User
import com.yeye.backend.repository.UserRepository
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import com.yeye.backend.graphql.Schema
import caliban.{GraphQLRequest, GraphQLResponse}
import io.getquill.*
import javax.sql.DataSource
import java.io.File
import java.nio.file.Paths

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

  /** CORS configuration */
  private val allowedOrigins = Set(
    "http://localhost:3000", // Frontend development server
    "http://localhost:8080", // Backend server
    "http://localhost" // Localhost without port
  )

  private val corsConfig = CorsConfig(
    allowedOrigin = origin => {
      val originStr = origin.toString
      if (allowedOrigins.contains(originStr)) {
        Some(Header.AccessControlAllowOrigin.Specific(origin))
      } else {
        None
      }
    },
    allowedMethods =
      Header.AccessControlAllowMethods(Method.GET, Method.POST, Method.OPTIONS),
    allowedHeaders = Header.AccessControlAllowHeaders("Content-Type", "Accept"),
    exposedHeaders = Header.AccessControlExposeHeaders.All,
    maxAge = Some(Header.AccessControlMaxAge(3.seconds))
  )

  /** Handler for serving static files from the frontend distribution directory.
    * Returns 404 if the file is not found.
    */
  val staticHandler
      : Handler[DataSource & UserRepository, Response, Any, Response] =
    Handler
      .fromFile(Paths.get("frontend/dist").toAbsolutePath.toFile)
      .mapError(_ => Response.status(Status.NotFound))
      .map(response => {
        val path =
          response.headers.get(Header.Location).map(_.toString).getOrElse("")
        val contentType = path match {
          case p if p.endsWith(".js")   => MediaType.application.javascript
          case p if p.endsWith(".html") => MediaType.text.html
          case p if p.endsWith(".css")  => MediaType.text.css
          case p if p.endsWith(".ico")  => MediaType("image", "x-icon")
          case p if p.endsWith(".png")  => MediaType.image.png
          case p if p.endsWith(".jpg") || p.endsWith(".jpeg") =>
            MediaType.image.jpeg
          case p if p.endsWith(".gif") => MediaType.image.gif
          case p if p.endsWith(".svg") => MediaType("image", "svg+xml")
          case _                       => MediaType.text.plain
        }
        response.addHeader(Header.ContentType(contentType))
      })

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
    Method.GET / "main.js.map" -> staticHandler,
    Method.GET / "favicon.ico" -> staticHandler,
    Method.GET / "index.html" -> staticHandler,
    Method.GET / "" -> staticHandler,
    Method.GET / "assets" / "**" -> staticHandler
  )

  /** GraphQL request handler that processes raw GraphQL requests.
    */
  val graphQLHandler = Handler.fromFunctionZIO { (request: Request) =>
    (for
      body <- request.body.asString
      request <- ZIO
        .fromEither(body.fromJson[GraphQLRequest])
        .mapError(err => new RuntimeException(s"Invalid GraphQL request: $err"))
      interpreter <- Schema.api.interpreter
      result <- interpreter.execute(
        request.query.getOrElse(""),
        request.operationName,
        request.variables.getOrElse(Map.empty[String, caliban.InputValue])
      )
      responseJson = result.data.toString
      _ <- ZIO.debug(s"GraphQL response: $responseJson")
    yield Response.json(responseJson))
      .catchAll { error =>
        ZIO.debug(s"GraphQL error: ${error.getMessage}") *>
          ZIO.succeed(
            Response
              .json(s"""{"errors":[{"message":"${error.getMessage
                  .replace("\"", "\\\"")}"}]}""")
              .status(Status.BadRequest)
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
    Method.POST / "api" / "graphql" -> graphQLHandler,
    // Static file routes - exact file matches
    Method.GET / "main.js" -> staticHandler,
    Method.GET / "main.js.map" -> staticHandler,
    Method.GET / "favicon.ico" -> staticHandler,
    Method.GET / "index.html" -> staticHandler,
    // Root path redirects to index.html
    Method.GET / "" -> staticHandler,
    // Assets path for other static files
    Method.GET / "assets" / "**" -> staticHandler
  )

  /** Main application entry point that:
    *   1. Starts the HTTP server on port 8080 2. Provides the UserRepository
    *      and database layer 3. Serves the combined application routes
    */
  def run =
    zio.http.Server
      .serve(app.toHttpApp @@ cors(corsConfig))
      .provide(
        zio.http.Server.defaultWithPort(8080),
        UserRepository.live,
        dataSourceLayer
      )
