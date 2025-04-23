package com.yeye.frontend.core

import com.yeye.frontend.TestHelper

/** Test suite for the Config
  *
  * Tests the configuration settings and URL construction
  */
object ConfigTest {

  /** Test that Config has backend ports defined */
  def testBackendPortsDefined(): Boolean = {
    Config.backendPorts.nonEmpty && Config.backendPorts.contains("8080")
  }

  /** Test that Config correctly constructs Users endpoint */
  def testUsersEndpoint(): Boolean = {
    // Set to first port for testing
    Config.currentPortIndex = 0
    val expectedUrl = s"http://localhost:${Config.backendPorts(0)}/users"
    Config.Endpoints.Users == expectedUrl
  }

  /** Test that Config correctly constructs user endpoint with ID */
  def testUserEndpoint(): Boolean = {
    // Set to first port for testing
    Config.currentPortIndex = 0
    val userId = 123L
    val expectedUrl =
      s"http://localhost:${Config.backendPorts(0)}/users/$userId"
    Config.Endpoints.user(userId) == expectedUrl
  }

  /** Test that Config correctly cycles through port options */
  def testPortCycling(): Boolean = {
    val initialPort = Config.currentPortIndex
    Config.tryNextPort()
    val newPort = Config.currentPortIndex

    // Either port changed or there's only one port
    val portChangedOrSinglePort =
      (newPort != initialPort || Config.backendPorts.length == 1)

    // Port is within bounds
    val portInBounds = newPort < Config.backendPorts.length

    portChangedOrSinglePort && portInBounds
  }

  /** Run all tests */
  def runAllTests(): Unit = {
    println(
      "Testing backend ports defined: " +
        (if (testBackendPortsDefined()) "PASSED" else "FAILED")
    )

    println(
      "Testing Users endpoint construction: " +
        (if (testUsersEndpoint()) "PASSED" else "FAILED")
    )

    println(
      "Testing user endpoint with ID: " +
        (if (testUserEndpoint()) "PASSED" else "FAILED")
    )

    println(
      "Testing port cycling: " +
        (if (testPortCycling()) "PASSED" else "FAILED")
    )
  }
}
