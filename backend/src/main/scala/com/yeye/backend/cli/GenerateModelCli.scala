package com.yeye.backend.cli

import cats.effect.{IO, IOApp, ExitCode}
import com.yeye.backend.codegen.CodeGenerator
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import scala.io.StdIn

/** Command-line application to generate model classes and repositories from
  * database tables.
  *
  * Usage: sbt "runMain com.yeye.backend.cli.GenerateModelCli"
  */
object GenerateModelCli extends IOApp {
  case class Config(
      jdbcUrl: String = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
      username: String = "hr",
      password: String = "hr",
      schema: String = "HR",
      tableName: String = "",
      outputDir: String = "backend/src/main/scala/com/yeye/backend/model"
  )

  def promptForConfig: IO[Config] = for {
    _ <- IO.println("Database Model Generator")
    _ <- IO.println("======================\n")

    jdbcUrl <- IO.println(
      "JDBC URL [jdbc:oracle:thin:@//localhost:1521/FREEPDB1]: "
    ) *>
      IO(StdIn.readLine()).map(s =>
        if (s.isEmpty) "jdbc:oracle:thin:@//localhost:1521/FREEPDB1" else s
      )

    username <- IO.println("Username [hr]: ") *>
      IO(StdIn.readLine()).map(s => if (s.isEmpty) "hr" else s)

    password <- IO.println("Password [hr]: ") *>
      IO(StdIn.readLine()).map(s => if (s.isEmpty) "hr" else s)

    schema <- IO.println("Schema [HR]: ") *>
      IO(StdIn.readLine()).map(s => if (s.isEmpty) "HR" else s)

    tableName <- IO.println("Table name: ") *>
      IO(StdIn.readLine())

    outputDir <- IO.println(
      "Output directory [backend/src/main/scala/com/yeye/backend/model]: "
    ) *>
      IO(StdIn.readLine()).map(s =>
        if (s.isEmpty) "backend/src/main/scala/com/yeye/backend/model" else s
      )
  } yield Config(jdbcUrl, username, password, schema, tableName, outputDir)

  def generateAndSave(config: Config): IO[Unit] = {
    if (config.tableName.isEmpty) {
      IO.println("Error: Table name is required")
    } else {
      for {
        _ <- IO.println(s"Generating code for table: ${config.tableName}")
        code <- CodeGenerator.generateTableCode(
          config.tableName,
          config.schema,
          config.jdbcUrl,
          config.username,
          config.password
        )

        // Ensure output directory exists
        _ <- IO.delay {
          val dir = Paths.get(config.outputDir)
          if (!Files.exists(dir)) {
            Files.createDirectories(dir)
          }
        }

        // Write the generated code to a file
        className = config.tableName.split("_").map(_.capitalize).mkString
        filePath = s"${config.outputDir}/${className}.scala"
        _ <- IO.delay {
          Files.write(
            Paths.get(filePath),
            code.getBytes(StandardCharsets.UTF_8)
          )
        }

        _ <- IO.println(s"Generated code saved to: $filePath")
      } yield ()
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- promptForConfig
      _ <- generateAndSave(config)
    } yield ExitCode.Success
  }
}
