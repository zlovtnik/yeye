package com.yeye.backend.examples

import cats.effect.{IO, IOApp, ExitCode}

/** Simple example to test Oracle database connectivity using doobie
  *
  * NOTE: This example has been temporarily disabled due to compilation issues.
  * To re-enable it, restore the original code and fix the doobie integration.
  */
object SimpleExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    IO.println("This example is currently disabled.") *> IO.pure(
      ExitCode.Success
    )
  }
}
