package com.yeye.backend.examples

import cats.effect.{IO, IOApp, ExitCode}
import com.yeye.backend.codegen.ExampleGenerator

/** Example app that generates a case class for the EMPLOYEES table Uses direct
  * JDBC instead of doobie to avoid compatibility issues
  */
object GenerateEmployees extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    // JDBC connection parameters - can be overridden with command-line args
    val jdbcUrl =
      args.headOption.getOrElse("jdbc:oracle:thin:@//localhost:1521/FREEPDB1")
    val username = if (args.length > 1) args(1) else "hr"
    val password = if (args.length > 2) args(2) else "hr"

    // Table to generate
    val tableName = "EMPLOYEES"
    val schema = "HR"

    // Generate the code and print to console
    for {
      _ <- IO.println(s"Generating code for $schema.$tableName...")
      _ <- IO.println(s"Using connection: $jdbcUrl")

      // Generate the model code
      result <- ExampleGenerator
        .generateModelCode(
          tableName,
          schema,
          jdbcUrl,
          username,
          password
        )
        .attempt

      // Handle the result
      _ <- result match {
        case Right(code) =>
          IO.println(s"Successfully generated code:\n\n$code")
        case Left(error) =>
          IO.println(s"Error generating code: ${error.getMessage}")
      }
    } yield ExitCode.Success
  }
}
