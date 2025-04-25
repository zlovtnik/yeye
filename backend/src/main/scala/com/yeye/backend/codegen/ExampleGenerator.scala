package com.yeye.backend.codegen

import cats.effect.{IO, Resource, Sync}
import java.sql.{Connection, DriverManager}

/** A simplified code generator that uses direct JDBC instead of doobie for
  * better compatibility with Scala 3
  */
object ExampleGenerator {

  case class Column(name: String, dataType: String, nullable: Boolean)

  // Connect to the database using JDBC directly
  def getConnection(
      jdbcUrl: String,
      username: String,
      password: String
  ): Resource[IO, Connection] = {
    val acquire = IO.blocking(
      DriverManager.getConnection(jdbcUrl, username, password)
    )

    Resource.make(acquire)(conn => IO.blocking(conn.close()))
  }

  // Get the schema of a table
  def getTableColumns(
      conn: Connection,
      tableName: String,
      schema: String
  ): IO[List[Column]] =
    IO.blocking {
      val stmt = conn.prepareStatement(
        s"""SELECT 
           |  column_name, 
           |  data_type, 
           |  DECODE(nullable, 'Y', 1, 0) as is_nullable
           |FROM all_tab_columns
           |WHERE table_name = ?
           |  AND owner = ?
           |ORDER BY column_id
           |""".stripMargin
      )

      try {
        stmt.setString(1, tableName.toUpperCase)
        stmt.setString(2, schema.toUpperCase)

        val rs = stmt.executeQuery()
        val columns = scala.collection.mutable.ListBuffer.empty[Column]

        while (rs.next()) {
          columns += Column(
            name = rs.getString("column_name").toLowerCase,
            dataType = rs.getString("data_type"),
            nullable = rs.getInt("is_nullable") == 1
          )
        }

        columns.toList
      } finally {
        stmt.close()
      }
    }

  // Convert SQL type to Scala type
  def sqlTypeToScala(sqlType: String, nullable: Boolean): String = {
    val baseType = sqlType.toUpperCase match {
      case t if t.contains("VARCHAR") || t.contains("CHAR") => "String"
      case "NUMBER"                                         => "BigDecimal"
      case "INTEGER" | "INT"                                => "Int"
      case "SMALLINT"                                       => "Short"
      case "DATE"      => "java.time.LocalDate"
      case "TIMESTAMP" => "java.time.LocalDateTime"
      case "BLOB"      => "Array[Byte]"
      case "CLOB"      => "String"
      case "BOOLEAN"   => "Boolean"
      case _           => "String" // Default fallback
    }

    if (nullable) s"Option[$baseType]" else baseType
  }

  // Generate a case class for a table
  def generateCaseClass(tableName: String, columns: List[Column]): String = {
    val className = tableName.split("_").map(_.capitalize).mkString

    val fields = columns
      .map { col =>
        val scalaType = sqlTypeToScala(col.dataType, col.nullable)
        s"  ${col.name.toLowerCase}: $scalaType"
      }
      .mkString(",\n")

    s"""
    |case class $className(
    |$fields
    |)
    """.stripMargin
  }

  // Generate model code for a table
  def generateModelCode(
      tableName: String,
      schema: String,
      jdbcUrl: String,
      username: String,
      password: String
  ): IO[String] = {
    getConnection(jdbcUrl, username, password).use { conn =>
      for {
        columns <- getTableColumns(conn, tableName, schema)
        caseClass = generateCaseClass(tableName, columns)
      } yield s"""
        |package com.yeye.backend.model
        |
        |$caseClass
        """.stripMargin
    }
  }

  // Main function to generate code for a table and print to console
  def generateAndPrint(
      tableName: String,
      schema: String = "HR",
      jdbcUrl: String = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
      username: String = "hr",
      password: String = "hr"
  ): IO[Unit] = {
    generateModelCode(tableName, schema, jdbcUrl, username, password).flatMap {
      code =>
        IO.println(s"Generated code for $tableName:\n\n$code")
    }
  }
}
