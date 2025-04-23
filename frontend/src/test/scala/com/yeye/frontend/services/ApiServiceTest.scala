package com.yeye.frontend.services

import com.yeye.frontend.TestHelper
import com.yeye.frontend.types.User

/** Test suite for the ApiService
  *
  * Tests the service layer that interacts with the backend API
  */
object ApiServiceTest {

  /** Test that verifies ApiService operations exist and have the expected
    * signatures
    */
  def testApiServiceMethods(): Boolean = {
    // Instead of using reflection (which doesn't work in Scala.js),
    // we just verify that the methods can be referenced without errors

    // Test reference to getUsers method
    val getUsersRef = ApiService.getUsers

    // Test reference to createUser method
    val createUserRef = ApiService.createUser(_: String, _: String, _: Int)

    // Test reference to getUser method
    val getUserRef = ApiService.getUser(_: String)

    // If we can compile and reference these methods, they exist
    true
  }

  /** Run all tests */
  def runAllTests(): Unit = {
    println(
      "Testing ApiService methods: " +
        (if (testApiServiceMethods()) "PASSED" else "FAILED")
    )
  }
}
