package com.yeye.backend.examples

import cats.effect.{IO, IOApp, ExitCode}

/** Example showing how to generate and use model code for the HR.EMPLOYEES
  * table
  *
  * NOTE: This example has been temporarily disabled due to compilation issues.
  * To re-enable it, restore the original code and fix the doobie integration.
  */
object EmployeesExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    IO.println("This example is currently disabled.") *> IO.pure(
      ExitCode.Success
    )
  }
}
