package com.yeye.frontend

import com.yeye.frontend.types._
import org.scalajs.dom

/** Main class for running in-browser tests
  *
  * This provides a way to run tests directly in the browser after the Scala.js
  * code is compiled
  */
object TestMain {

  /** Run model tests */
  def runModelTests(): Boolean = {
    var passed = true

    try {
      // Test User model
      val user = User("123", "John Doe", "john@example.com", 30)
      if (
        user.id != "123" ||
        user.name != "John Doe" ||
        user.email != "john@example.com" ||
        user.age != 30
      ) {
        logError("User model test failed")
        passed = false
      } else {
        logSuccess("User model test passed")
      }

      // Test File model
      val file =
        File("456", "document.pdf", 1024L, "application/pdf", 1625097600000L)
      if (
        file.id != "456" ||
        file.name != "document.pdf" ||
        file.size != 1024L ||
        file.fileType != "application/pdf" ||
        file.lastModified != 1625097600000L
      ) {
        logError("File model test failed")
        passed = false
      } else {
        logSuccess("File model test passed")
      }

      // Test CreateUserRequest model
      val request = CreateUserRequest("Jane Doe", "jane@example.com", 25)
      if (
        request.name != "Jane Doe" ||
        request.email != "jane@example.com" ||
        request.age != 25
      ) {
        logError("CreateUserRequest model test failed")
        passed = false
      } else {
        logSuccess("CreateUserRequest model test passed")
      }
    } catch {
      case e: Exception =>
        logError(s"Exception in model tests: ${e.getMessage}")
        passed = false
    }

    passed
  }

  /** Test router functionality */
  def testRouter(): Boolean = {
    try {
      // Simple tests
      val landingPage = Router.LandingPage
      val usersPage = Router.UsersListPage

      // Just verify no exceptions are thrown
      logSuccess("Router test passed")
      true
    } catch {
      case e: Exception =>
        logError(s"Router test failed: ${e.getMessage}")
        false
    }
  }

  /** Add a result to the output with styling */
  private def logResult(message: String, color: String): Unit = {
    dom.console.log(s"%c$message", s"color: $color; font-weight: bold")
  }

  /** Log a success message */
  private def logSuccess(message: String): Unit = {
    logResult(s"✅ $message", "green")
  }

  /** Log an error message */
  private def logError(message: String): Unit = {
    logResult(s"❌ $message", "red")
  }

  /** Main entry point
    *
    * @param args
    *   Command line arguments (unused)
    */
  def main(args: Array[String]): Unit = {
    dom.console.log("Running frontend tests in browser")

    var testsPassed = 0
    var testsFailed = 0

    // Run model tests
    if (runModelTests()) {
      testsPassed += 1
    } else {
      testsFailed += 1
    }

    // Run router tests
    if (testRouter()) {
      testsPassed += 1
    } else {
      testsFailed += 1
    }

    // Summary
    dom.console.log("==== Test Summary ====")
    dom.console.log(s"Tests passed: $testsPassed")
    dom.console.log(s"Tests failed: $testsFailed")

    if (testsFailed > 0) {
      logError(s"$testsFailed tests failed")
    } else {
      logSuccess("All tests passed")
    }
  }
}
