package com.yeye.frontend

import com.yeye.frontend.TestHelper

/** Test suite for the Main application component
  *
  * Tests the basic functionality of the Main component that serves as the entry
  * point for the application.
  */
object MainTest {

  /** Test that Main.app is defined */
  def testAppIsDefined(): Boolean = {
    // Simple check that Main.app exists
    Main.app != null
  }

  /** Test that Main.app is a div element */
  def testAppIsDiv(): Boolean = {
    // Check if app is of the expected type without direct type comparison
    // This avoids the warning about unrelated types
    try {
      val appRef = Main.app.ref
      // Check the element name rather than using isInstanceOf
      appRef.tagName.toLowerCase() == "div"
    } catch {
      case _: Throwable => false
    }
  }

  /** Run all tests */
  def runAllTests(): Unit = {
    println(
      "Testing Main.app is defined: " +
        (if (testAppIsDefined()) "PASSED" else "FAILED")
    )

    println(
      "Testing Main.app is a div: " +
        (if (testAppIsDiv()) "PASSED" else "FAILED")
    )
  }
}
