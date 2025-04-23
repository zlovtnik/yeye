package com.yeye.backend.http

import cats.effect.IO
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object HealthCheck {
  private val logger = Slf4jLogger.getLogger[IO]

  def routes: HttpRoutes[IO] = {
    HttpRoutes.of[IO] { case GET -> Root / "internal" / "health" =>
      for {
        _ <- logger.debug("Health check requested")
        response <- Ok("OK")
      } yield response
    }
  }
}
