package com.yeye.frontend.pages

import com.raquo.laminar.api.L.*
import com.yeye.frontend.core.Style

object LandingPage {

  def apply(): Element = {
    div(
      cls := "landing-page",

      // Hero section
      div(
        cls := "hero-section",
        h1("Welcome to YeYe"),
        p("A modern web application built with Scala, ZIO, and Laminar")
      ),

      // Features section
      div(
        cls := "features-section",
        h2("Features"),
        div(
          cls := "feature-cards",

          // Feature 1
          div(
            cls := "feature-card",
            div(cls := "feature-icon", "📊"),
            h3("User Management"),
            p("Easily manage users with our intuitive interface")
          ),

          // Feature 2
          div(
            cls := "feature-card",
            div(cls := "feature-icon", "🚀"),
            h3("Scala Powered"),
            p("Built with Scala 3, ZIO, Cats, and Scala.js")
          ),

          // Feature 3
          div(
            cls := "feature-card",
            div(cls := "feature-icon", "🔒"),
            h3("Secure"),
            p("Enterprise-grade security for your data")
          )
        )
      ),

      // Call to action
      div(
        cls := "cta-section",
        h2("Ready to get started?"),
        button(
          cls := "secondary",
          "Explore Users",
          onClick --> (_ =>
            com.yeye.frontend.Router
              .navigateTo(com.yeye.frontend.Router.UsersListPage)
          )
        )
      )
    )
  }
}
