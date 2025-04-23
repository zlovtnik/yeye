package com.yeye.frontend

import utest._
import com.yeye.frontend.{TestRunner => YeYeTestRunner}

/** UTest runner that integrates with sbt test
  *
  * This is a minimal test file that just calls our TestRunner but allows the
  * utest framework to detect and run it.
  */
object UTestRunner extends TestSuite {
  val tests = Tests {
    test("Run all YeYe tests") {
      // Run our custom test runner
      val failedTests = YeYeTestRunner.runAllTests()
      assert(failedTests == 0)
    }
  }
}
