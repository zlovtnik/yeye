package com.yeye.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom
// Using fully qualified names for Waypoint classes to avoid import issues

/** Application router that handles navigation between pages
  *
  * This object manages page routing for the application, detecting URL changes
  * and rendering the appropriate page components. It provides a simple
  * hash-based routing mechanism and navigation helpers.
  */
object Router {

  /** Algebraic Data Type (ADT) representing the different pages in the
    * application
    *
    * Each case object represents a distinct page that can be navigated to.
    */
  sealed trait Page

  /** The landing/home page of the application */
  case object LandingPage extends Page

  /** The users list page showing all users */
  case object UsersListPage extends Page

  /** Renders the appropriate component for a given page
    *
    * @param page
    *   The page to render
    * @return
    *   The Laminar Element containing the rendered page
    */
  def renderPage(page: Page): Element = page match {
    case LandingPage   => com.yeye.frontend.pages.LandingPage()
    case UsersListPage => com.yeye.frontend.pages.users.UsersPage()
  }

  /** Signal that tracks the current page based on URL hash changes
    *
    * This provides a reactive way to observe the current page state and will
    * update automatically when the URL hash changes.
    *
    * @return
    *   A Signal containing the current Page
    */
  def currentPageSignal: Signal[Page] = {
    val locationHash = windowEvents(_.onHashChange)
      .map(_ => dom.window.location.hash)
      .toSignal(dom.window.location.hash)

    locationHash.map {
      case "#users" => UsersListPage
      case _        => LandingPage
    }
  }

  /** Navigates to the specified page by updating the URL hash
    *
    * @param page
    *   The page to navigate to
    */
  def navigateTo(page: Page): Unit = page match {
    case LandingPage   => dom.window.location.hash = ""
    case UsersListPage => dom.window.location.hash = "users"
  }

  /** Router component that renders the current page
    *
    * @return
    *   A Laminar Element that updates when the page changes
    */
  def apply(): Element = {
    div(
      child <-- currentPageSignal.map(renderPage)
    )
  }
}
