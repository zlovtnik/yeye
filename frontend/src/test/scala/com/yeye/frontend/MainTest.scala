package com.yeye.frontend

import com.raquo.laminar.api.L.*
import com.yeye.shared.User
import zio.json.*
import org.scalajs.dom
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import org.scalajs.dom.HttpMethod
import scalajs.js.DynamicImplicits.truthValue
import utest.{TestSuite, Tests, test}

object MainTest extends TestSuite {
  private var mockFetchCalls: List[(String, dom.RequestInit)] = Nil
  private var mockFetchResponses: Map[String, String] = Map.empty

  override def utestBeforeEach(path: Seq[String]): Unit = {
    TestConfig.setupTestEnvironment()
    mockFetchCalls = Nil
    mockFetchResponses = Map.empty
    // Set up mock fetch in the test environment
    TestConfig.setMockFetch(mockFetch)
  }

  override def utestAfterEach(path: Seq[String]): Unit = {
    TestConfig.clearMockFetch()
    TestConfig.teardownTestEnvironment()
  }

  // Mock implementation of fetch
  private def mockFetch(
      requestUrl: String,
      init: dom.RequestInit
  ): js.Promise[dom.Response] = {
    mockFetchCalls = mockFetchCalls :+ (requestUrl, init)
    js.Promise.resolve(
      TestConfig.createMockResponse(
        mockFetchResponses.getOrElse(requestUrl, "{}")
      )
    )
  }

  val tests = Tests {
    test("GraphQL query execution") {
      // Setup mock response
      val mockUsers = List(
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)
      )
      mockFetchResponses = Map(
        "/api/graphql" -> s"""{"data": {"users": ${mockUsers.toJson}}}"""
      )

      // Execute the query
      val result = Main.executeGraphQL(Main.usersQuery)

      // Verify the fetch was called with correct parameters
      assert(mockFetchCalls.length == 1)
      val (url, init) = mockFetchCalls.head
      assert(url == "/api/graphql")
      assert(init.method == HttpMethod.POST)
      val headers = init.headers.asInstanceOf[js.Dictionary[String]]
      assert(headers("Content-Type") == "application/json")
      assert(headers("Accept") == "application/json")

      // Verify the response parsing
      result.map { response =>
        val users = response.fromJson[List[User]].getOrElse(List.empty)
        assert(users.length == 1)
        assert(users.head.email == "test@example.com")
      }
    }

    test("User creation") {
      // Setup mock response
      val mockUser = User("1", "new@example.com", "New", "User", "active", 0, 0)
      mockFetchResponses = Map(
        "/api/graphql" -> s"""{"data": {"createUser": ${mockUser.toJson}}}"""
      )

      // Create test variables
      val variables = s"""{
        "email": "new@example.com",
        "firstName": "New",
        "lastName": "User",
        "status": "active"
      }"""

      // Execute the mutation
      val result = Main.executeGraphQL(Main.createUserMutation, variables)

      // Verify the fetch was called with correct parameters
      assert(mockFetchCalls.length == 1)
      val (url, init) = mockFetchCalls.head
      assert(url == "/api/graphql")
      assert(init.method == HttpMethod.POST)
      val headers = init.headers.asInstanceOf[js.Dictionary[String]]
      assert(headers("Content-Type") == "application/json")
      assert(headers("Accept") == "application/json")

      // Verify the response parsing
      result.map { response =>
        val user = response.fromJson[User].getOrElse(User.empty)
        assert(user.email == "new@example.com")
        assert(user.firstName == "New")
        assert(user.lastName == "User")
        assert(user.status == "active")
      }
    }

    test("User update") {
      // Setup mock response
      val mockUser =
        User("1", "updated@example.com", "Updated", "User", "inactive", 0, 0)
      mockFetchResponses = Map(
        "/api/graphql" -> s"""{"data": {"updateUser": ${mockUser.toJson}}}"""
      )

      // Create test variables
      val variables = s"""{
        "id": "1",
        "email": "updated@example.com",
        "firstName": "Updated",
        "lastName": "User",
        "status": "inactive"
      }"""

      // Execute the mutation
      val result = Main.executeGraphQL(Main.updateUserMutation, variables)

      // Verify the fetch was called with correct parameters
      assert(mockFetchCalls.length == 1)
      val (url, init) = mockFetchCalls.head
      assert(url == "/api/graphql")
      assert(init.method == HttpMethod.POST)
      val headers = init.headers.asInstanceOf[js.Dictionary[String]]
      assert(headers("Content-Type") == "application/json")
      assert(headers("Accept") == "application/json")

      // Verify the response parsing
      result.map { response =>
        val user = response.fromJson[User].getOrElse(User.empty)
        assert(user.email == "updated@example.com")
        assert(user.firstName == "Updated")
        assert(user.lastName == "User")
        assert(user.status == "inactive")
      }
    }

    test("User deletion") {
      // Setup mock response
      mockFetchResponses = Map(
        "/api/graphql" -> """{"data": {"deleteUser": true}}"""
      )

      // Create test variables
      val variables = s"""{"id": "1"}"""

      // Execute the mutation
      val result = Main.executeGraphQL(Main.deleteUserMutation, variables)

      // Verify the fetch was called with correct parameters
      assert(mockFetchCalls.length == 1)
      val (url, init) = mockFetchCalls.head
      assert(url == "/api/graphql")
      assert(init.method == HttpMethod.POST)
      val headers = init.headers.asInstanceOf[js.Dictionary[String]]
      assert(headers("Content-Type") == "application/json")
      assert(headers("Accept") == "application/json")

      // Verify the response parsing
      result.map { response =>
        val json = js.JSON.parse(response).asInstanceOf[js.Dynamic]
        assert(json.data.deleteUser.asInstanceOf[Boolean] == true)
      }
    }

    test("GraphQL error handling") {
      // Setup mock response with error
      mockFetchResponses = Map(
        "/api/graphql" -> """{"errors": [{"message": "Invalid input", "path": ["createUser"]}]}"""
      )

      // Create test variables
      val variables = s"""{
        "email": "invalid@example.com",
        "firstName": "Invalid",
        "lastName": "User",
        "status": "invalid"
      }"""

      // Execute the mutation and verify it throws an exception
      val result = Main.executeGraphQL(Main.createUserMutation, variables)

      result.failed.map { exception =>
        assert(exception.getMessage.contains("GraphQL Error"))
        assert(exception.getMessage.contains("Invalid input"))
      }
    }

    test("Empty response handling") {
      // Setup mock response with empty data
      mockFetchResponses = Map(
        "/api/graphql" -> """{"data": {}}"""
      )

      // Execute the query
      val result = Main.executeGraphQL(Main.usersQuery)

      // Verify the response parsing
      result.map { response =>
        val users = response.fromJson[List[User]].getOrElse(List.empty)
        assert(users.isEmpty)
      }
    }

    test("Network error handling") {
      // Setup mock to simulate network error
      TestConfig.setMockFetch((_, _) =>
        js.Promise.reject(new Exception("Network error"))
      )

      // Execute the query and verify error handling
      val result = Main.executeGraphQL(Main.usersQuery)

      result.failed.map { exception =>
        assert(exception.getMessage == "Network error")
      }
    }

    test("Invalid JSON response handling") {
      // Setup mock response with invalid JSON
      mockFetchResponses = Map(
        "/api/graphql" -> "invalid json"
      )

      // Execute the query and verify error handling
      val result = Main.executeGraphQL(Main.usersQuery)

      result.failed.map { exception =>
        assert(exception.getMessage.contains("JSON"))
      }
    }
  }
}
