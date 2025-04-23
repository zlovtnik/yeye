package com.yeye.frontend.components.layout

import com.raquo.laminar.api.L.* // Base import for modifiers, lifecycle, tags etc.
import org.scalajs.dom
import com.yeye.frontend.Router // Uncomment when used

// TODO: Required imports
// import com.yeye.frontend.core.Style

object BurgerMenu {

  // Menu item model
  case class MenuItem(
      id: String,
      label: String,
      page: Router.Page,
      icon: Option[String] = None
  )

  // TODO: Define menu items
  val menuItems = Seq(
    MenuItem("home", "Home", Router.LandingPage),
    MenuItem("users", "Users", Router.UsersListPage)
  )

  def apply(
      currentPage: Signal[Router.Page],
      navigate: Router.Page => Unit
  ): Element = {
    val isExpanded = Var(false)

    div(
      cls := "burger-menu",
      cls <-- isExpanded.signal.map(expanded =>
        if (expanded) "burger-menu expanded" else "burger-menu"
      ),

      // Burger icon for mobile
      div(
        cls := "burger-icon",
        span(cls := "bar"),
        span(cls := "bar"),
        span(cls := "bar"),
        onClick --> (_ => isExpanded.update(!_))
      ),

      // Menu container with hover effect
      div(
        cls := "menu-container",
        onMouseEnter --> (_ => isExpanded.set(true)),
        onMouseLeave --> (_ => isExpanded.set(false)),

        // Menu header/logo
        div(
          cls := "menu-header",
          h2("YeYe")
        ),

        // Menu items list
        ul(
          cls := "menu-items",

          // Home menu item
          li(
            cls <-- currentPage.map(p =>
              if (p == Router.LandingPage) "menu-item active" else "menu-item"
            ),
            onClick.preventDefault.mapTo(Router.LandingPage) --> navigate,
            div(cls := "menu-icon", "🏠"),
            span(cls := "menu-label", "Home")
          ),

          // Users menu item
          li(
            cls <-- currentPage.map(p =>
              if (p == Router.UsersListPage) "menu-item active" else "menu-item"
            ),
            onClick.preventDefault.mapTo(Router.UsersListPage) --> navigate,
            div(cls := "menu-icon", "👥"),
            span(cls := "menu-label", "Users")
          )
        )
      )
    )
  }
}
