package com.yeye.frontend.components

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint.{*, given}
import com.yeye.frontend.pages.users.UsersPage
import com.yeye.frontend.pages.LandingPage
import com.yeye.frontend.Router
import org.scalajs.dom

object App {

  def apply(): L.HtmlElement = {
    val router = Router.appRouter

    L.div(
      L.cls := "app-container",
      L.div(
        L.cls := "app-header",
        L.h1("YeYe Application"),
        L.div(
          L.cls := "nav-links",
          L.a(
            L.cls := "nav-link",
            L.href := router.relativeUrlForPage(Router.LandingPage),
            "Home"
          ),
          L.a(
            L.cls := "nav-link",
            L.href := router.relativeUrlForPage(Router.UsersListPage),
            "Users"
          )
        )
      ),
      L.div(
        L.cls := "app-content",
        L.child <-- Router.currentPageSignal.map {
          case Router.LandingPage   => LandingPage()
          case Router.UsersListPage => UsersPage()
        }
      )
    )
  }
}
