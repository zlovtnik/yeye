package com.yeye.backend.http

import zio.http.*
import zio.*
import com.yeye.backend.graphql.Schema
import com.yeye.backend.repository.UserRepository
import javax.sql.DataSource

/** GraphQL server implementation that handles GraphQL requests.
  *
  * This server:
  *   - Exposes a POST endpoint at /api/graphql
  *   - Processes GraphQL queries and mutations
  *   - Returns JSON responses
  *   - Handles errors with appropriate status codes
  */
object GraphQLServer:
  /** GraphQL application routes that:
    *   1. Accepts POST requests to /api/graphql 2. Extracts the request body 3.
    *      Creates a GraphQL interpreter 4. Executes the query/mutation 5.
    *      Returns the result as JSON
    *
    * @return
    *   Routes configured for GraphQL operations
    */
  val graphQLApp: Routes[UserRepository & DataSource, Response] = Routes(
    Method.POST / "api" / "graphql" -> handler { (request: Request) =>
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
  )
