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

object BrowserTest extends TestSuite {
  // Mock the fetch function for testing
  private var mockFetchCalls: List[(String, dom.RequestInit)] = Nil
  private var mockFetchResponses: Map[String, String] = Map.empty

  override def utestBeforeEach(path: Seq[String]): Unit = {
    TestConfig.setupTestEnvironment()
    mockFetchCalls = Nil
    mockFetchResponses = Map.empty
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
    test("Browser environment setup") {
      // Verify that we're running in a browser environment
      assert(js.typeOf(dom.window) == "object")
      assert(js.typeOf(dom.document) == "object")
      assert(
        js.typeOf(
          TestConfig.getMockWindow.get.asInstanceOf[js.Dynamic].fetch
        ) == "undefined"
      )
    }

    test("GraphQL request headers") {
      // Setup mock response
      mockFetchResponses = Map(
        "/api/graphql" -> """{"data": {"users": []}}"""
      )

      // Set up mock fetch
      TestConfig.setMockFetch(mockFetch)

      // Execute the query
      val result = Main.executeGraphQL(Main.usersQuery)

      // Verify the fetch was called with correct headers
      assert(mockFetchCalls.length == 1)
      val (_, init) = mockFetchCalls.head
      val headers = init.headers.asInstanceOf[js.Dictionary[String]]
      assert(headers("Content-Type") == "application/json")
      assert(headers("Accept") == "application/json")
    }

    test("GraphQL request body formatting") {
      // Setup mock response
      mockFetchResponses = Map(
        "/api/graphql" -> """{"data": {"users": []}}"""
      )

      // Set up mock fetch
      TestConfig.setMockFetch(mockFetch)

      // Execute the query
      val result = Main.executeGraphQL(Main.usersQuery)

      // Verify the request body format
      assert(mockFetchCalls.length == 1)
      val (_, init) = mockFetchCalls.head
      val body = init.body.asInstanceOf[String]
      val json = js.JSON.parse(body)
      assert(json.hasOwnProperty("query"))
      assert(json.hasOwnProperty("variables"))
      assert(json.variables.toString == "{}")
    }

    test("GraphQL response handling in browser") {
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)

      // Setup mock response
      mockFetchResponses = Map(
        "/api/graphql" -> s"""{"data": {"users": [${testUser.toJson}]}}"""
      )

      // Set up mock fetch
      TestConfig.setMockFetch(mockFetch)

      // Execute the query
      val result = Main.executeGraphQL(Main.usersQuery)

      // Verify the response handling
      result.map { response =>
        val json = js.JSON.parse(response).asInstanceOf[js.Dynamic]
        assert(json.data.users.asInstanceOf[js.Array[js.Dynamic]].length == 1)
        assert(
          json.data.users
            .asInstanceOf[js.Array[js.Dynamic]](0)
            .id
            .asInstanceOf[String] == testUser.id
        )
        assert(
          json.data.users
            .asInstanceOf[js.Array[js.Dynamic]](0)
            .email
            .asInstanceOf[String] == testUser.email
        )
      }
    }

    test("GraphQL error handling in browser") {
      // Setup mock error response
      mockFetchResponses = Map(
        "/api/graphql" -> """{
          "errors": [
            {
              "message": "Network error",
              "extensions": {
                "code": "NETWORK_ERROR"
              }
            }
          ]
        }"""
      )

      // Set up mock fetch
      TestConfig.setMockFetch(mockFetch)

      // Execute the query and verify error handling
      val result = Main.executeGraphQL(Main.usersQuery)

      result.failed.map { exception =>
        assert(exception.getMessage.contains("GraphQL Error"))
        assert(exception.getMessage.contains("Network error"))
      }
    }

    test("GraphQL timeout handling") {
      // Setup mock to simulate timeout
      TestConfig.setMockFetch((_, _) =>
        js.Promise.reject(new Exception("Request timeout"))
      )

      // Execute the query and verify timeout handling
      val result = Main.executeGraphQL(Main.usersQuery)

      result.failed.map { exception =>
        assert(exception.getMessage == "Request timeout")
      }
    }
  }
}
