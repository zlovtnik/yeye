package com.yeye.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom
// Using fully qualified names for Waypoint classes to avoid import issues

object Router {
  // Define Page ADT (Algebraic Data Type)
  sealed trait Page
  case object LandingPage extends Page
  case object UsersListPage extends Page

  // Static renderer function for each page
  def renderPage(page: Page): Element = page match {
    case LandingPage   => com.yeye.frontend.pages.LandingPage()
    case UsersListPage => com.yeye.frontend.pages.users.UsersPage()
  }

  // Simple route parsing logic
  def currentPageSignal: Signal[Page] = {
    val locationHash = windowEvents(_.onHashChange)
      .map(_ => dom.window.location.hash)
      .toSignal(dom.window.location.hash)

    locationHash.map {
      case "#users" => UsersListPage
      case _        => LandingPage
    }
  }

  // Navigation helpers
  def navigateTo(page: Page): Unit = page match {
    case LandingPage   => dom.window.location.hash = ""
    case UsersListPage => dom.window.location.hash = "users"
  }

  // Router component that listens to URL changes and renders the appropriate page
  def apply(): Element = {
    div(
      child <-- currentPageSignal.map(renderPage)
    )
  }
}
