package com.yeye.devserver

import zio.*
import zio.http.*
import java.io.File
import java.nio.file.Paths

object DevServer extends ZIOAppDefault:
  def log(level: String, message: String) =
    println(s"${java.time.LocalDateTime.now()} [$level] $message")

  def run =
    val app = Http.collectZIO[Request] {
      case req @ Method.GET -> Root / "index.html" =>
        for {
          _ <- ZIO.succeed(
            log("INFO", s"Received request for index.html: $req")
          )
          response <- ZIO
            .attempt {
              val content = scala.io.Source.fromResource("index.html").mkString
              log("INFO", s"Serving index.html with ${content.length} bytes")
              Response.html(content)
            }
            .catchAll { e =>
              log("ERROR", s"Error serving index.html: ${e.getMessage}")
              ZIO.succeed(
                Response(
                  Status.InternalServerError,
                  body = Body.fromString(s"Error: ${e.getMessage}")
                )
              )
            }
        } yield response

      case req @ Method.GET -> Root / "target" / "scala-3.3.1" / "yeye-frontend-opt" / "main.js" =>
        for {
          _ <- ZIO.succeed(log("INFO", s"Received request for main.js: $req"))
          response <- ZIO
            .attempt {
              val jsFile = Paths
                .get("frontend/target/scala-3.3.1/yeye-frontend-opt/main.js")
                .toAbsolutePath
              log("INFO", s"Looking for main.js at: $jsFile")
              if (jsFile.toFile.exists()) {
                val content = scala.io.Source.fromFile(jsFile.toFile).mkString
                log("INFO", s"Serving main.js with ${content.length} bytes")
                Response(
                  Status.Ok,
                  headers = Headers(
                    Header.ContentType(MediaType.text.javascript),
                    Header.AccessControlAllowOrigin.All,
                    Header.AccessControlAllowMethods.All,
                    Header.AccessControlAllowHeaders.All
                  ),
                  body = Body.fromString(content)
                ).addHeader(Header.ContentType(MediaType.text.javascript))
              } else {
                log("WARN", s"main.js not found at $jsFile")
                Response(
                  Status.NotFound,
                  body = Body.fromString("main.js not found")
                )
              }
            }
            .catchAll { e =>
              log("ERROR", s"Error serving main.js: ${e.getMessage}")
              ZIO.succeed(
                Response(
                  Status.InternalServerError,
                  body = Body.fromString(s"Error: ${e.getMessage}")
                )
              )
            }
        } yield response

      case req @ Method.GET -> Root / "target" / "scala-3.3.1" / "yeye-frontend-opt" / "main.js.map" =>
        for {
          _ <- ZIO.succeed(
            log("INFO", s"Received request for source map: $req")
          )
          response <- ZIO
            .attempt {
              val mapFile = Paths
                .get(
                  "frontend/target/scala-3.3.1/yeye-frontend-opt/main.js.map"
                )
                .toAbsolutePath
              log("INFO", s"Looking for source map at: $mapFile")
              if (mapFile.toFile.exists()) {
                val content = scala.io.Source.fromFile(mapFile.toFile).mkString
                Response(
                  Status.Ok,
                  headers = Headers(
                    Header.ContentType(MediaType.application.json),
                    Header.AccessControlAllowOrigin.All,
                    Header.AccessControlAllowMethods.All,
                    Header.AccessControlAllowHeaders.All
                  ),
                  body = Body.fromString(content)
                ).addHeader(Header.ContentType(MediaType.application.json))
              } else {
                log("WARN", s"Source map not found at $mapFile")
                Response(Status.NotFound)
              }
            }
            .catchAll { e =>
              log("ERROR", s"Error serving source map: ${e.getMessage}")
              ZIO.succeed(Response(Status.InternalServerError))
            }
        } yield response

      case req @ Method.GET -> Root / "favicon.ico" =>
        ZIO.succeed(
          Response(
            Status.NotFound,
            body = Body.fromString("Not found")
          )
        )

      case req @ Method.OPTIONS -> _ =>
        ZIO.succeed(
          Response.ok
            .addHeader(Header.AccessControlAllowOrigin.All)
            .addHeader(Header.AccessControlAllowMethods.All)
            .addHeader(Header.AccessControlAllowHeaders.All)
        )

      case req =>
        ZIO.succeed(log("WARN", s"Received unknown request: $req")) *>
          ZIO.succeed(
            Response(Status.NotFound, body = Body.fromString("Not found"))
          )
    }

    ZIO.succeed(log("INFO", "Starting development server...")) *>
      Server
        .serve(app)
        .provide(
          Server.defaultWithPort(8080)
        )
