package com.yeye.devserver

import zio.*
import zio.http.*
import java.io.File
import java.nio.file.Paths

object DevServer extends ZIOAppDefault:
  def log(level: String, message: String) =
    println(s"${java.time.LocalDateTime.now()} [$level] $message")

  def run =
    val app = Routes(
      Method.GET / "index.html" -> handler(serveIndexHtml),
      Method.GET / "main.js" -> handler(serveMainJs),
      Method.GET / "main.js.map" -> handler(serveSourceMap),
      Method.GET / "favicon.ico" -> handler(_ =>
        ZIO.succeed(Response.status(Status.NotFound))
      ),
      Method.OPTIONS / "" -> handler(_ => ZIO.succeed(Response.ok))
    ).toHttpApp

    def serveIndexHtml(req: Request) =
      ZIO.succeed(log("INFO", "Received request for index.html")) *>
        ZIO
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

    def serveMainJs(req: Request) =
      ZIO.succeed(log("INFO", "Received request for main.js")) *>
        ZIO
          .attempt {
            val jsFile = Paths.get("frontend/dist/main.js").toAbsolutePath
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
              )
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

    def serveSourceMap(req: Request) =
      ZIO.succeed(log("INFO", "Received request for source map")) *>
        ZIO
          .attempt {
            val mapFile = Paths.get("frontend/dist/main.js.map").toAbsolutePath
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
              )
            } else {
              log("WARN", s"Source map not found at $mapFile")
              Response(Status.NotFound)
            }
          }
          .catchAll { e =>
            log("ERROR", s"Error serving source map: ${e.getMessage}")
            ZIO.succeed(Response(Status.InternalServerError))
          }

    ZIO.succeed(log("INFO", "Starting development server...")) *>
      Server
        .serve(app)
        .provide(
          Server.defaultWithPort(8090)
        )
