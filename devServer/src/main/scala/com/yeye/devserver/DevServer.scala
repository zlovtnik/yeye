package com.yeye.devserver

import cats.effect.{IO, IOApp, ExitCode}
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.implicits.*
import com.comcast.ip4s.*
import org.http4s.StaticFile
import java.io.File
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`

object DevServer extends IOApp {
  private val dsl = new Http4sDsl[IO] {}
  import dsl.*

  private val jsOutputDir = new File(
    "../frontend/target/scala-3.3.1/yeye-frontend-fastopt/"
  )

  private val jsFileService = HttpRoutes.of[IO] {
    case req @ GET -> Root / fileName
        if fileName.endsWith(".js") || fileName.endsWith(".js.map") =>
      StaticFile
        .fromFile(new File(jsOutputDir, fileName), Some(req))
        .getOrElseF(NotFound())
  }

  private val routes = HttpRoutes.of[IO] {
    case req @ GET -> Root =>
      StaticFile
        .fromResource("/index.html", Some(req))
        .map(_.putHeaders(`Content-Type`(MediaType.text.html)))
        .getOrElseF(NotFound())
    case req @ GET -> Root / fileName
        if fileName.endsWith(".js") || fileName.endsWith(".js.map") =>
      jsFileService(req).getOrElseF(NotFound())
    case GET -> Root / "favicon.ico" =>
      NoContent()
  }

  def run(args: List[String]): IO[ExitCode] = {
    val finalHttpApp = Router(
      "/" -> routes
    ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(finalHttpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
