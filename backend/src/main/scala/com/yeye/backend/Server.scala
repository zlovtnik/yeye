package com.yeye.backend

import cats.effect.{IO, IOApp, ExitCode}
import cats.syntax.all.*
import com.yeye.backend.config.DatabaseConfig
import com.yeye.backend.routes.{HealthRoutes, UserRoutes}
import com.yeye.backend.repository.UserRepository
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.implicits.*
import com.comcast.ip4s.*
import org.flywaydb.core.Flyway

/** Main server application that handles HTTP requests and serves the
  * application.
  *
  * This server provides:
  *   - User CRUD API endpoints
  *   - Health check endpoint
  *   - Database connection management
  */
object Server extends IOApp {
  def runMigrations(dataSource: javax.sql.DataSource): IO[Unit] = IO {
    val flyway = Flyway
      .configure()
      .dataSource(dataSource)
      .locations("db/migration")
      .baselineOnMigrate(true)
      .load()
    flyway.migrate()
  }

  def run(args: List[String]): IO[ExitCode] = {
    DatabaseConfig.dataSource.use { dataSource =>
      for {
        _ <- runMigrations(dataSource)
        userRepo = UserRepository.live(dataSource)
        httpApp = Router(
          "/health" -> HealthRoutes.routes[IO],
          "/users" -> UserRoutes.routes(userRepo)
        ).orNotFound
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(httpApp)
          .build
          .use(_ => IO.never)
      } yield ExitCode.Success
    }
  }
}
