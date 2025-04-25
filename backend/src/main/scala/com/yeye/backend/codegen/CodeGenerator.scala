package com.yeye.backend.codegen

import cats.effect.IO

/** Utility for generating Scala case classes and repository code from database
  * tables
  *
  * NOTE: This utility has been temporarily disabled due to compilation issues
  * with doobie. It will generate a placeholder message instead of actual code.
  */
object CodeGenerator {
  case class ColumnMetadata(
      name: String,
      dataType: String,
      nullable: String,
      isPrimaryKey: Boolean
  )

  // Generate all code for a table (currently disabled)
  def generateTableCode(
      tableName: String,
      schema: String,
      jdbcUrl: String,
      username: String,
      password: String
  ): IO[String] = {
    IO.pure(
      s"""
      |// CodeGenerator is temporarily disabled
      |// Would generate code for table $tableName in schema $schema
      |
      |package com.yeye.backend.model
      |
      |import cats.effect.IO
      |
      |// Placeholder for generated code
      |case class Placeholder(
      |  id: Long,
      |  name: String
      |)
      """.stripMargin
    )
  }
}
