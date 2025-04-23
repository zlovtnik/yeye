package com.yeye.frontend

// Simple test runner that extends JUnit's FunSuite
class MUnitTestRunner {

  // Run YeYe frontend tests using our custom runner
  def testYeYeFrontend(): Unit = {
    // Run our custom test runner
    val failedTests = TestRunner.runAllTests()
    // Just print the results - we don't have junit assertions available
    val success = failedTests == 0
    if (!success) {
      throw new AssertionError(s"Tests failed: $failedTests")
    }
  }
}
