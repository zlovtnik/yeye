package com.yeye.backend.routes

import cats.effect.{IO, Concurrent}
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.*
import io.circe.Json
import org.http4s.Method

object HealthRoutes {
  def routes[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] { case GET -> Root =>
      Ok(Json.obj("status" -> Json.fromString("ok")))
    }
  }
}
