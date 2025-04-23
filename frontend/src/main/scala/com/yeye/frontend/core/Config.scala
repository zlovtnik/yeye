package com.yeye.frontend.core

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

object Config {
  // Detect development vs production
  val isDev =
    dom.window.location.hostname == "localhost" || dom.window.location.hostname == "127.0.0.1"

  // Try multiple backend ports
  val backendPorts = List("8080", "3000", "8000")

  // Current port index for troubleshooting
  var currentPortIndex = 0

  // Backend URL options
  def getCurrentBackendUrl: String = {
    // For safety, ensure we're within bounds
    if (currentPortIndex >= backendPorts.length) {
      currentPortIndex = 0
    }
    s"http://localhost:${backendPorts(currentPortIndex)}"
  }

  // API endpoints
  object Endpoints {
    // Fixed endpoint calculation
    def Users: String = s"${getCurrentBackendUrl}/users"

    def user(id: Long): String = s"${Users}/$id"
  }

  // Try the next port configuration
  def tryNextPort(): Unit = {
    currentPortIndex = (currentPortIndex + 1) % backendPorts.length
    dom.console.log(s"Trying next port configuration: ${getCurrentBackendUrl}")
    dom.console.log(s"Users endpoint now: ${Endpoints.Users}")
  }

  // Initial logging
  dom.console.log(s"Backend URL: ${getCurrentBackendUrl}")
  dom.console.log(s"Users endpoint: ${Endpoints.Users}")
  dom.console.log(
    "To try different ports, call fetchFromPort() with numbers 0-2"
  )

  // Debug helper method that can be called from browser console
  def fetchFromPort(portIndex: Int): Unit = {
    if (portIndex >= 0 && portIndex < backendPorts.length) {
      currentPortIndex = portIndex
      dom.console.log(s"Switched to port ${backendPorts(portIndex)}")
      dom.console.log(s"Users endpoint now: ${Endpoints.Users}")
      // Try a fetch
      dom
        .fetch(Endpoints.Users)
        .toFuture
        .foreach(response =>
          dom.console
            .log(s"Response status: ${response.status} ${response.statusText}")
        )
    } else {
      dom.console.error(
        s"Invalid port index. Must be between 0 and ${backendPorts.length - 1}"
      )
    }
  }
}
