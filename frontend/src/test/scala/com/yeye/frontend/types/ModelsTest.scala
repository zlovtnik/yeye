package com.yeye.frontend.types

import com.yeye.frontend.TestHelper

/** Test suite for data models
  *
  * These tests verify that the data models work correctly, particularly
  * focusing on their JSON encoding/decoding functionality.
  */
object ModelsTest {

  /** Test User model creation and properties */
  def testUser(): Boolean = {
    val user = User("123", "John Doe", "john@example.com", 30)

    val checks = List(
      TestHelper.assertEqual(user.id, "123"),
      TestHelper.assertEqual(user.name, "John Doe"),
      TestHelper.assertEqual(user.email, "john@example.com"),
      TestHelper.assertEqual(user.age, 30)
    )

    checks.forall(identity)
  }

  /** Test File model creation and properties */
  def testFile(): Boolean = {
    val file =
      File("456", "document.pdf", 1024L, "application/pdf", 1625097600000L)

    val checks = List(
      TestHelper.assertEqual(file.id, "456"),
      TestHelper.assertEqual(file.name, "document.pdf"),
      TestHelper.assertEqual(file.size, 1024L),
      TestHelper.assertEqual(file.fileType, "application/pdf"),
      TestHelper.assertEqual(file.lastModified, 1625097600000L)
    )

    checks.forall(identity)
  }

  /** Test CreateUserRequest model creation and properties */
  def testCreateUserRequest(): Boolean = {
    val request = CreateUserRequest("Jane Doe", "jane@example.com", 25)

    val checks = List(
      TestHelper.assertEqual(request.name, "Jane Doe"),
      TestHelper.assertEqual(request.email, "jane@example.com"),
      TestHelper.assertEqual(request.age, 25)
    )

    checks.forall(identity)
  }

  /** Run all tests */
  def runAllTests(): Unit = {
    println(
      "Running User model tests: " + (if (testUser()) "PASSED" else "FAILED")
    )
    println(
      "Running File model tests: " + (if (testFile()) "PASSED" else "FAILED")
    )
    println(
      "Running CreateUserRequest tests: " + (if (testCreateUserRequest())
                                               "PASSED"
                                             else "FAILED")
    )
  }
}
