package com.yeye.backend.config

import zio.*
import javax.sql.DataSource
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.context.jdbc.JdbcContext
import io.getquill.jdbczio.Quill
import io.getquill.OracleDialect

/** Database configuration and setup for the application.
  *
  * This object provides:
  *   - DataSource layer configuration
  *   - Quill context for Oracle database
  */
object DatabaseConfig:
  /** DataSource layer configured from the "oracle" prefix in application.conf.
    * Provides database connection pooling and management.
    */
  val dataSourceLayer = Quill.DataSource.fromPrefix("oracle")

  /** Quill context for Oracle database operations. Uses snake_case naming
    * convention for database columns.
    */
  object context extends OracleZioJdbcContext(SnakeCase)
