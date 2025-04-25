package com.yeye.frontend

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import scala.concurrent.ExecutionContext.Implicits.global
import com.yeye.frontend.components.layout.BurgerMenu
import com.yeye.frontend.core.Style

/** Represents a user in the application
  *
  * @param id
  *   Unique identifier for the user
  * @param name
  *   User's full name
  * @param email
  *   User's email address
  */
case class User(id: Long, name: String, email: String)

object User {
  implicit val decoder: io.circe.Decoder[User] = deriveDecoder[User]
  implicit val encoder: io.circe.Encoder[User] = deriveEncoder[User]
}

/** Main entry point for the frontend application.
  *
  * This object serves as the entry point for the Scala.js application. It
  * initializes the UI, renders the main application structure, and handles the
  * high-level application flow.
  */
object Main {

  /** Flag to indicate whether to run tests on startup Can be set via URL
    * parameter: ?test=true
    */
  private val shouldRunTests: Boolean = {
    val url = dom.window.location.search
    val testParam = """[?&]test=([^&]*)""".r.findFirstMatchIn(url)
    testParam.exists(_.group(1) == "true")
  }

  /** Application entry point method
    *
    * This is called automatically by the Scala.js runtime when the application
    * starts. It initializes styles and renders the root component into the DOM.
    *
    * @param args
    *   Command line arguments (unused)
    */
  def main(args: Array[String]): Unit = {
    // Initialize styles
    Style.init()

    // Run tests if requested
    if (shouldRunTests) {
      dom.console.log("Running tests - check console for results")
      TestMain.main(args)
    }

    val containerNode = dom.document.querySelector("#app")
    render(containerNode, app)
  }

  /** Custom owner class for managing router lifecycle */
  class RouterOwner extends L.Owner {
    override def killSubscriptions(): Unit = {
      // Clean up any subscriptions when the owner is killed
      super.killSubscriptions()
    }
  }

  /** Global router owner instance */
  val routerOwner = new RouterOwner()

  /** Main application component
    *
    * Creates the main application structure with the navigation menu and
    * content container for the current page.
    *
    * @return
    *   A Div element containing the full application UI
    */
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

  /** Legacy user management UI (kept for reference)
    *
    * An older implementation of the user management UI that uses GraphQL. This
    * is kept for reference purposes only and is not used in the current
    * application.
    *
    * @return
    *   A Div element containing the user management UI
    */
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
}
