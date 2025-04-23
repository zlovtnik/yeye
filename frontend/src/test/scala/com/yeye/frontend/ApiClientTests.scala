package com.yeye.frontend

import com.yeye.frontend.api.ApiClient
import com.yeye.frontend.types.CreateUserRequest

// Test suite for ApiClient
class ApiClientTests {

  // Test CreateUserRequest construction
  def testCreateUserRequest(): Unit = {
    val request = CreateUserRequest("Test User", "test@example.com", 30)

    assert(request.name == "Test User")
    assert(request.email == "test@example.com")
    assert(request.age == 30)
  }
}
