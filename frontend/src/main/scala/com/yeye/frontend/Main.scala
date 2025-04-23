package com.yeye.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import scala.concurrent.ExecutionContext.Implicits.global
import com.yeye.frontend.components.layout.BurgerMenu
import com.yeye.frontend.core.Style

case class User(id: Long, name: String, email: String)

/** Main entry point for the frontend application.
  *
  * @param args
  *   Command line arguments (unused)
  */
object Main:
  def main(args: Array[String]): Unit =
    // Initialize styles
    Style.init()

    val containerNode = dom.document.querySelector("#app")
    render(containerNode, app)

  def app: Div = {
    // Page routing
    val currentPage = Router.currentPageSignal

    div(
      cls := "app-container",

      // Side menu
      BurgerMenu(
        currentPage,
        Router.navigateTo
      ),

      // Main content area
      div(
        cls := "content-container",
        child <-- currentPage.map(Router.renderPage)
      )
    )
  }

  // Keep the user-related code for reference, to be moved to UsersPage later
  def oldUserApp: Div = {
    val users = Var(List.empty[User])
    val nameVar = Var("")
    val emailVar = Var("")

    def fetchUsers(): Unit = {
      val query = """
        query {
          users {
            id
            name
            email
          }
        }
      """

      dom
        .fetch(
          "/api/graphql",
          new dom.RequestInit {
            method = dom.HttpMethod.POST
            headers = new dom.Headers {
              append("Content-Type", "application/json")
            }
            body = s"""{"query": "$query"}"""
          }
        )
        .toFuture
        .flatMap(_.text().toFuture)
        .foreach { response =>
          decode[Map[String, Map[String, List[User]]]](response)
            .foreach(data => users.set(data("data")("users")))
        }
    }

    def createUser(): Unit = {
      val mutation = s"""
        mutation {
          createUser(input: {
            name: "${nameVar.now()}"
            email: "${emailVar.now()}"
          }) {
            id
            name
            email
          }
        }
      """

      dom
        .fetch(
          "/api/graphql",
          new dom.RequestInit {
            method = dom.HttpMethod.POST
            headers = new dom.Headers {
              append("Content-Type", "application/json")
            }
            body = s"""{"query": "$mutation"}"""
          }
        )
        .toFuture
        .flatMap(_.text().toFuture)
        .foreach { _ =>
          nameVar.set("")
          emailVar.set("")
          fetchUsers()
        }
    }

    div(
      h1("User Management"),
      div(
        div(
          input(
            placeholder := "Name",
            controlled(
              value <-- nameVar,
              onInput.mapToValue --> nameVar
            )
          ),
          input(
            placeholder := "Email",
            controlled(
              value <-- emailVar,
              onInput.mapToValue --> emailVar
            )
          ),
          button(
            "Add User",
            onClick --> (_ => createUser())
          )
        )
      ),
      div(
        children <-- users.signal.map { userList =>
          userList.map { user =>
            div(
              div(user.name),
              div(user.email)
            )
          }
        }
      ),
      onMountCallback(_ => fetchUsers())
    )
  }
