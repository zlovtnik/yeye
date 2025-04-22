package com.yeye.backend.config

import zio.*
import javax.sql.DataSource
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.context.jdbc.JdbcContext
import io.getquill.jdbczio.Quill
import io.getquill.OracleDialect

object DatabaseConfig:
  val dataSourceLayer = Quill.DataSource.fromPrefix("oracle")

  object context extends OracleZioJdbcContext(SnakeCase)
