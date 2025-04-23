package com.yeye.frontend.pages.users

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import scala.concurrent.ExecutionContext.Implicits.global
import com.yeye.frontend.core.Config

case class User(id: Long, name: String, email: String)

object UsersPage {

  def apply(): Element = {
    val users = Var(List.empty[User])
    val nameVar = Var("")
    val emailVar = Var("")
    val filterVar = Var("")
    val errorMessageVar = Var("")
    val isLoadingVar = Var(false)

    // Filtered users signal based on filterVar
    val filteredUsers = users.signal.combineWith(filterVar.signal).map {
      case (userList, filter) if filter.isEmpty => userList
      case (userList, filter) =>
        userList.filter(user =>
          user.name.toLowerCase.contains(filter.toLowerCase) ||
            user.email.toLowerCase.contains(filter.toLowerCase)
        )
    }

    def fetchUsers(): Unit = {
      isLoadingVar.set(true)
      errorMessageVar.set("")

      dom
        .fetch(Config.Endpoints.Users)
        .toFuture
        .flatMap(response => {
          if (response.ok) {
            response.text().toFuture.map(Right(_))
          } else {
            response
              .text()
              .toFuture
              .map(text => Left(s"Error ${response.status}: ${text}"))
          }
        })
        .foreach {
          case Right(responseText) =>
            decode[List[User]](responseText) match {
              case Right(userList) =>
                users.set(userList)
                errorMessageVar.set("")
              case Left(error) =>
                errorMessageVar.set(
                  s"Error decoding users: ${error.getMessage}"
                )
                dom.console.error(s"Error decoding users: ${error.getMessage}")
            }
          case Left(errorMsg) =>
            errorMessageVar.set(errorMsg)
            dom.console.error(errorMsg)

            // Set loading to false when complete
            isLoadingVar.set(false)
        }
    }

    def createUser(): Unit = {
      if (nameVar.now().trim.isEmpty || emailVar.now().trim.isEmpty) {
        errorMessageVar.set("Name and email are required")
        return
      }

      isLoadingVar.set(true)
      errorMessageVar.set("")
      val newUser = User(0, nameVar.now(), emailVar.now())

      dom
        .fetch(
          Config.Endpoints.Users,
          new dom.RequestInit {
            method = dom.HttpMethod.POST
            headers = new dom.Headers {
              append("Content-Type", "application/json")
            }
            body = newUser.asJson.noSpaces
          }
        )
        .toFuture
        .flatMap(response => {
          if (response.ok) {
            response.text().toFuture.map(Right(_))
          } else {
            response
              .text()
              .toFuture
              .map(text => Left(s"Error ${response.status}: ${text}"))
          }
        })
        .foreach {
          case Right(_) =>
            nameVar.set("")
            emailVar.set("")
            fetchUsers()
          case Left(errorMsg) =>
            errorMessageVar.set(errorMsg)
            dom.console.error(errorMsg)

            // Set loading to false when complete
            isLoadingVar.set(false)
        }
    }

    def deleteUser(id: Long): Unit = {
      isLoadingVar.set(true)
      errorMessageVar.set("")

      dom
        .fetch(
          Config.Endpoints.user(id),
          new dom.RequestInit {
            method = dom.HttpMethod.DELETE
          }
        )
        .toFuture
        .flatMap(response => {
          if (response.ok) {
            response.text().toFuture.map(Right(_))
          } else {
            response
              .text()
              .toFuture
              .map(text => Left(s"Error ${response.status}: ${text}"))
          }
        })
        .foreach {
          case Right(_) => fetchUsers()
          case Left(errorMsg) =>
            errorMessageVar.set(errorMsg)
            dom.console.error(errorMsg)

            // Set loading to false when complete
            isLoadingVar.set(false)
        }
    }

    div(
      h1("Users Management"),

      // Error message display
      child <-- errorMessageVar.signal.map(errorMsg =>
        if (errorMsg.isEmpty) emptyNode
        else div(cls := "error-message", errorMsg)
      ),

      // Loading indicator
      child <-- isLoadingVar.signal.map(isLoading =>
        if (isLoading) div(cls := "loading-spinner", "Loading...")
        else emptyNode
      ),

      // Filter section
      div(
        cls := "filter-section",
        h3("Filter"),
        input(
          placeholder := "Search by name or email",
          controlled(
            value <-- filterVar,
            onInput.mapToValue --> filterVar
          )
        )
      ),

      // Add user form
      div(
        cls := "add-user-form",
        h3("Add New User"),
        div(
          cls := "form-row",
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

      // Users table
      div(
        cls := "table-container",
        table(
          thead(
            tr(
              th("ID"),
              th("Name"),
              th("Email"),
              th("Actions")
            )
          ),
          tbody(
            children <-- filteredUsers.map { userList =>
              if (userList.isEmpty && !isLoadingVar.now()) {
                List(
                  tr(
                    td(
                      colSpan := 4,
                      "No users found"
                    )
                  )
                )
              } else {
                userList.map { user =>
                  tr(
                    td(user.id.toString),
                    td(user.name),
                    td(user.email),
                    td(
                      button(
                        cls := "secondary",
                        "Delete",
                        onClick --> (_ => deleteUser(user.id))
                      )
                    )
                  )
                }
              }
            }
          )
        )
      ),
      onMountCallback(_ => fetchUsers())
    )
  }
}
