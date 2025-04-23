package com.yeye.frontend

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.DynamicImplicits.truthValue
import scala.concurrent.Future
import utest.*

object TestConfig {
  // Initialize globals when the object is first loaded
  private val initialWindow = js.Dynamic
    .literal(
      "fetch" -> js.undefined,
      "document" -> js.Dynamic.literal()
    )
    .asInstanceOf[dom.Window]

  private val initialDocument = js.Dynamic.literal().asInstanceOf[dom.Document]

  // Set up global mocks using js.Dynamic.global
  js.Dynamic.global.updateDynamic("window")(initialWindow)
  js.Dynamic.global.updateDynamic("document")(initialDocument)
  js.Dynamic.global.updateDynamic("fetch")(js.undefined)

  // This object can be used to configure test-specific settings
  // For example, setting up mock implementations or test-specific configurations

  private var mockWindow: Option[dom.Window] = Some(initialWindow)
  private var mockDocument: Option[dom.Document] = Some(initialDocument)
  private var mockFetch: Option[
    js.Function2[String, dom.RequestInit, js.Promise[dom.Response]]
  ] = None

  def setupTestEnvironment(): Unit = {
    // Reset to initial state
    mockWindow = Some(initialWindow)
    mockDocument = Some(initialDocument)
    mockFetch = None

    // Set up global mocks using js.Dynamic.global
    js.Dynamic.global.updateDynamic("window")(mockWindow.get)
    js.Dynamic.global.updateDynamic("document")(mockDocument.get)
    js.Dynamic.global.updateDynamic("fetch")(js.undefined)
  }

  def teardownTestEnvironment(): Unit = {
    // Clean up mocks
    mockWindow.foreach { window =>
      window.asInstanceOf[js.Dynamic].updateDynamic("fetch")(js.undefined)
    }
    mockWindow = None
    mockDocument = None
    mockFetch = None
    js.Dynamic.global.updateDynamic("window")(js.undefined)
    js.Dynamic.global.updateDynamic("document")(js.undefined)
    js.Dynamic.global.updateDynamic("fetch")(js.undefined)
  }

  def getMockWindow: Option[dom.Window] = mockWindow
  def getMockDocument: Option[dom.Document] = mockDocument

  def setMockFetch(
      fetch: js.Function2[String, dom.RequestInit, js.Promise[dom.Response]]
  ): Unit = {
    mockFetch = Some(fetch)
    mockWindow.get.asInstanceOf[js.Dynamic].updateDynamic("fetch")(fetch)
  }

  def clearMockFetch(): Unit = {
    mockFetch = None
    mockWindow.get.asInstanceOf[js.Dynamic].updateDynamic("fetch")(js.undefined)
  }

  // Helper method to create a mock response
  def createMockResponse(body: String): dom.Response = {
    val response = js.Dynamic
      .literal(
        "ok" -> true,
        "status" -> 200,
        "statusText" -> "OK",
        "headers" -> js.Dynamic.literal(),
        "text" -> (() => js.Promise.resolve(body))
      )
      .asInstanceOf[dom.Response]
    response
  }
}
