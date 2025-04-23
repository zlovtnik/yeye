package com.yeye.backend.config

import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import javax.sql.DataSource

/** Database configuration for the application.
  */
object DatabaseConfig:
  private val config = ConfigFactory.load()

  /** HikariCP data source configuration for Oracle database.
    */
  private val hikariConfig = new HikariConfig()
  hikariConfig.setJdbcUrl(config.getString("db.url"))
  hikariConfig.setUsername(config.getString("db.user"))
  hikariConfig.setPassword(config.getString("db.password"))
  hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver")
  hikariConfig.setMaximumPoolSize(10)
  hikariConfig.setMinimumIdle(5)

  /** HikariCP data source for Oracle database operations.
    */
  val dataSource: Resource[IO, DataSource] =
    Resource.make(IO(new HikariDataSource(hikariConfig)))(ds =>
      IO(ds.asInstanceOf[HikariDataSource].close())
    )
