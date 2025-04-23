package com.yeye.frontend

import com.yeye.frontend.types.ModelsTest
import com.yeye.frontend.api.ApiClientTest
import com.yeye.frontend.services.ApiServiceTest
import com.yeye.frontend.core.ConfigTest

/** Main test runner for the frontend application
  *
  * This object provides a way to run all tests directly.
  */
object TestRunner {

  /** Executes all test suites
    *
    * @return
    *   Total number of failed tests
    */
  def runAllTests(): Int = {
    println("Starting YeYe Frontend Tests")
    println("===========================")

    var passedCount = 0
    var failedCount = 0

    // Helper to run and report tests
    def runSuite(name: String, tests: List[(String, Boolean)]): Unit = {
      println(s"\n$name:")

      tests.foreach { case (testName, result) =>
        if (result) {
          println(s"  ✅ $testName: PASSED")
          passedCount += 1
        } else {
          println(s"  ❌ $testName: FAILED")
          failedCount += 1
        }
      }
    }

    // Run model tests
    runSuite(
      "Model Tests",
      List(
        "User model" -> ModelsTest.testUser(),
        "File model" -> ModelsTest.testFile(),
        "CreateUserRequest model" -> ModelsTest.testCreateUserRequest()
      )
    )

    // Run Main tests
    runSuite(
      "Main Component Tests",
      List(
        "Main.app is defined" -> MainTest.testAppIsDefined(),
        "Main.app is a div" -> MainTest.testAppIsDiv()
      )
    )

    // Run Router tests
    runSuite(
      "Router Tests",
      List(
        "Empty hash mapping" -> RouterTest.testEmptyHashMapping(),
        "Users hash mapping" -> RouterTest.testUsersHashMapping(),
        "Page rendering" -> RouterTest.testPageRendering()
      )
    )

    // Run Config tests
    runSuite(
      "Config Tests",
      List(
        "Backend ports defined" -> ConfigTest.testBackendPortsDefined(),
        "Users endpoint" -> ConfigTest.testUsersEndpoint(),
        "User endpoint" -> ConfigTest.testUserEndpoint(),
        "Port cycling" -> ConfigTest.testPortCycling()
      )
    )

    // Run ApiClient tests
    runSuite(
      "ApiClient Tests",
      List(
        "GetUsers URL" -> ApiClientTest.testGetUsersUrl(),
        "CreateUser request" -> ApiClientTest.testCreateUserRequest(),
        "GetUser URL" -> ApiClientTest.testGetUserUrl()
      )
    )

    // Run ApiService tests
    runSuite(
      "ApiService Tests",
      List(
        "ApiService methods" -> ApiServiceTest.testApiServiceMethods()
      )
    )

    println("\nTest Summary")
    println(s"Passed: $passedCount")
    println(s"Failed: $failedCount")
    println(s"Total: ${passedCount + failedCount}")

    failedCount
  }

  /** Entry point for direct execution
    *
    * @param args
    *   Command line arguments (unused)
    */
  def main(args: Array[String]): Unit = {
    val failedTests = runAllTests()
    if (failedTests > 0) {
      throw new AssertionError(s"$failedTests tests failed")
    }
  }
}
