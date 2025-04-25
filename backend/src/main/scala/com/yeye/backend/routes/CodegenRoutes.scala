package com.yeye.backend.routes

import cats.effect.IO
import cats.syntax.all
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import com.yeye.backend.codegen.CodeGenerator
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import java.sql.{Connection, PreparedStatement, ResultSet}

/** HTTP routes for code generation functionality. Allows generating model
  * classes from database tables via REST API.
  */
object CodegenRoutes {

  // Request model for table code generation
  case class GenerateTableRequest(
      tableName: String,
      schema: String = "HR",
      jdbcUrl: String = "jdbc:oracle:thin:@//oracle:1521/FREEPDB1",
      username: String = "hr",
      password: String = "hr",
      saveToFile: Boolean = false,
      outputDir: String = "backend/src/main/scala/com/yeye/backend/model"
  )

  // Response model
  case class GenerateResponse(
      tableName: String,
      code: String,
      filePath: Option[String] = None
  )

  def routes: HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      // Get the list of available tables in a schema
      case GET -> Root / "tables" / schema =>
        IO.blocking {
          val conn: Connection = ???
          val stmt: PreparedStatement = conn.prepareStatement(
            "SELECT table_name FROM all_tables WHERE owner = ? ORDER BY table_name"
          )
          stmt.setString(1, schema.toUpperCase)
          stmt.executeQuery()
        }.flatMap { rs =>
          IO.blocking {
            val tables = Iterator
              .continually(rs.next())
              .takeWhile(identity)
              .map(_ => rs.getString("table_name"))
              .toList
            rs.close()
            tables
          }
        }.flatMap(tables => Ok(tables.mkString("\n")))

      // Generate code for a table
      case req @ POST -> Root / "generate" =>
        for {
          request <- req.as[GenerateTableRequest]
          code <- CodeGenerator.generateTableCode(
            request.tableName,
            request.schema,
            request.jdbcUrl,
            request.username,
            request.password
          )
          filePath <-
            if (request.saveToFile) {
              IO.delay {
                val dir = Paths.get(request.outputDir)
                if (!Files.exists(dir)) {
                  Files.createDirectories(dir)
                }

                val className =
                  request.tableName.split("_").map(_.capitalize).mkString
                val path = s"${request.outputDir}/${className}.scala"

                Files.write(
                  Paths.get(path),
                  code.getBytes(StandardCharsets.UTF_8)
                )

                Some(path)
              }
            } else {
              IO.pure(None)
            }
          response = GenerateResponse(request.tableName, code, filePath)
          result <- Ok(response)
        } yield result
    }
  }
}
