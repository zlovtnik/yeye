package com.yeye.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: Long, name: String, email: String)

/** Main entry point for the frontend application.
  *
  * @param args
  *   Command line arguments (unused)
  */
object Main:
  def main(args: Array[String]): Unit =
    val containerNode = dom.document.querySelector("#app")
    render(containerNode, app)

  def app: Div = {
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
      cls := "container mx-auto p-4",
      h1(
        cls := "text-3xl font-bold mb-4",
        "User Management"
      ),
      div(
        cls := "mb-4",
        div(
          cls := "flex gap-2",
          input(
            cls := "border p-2 rounded",
            placeholder := "Name",
            controlled(
              value <-- nameVar,
              onInput.mapToValue --> nameVar
            )
          ),
          input(
            cls := "border p-2 rounded",
            placeholder := "Email",
            controlled(
              value <-- emailVar,
              onInput.mapToValue --> emailVar
            )
          ),
          button(
            cls := "bg-blue-500 text-white px-4 py-2 rounded",
            "Add User",
            onClick --> (_ => createUser())
          )
        )
      ),
      div(
        cls := "grid grid-cols-1 gap-4",
        children <-- users.signal.map { userList =>
          userList.map { user =>
            div(
              cls := "border p-4 rounded",
              div(
                cls := "font-bold",
                user.name
              ),
              div(
                cls := "text-gray-600",
                user.email
              )
            )
          }
        }
      ),
      onMountCallback(_ => fetchUsers())
    )
  }
