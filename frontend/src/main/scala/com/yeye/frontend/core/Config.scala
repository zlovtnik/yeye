package com.yeye.frontend.core

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

/** Application configuration and environment settings
  *
  * This object centralizes configuration settings for the frontend application,
  * particularly related to API endpoints and backend communication. It provides
  * functionality to handle different environments (dev/prod) and fallback
  * mechanisms for backend connectivity issues.
  */
object Config {

  /** Flag indicating whether the application is running in development mode
    *
    * Determined by checking if the hostname is localhost or 127.0.0.1
    */
  val isDev =
    dom.window.location.hostname == "localhost" || dom.window.location.hostname == "127.0.0.1"

  /** List of potential backend ports to try connecting to
    *
    * The application will attempt to connect to these ports in order when
    * troubleshooting connectivity issues.
    */
  val backendPorts = List("8080", "3000", "8000")

  /** Index of the currently selected backend port
    *
    * Used to track which port from backendPorts is currently being used
    */
  var currentPortIndex = 0

  /** Gets the current backend base URL using the selected port
    *
    * @return
    *   The base URL for the backend API with the current port
    */
  def getCurrentBackendUrl: String = {
    // For safety, ensure we're within bounds
    if (currentPortIndex >= backendPorts.length) {
      currentPortIndex = 0
    }
    s"http://localhost:${backendPorts(currentPortIndex)}"
  }

  /** Nested object containing API endpoint URLs
    *
    * Centralizes the construction of endpoint URLs based on the current backend
    * URL
    */
  object Endpoints {

    /** Users endpoint URL for accessing user data
      *
      * @return
      *   Full URL to the users endpoint
      */
    def Users: String = s"${getCurrentBackendUrl}/users"

    /** Constructs the URL for a specific user by ID
      *
      * @param id
      *   The user ID
      * @return
      *   Full URL to the specific user endpoint
      */
    def user(id: Long): String = s"${Users}/$id"
  }

  /** Switches to the next available backend port
    *
    * Used for troubleshooting when the current port is not working. Cycles
    * through the available ports in backendPorts.
    */
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

  /** Debug helper to switch to a specific port configuration
    *
    * Can be called from the browser console to try different backend ports.
    *
    * @param portIndex
    *   Index into the backendPorts list (0-2)
    */
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
