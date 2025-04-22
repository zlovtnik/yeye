package com.yeye.backend.http

import zio.http.*
import zio.*
import com.yeye.backend.graphql.Schema
import com.yeye.backend.repository.UserRepository
import javax.sql.DataSource

object GraphQLServer:
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
