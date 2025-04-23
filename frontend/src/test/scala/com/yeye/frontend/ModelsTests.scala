package com.yeye.frontend

import com.yeye.frontend.types._

// Test suite for models using simple assertions
class ModelsTests {

  // Test User model
  def testUser(): Unit = {
    val user = User("123", "John Doe", "john@example.com", 30)

    assert(user.id == "123")
    assert(user.name == "John Doe")
    assert(user.email == "john@example.com")
    assert(user.age == 30)
  }

  // Test File model
  def testFile(): Unit = {
    val file =
      File("456", "document.pdf", 1024L, "application/pdf", 1625097600000L)

    assert(file.id == "456")
    assert(file.name == "document.pdf")
    assert(file.size == 1024L)
    assert(file.fileType == "application/pdf")
    assert(file.lastModified == 1625097600000L)
  }

  // Test CreateUserRequest model
  def testCreateUserRequest(): Unit = {
    val request = CreateUserRequest("Jane Doe", "jane@example.com", 25)

    assert(request.name == "Jane Doe")
    assert(request.email == "jane@example.com")
    assert(request.age == 25)
  }
}
