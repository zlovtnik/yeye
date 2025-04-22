package com.yeye.devserver

import zio.*
import zio.http.*

object DevServer extends ZIOAppDefault:
  def run =
    val app = Http.collect[Request] {
      case Method.GET -> Root / "index.html" =>
        Response.html(scala.io.Source.fromResource("index.html").mkString)
      case Method.GET -> Root / "main.js" =>
        Response
          .text(
            scala.io.Source
              .fromFile(
                "frontend/target/scala-3.3.1/yeye-frontend-fastopt/main.js"
              )
              .mkString
          )
          .addHeader(Header.ContentType(MediaType.application.javascript))
    }

    Server
      .serve(app)
      .provide(
        Server.default
      )
