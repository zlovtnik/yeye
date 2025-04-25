package com.yeye.frontend

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.raquo.waypoint.{*, given}
import com.raquo.waypoint.Route
import urldsl.language.PathSegment
import com.raquo.waypoint.unaryPathSegment

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
  def renderPage(page: Page): L.Element = page match {
    case LandingPage   => com.yeye.frontend.pages.LandingPage()
    case UsersListPage => com.yeye.frontend.pages.users.UsersPage()
  }

  private val landingRoute = Route.static(LandingPage, root)
  private val usersRoute = Route.static(UsersListPage, root / "users")

  val appRouter = new Router[Page](
    routes = List(landingRoute, usersRoute),
    getPageTitle = {
      case LandingPage   => "YeYe - Home"
      case UsersListPage => "YeYe - Users"
    },
    serializePage = {
      case LandingPage   => ""
      case UsersListPage => "users"
    },
    deserializePage = {
      case ""      => LandingPage
      case "users" => UsersListPage
    }
  )(
    popStateEvents = L.windowEvents(_.onPopState),
    owner = Main.routerOwner,
    origin = dom.window.location.origin,
    initialUrl = dom.window.location.href
  )

  /** Signal that tracks the current page based on URL hash changes
    *
    * This provides a reactive way to observe the current page state and will
    * update automatically when the URL hash changes.
    *
    * @return
    *   A Signal containing the current Page
    */
  def currentPageSignal: L.Signal[Page] = appRouter.currentPageSignal

  /** Navigates to the specified page by updating the URL hash
    *
    * @param page
    *   The page to navigate to
    */
  def navigateTo(page: Page): Unit = appRouter.pushState(page)

  /** Router component that renders the current page
    *
    * @return
    *   A Laminar Element that updates when the page changes
    */
  def apply(): L.Element = {
    L.div(
      L.child <-- currentPageSignal.map(renderPage)
    )
  }
}
