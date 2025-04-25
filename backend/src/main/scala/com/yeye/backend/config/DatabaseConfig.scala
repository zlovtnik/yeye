package com.yeye.backend.config

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import cats.effect.IO
import cats.effect.Resource

/** Database configuration for the application.
  */
object DatabaseConfig:
  private val logger = LoggerFactory.getLogger(getClass)

  private val retryCount =
    sys.env.getOrElse("DB_CONNECTION_RETRY_COUNT", "30").toInt
  private val retryDelay =
    sys.env.getOrElse("DB_CONNECTION_RETRY_DELAY", "10").toInt.seconds

  /** HikariCP data source configuration for Oracle database.
    */
  private val hikariConfig = new HikariConfig()
  hikariConfig.setJdbcUrl(
    sys.env.getOrElse("DB_URL", "jdbc:oracle:thin:@//localhost:1521/FREE")
  )
  hikariConfig.setUsername(sys.env.getOrElse("DB_USER", "system"))
  hikariConfig.setPassword(sys.env.getOrElse("DB_PASSWORD", "ora"))
  hikariConfig.setMaximumPoolSize(10)
  hikariConfig.setMinimumIdle(2)
  hikariConfig.setIdleTimeout(300000)
  hikariConfig.setConnectionTimeout(30000)
  hikariConfig.setValidationTimeout(5000)
  hikariConfig.setMaxLifetime(1800000)
  hikariConfig.setLeakDetectionThreshold(30000)
  hikariConfig.setConnectionTestQuery("SELECT 1 FROM DUAL")

  /** HikariCP data source for Oracle database operations.
    */
  val dataSource: Resource[IO, HikariDataSource] = Resource.make(IO {
    val config = new HikariConfig()
    config.setJdbcUrl(
      sys.env.getOrElse("DB_URL", "jdbc:oracle:thin:@//localhost:1521/FREEPDB1")
    )
    config.setUsername(sys.env.getOrElse("DB_USER", "sh"))
    config.setPassword(sys.env.getOrElse("DB_PASSWORD", "sh"))
    config.setMinimumIdle(2)
    config.setMaximumPoolSize(10)
    config.setConnectionTimeout(30000)
    config.setIdleTimeout(300000)
    config.setMaxLifetime(1800000)
    config.setLeakDetectionThreshold(30000)
    config.setValidationTimeout(5000)
    config.setConnectionTestQuery("SELECT 1 FROM DUAL")
    new HikariDataSource(config)
  })(ds => IO(ds.close()))

  private def retryWithBackoff[T](maxRetries: Int, delay: Duration)(
      f: => T
  ): T = {
    def attempt(retries: Int): T = {
      Try(f) match {
        case Success(result) => result
        case Failure(e) if retries > 0 =>
          logger.warn(
            s"Database connection attempt failed (${maxRetries - retries + 1}/$maxRetries): ${e.getMessage}"
          )
          Thread.sleep(delay.toMillis)
          attempt(retries - 1)
        case Failure(e) =>
          logger.error(
            "Failed to establish database connection after all retries",
            e
          )
          throw e
      }
    }
    attempt(maxRetries)
  }
