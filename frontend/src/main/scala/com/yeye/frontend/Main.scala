package com.yeye.frontend

import com.raquo.laminar.api.L.*
import com.yeye.frontend.components.App
import org.scalajs.dom

/** Main entry point for the frontend application.
  *
  * @param args
  *   Command line arguments (unused)
  */
object Main:
  def main(args: Array[String]): Unit =
    // Mount the application to the DOM
    render(dom.document.getElementById("app"), App(unsafeWindowOwner))
