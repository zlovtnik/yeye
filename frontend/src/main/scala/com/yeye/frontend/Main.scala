package com.yeye.frontend

import com.raquo.laminar.api.L.*
import com.yeye.shared.User
import zio.json.*
import org.scalajs.dom
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import org.scalajs.dom.HttpMethod
import scalajs.js.DynamicImplicits.truthValue

object Main:
  def main(args: Array[String]): Unit =
    // State
    val emailVar = Var("")
    val firstNameVar = Var("")
    val lastNameVar = Var("")
    val statusVar = Var("")
    val selectedUserId = Var[Option[String]](None)

    // GraphQL queries
    val usersQuery = """
      query {
        users {
          id
          email
          firstName
          lastName
          status
          created
          lastUpdated
        }
      }
    """

    val createUserMutation = """
      mutation CreateUser($email: String!, $firstName: String!, $lastName: String!, $status: String!) {
        createUser(input: { email: $email, firstName: $firstName, lastName: $lastName, status: $status }) {
          id
          email
          firstName
          lastName
          status
          created
          lastUpdated
        }
      }
    """

    val updateUserMutation = """
      mutation UpdateUser($id: String!, $email: String!, $firstName: String!, $lastName: String!, $status: String!) {
        updateUser(input: { id: $id, user: { id: $id, email: $email, firstName: $firstName, lastName: $lastName, status: $status, created: 0, lastUpdated: 0 } }) {
          id
          email
          firstName
          lastName
          status
          created
          lastUpdated
        }
      }
    """

    val deleteUserMutation = """
      mutation DeleteUser($id: String!) {
        deleteUser(id: $id)
      }
    """

    def executeGraphQL(
        query: String,
        variables: String = "{}"
    ): Future[String] =
      dom
        .fetch(
          "/api/graphql",
          new dom.RequestInit {
            method = HttpMethod.POST
            headers = js.Dictionary(
              "Content-Type" -> "application/json",
              "Accept" -> "application/json"
            )
            body = s"""{"query": ${query.toJson}, "variables": $variables}"""
          }
        )
        .flatMap(_.text())
        .map { response =>
          // Parse the response to handle any GraphQL errors
          val json = js.JSON.parse(response)
          if (json.hasOwnProperty("errors")) {
            throw new Exception(
              s"GraphQL Error: ${js.JSON.stringify(json.errors)}"
            )
          }
          response
        }

    def createUser(): Unit =
      val variables = s"""{
        "email": ${emailVar.now().toJson},
        "firstName": ${firstNameVar.now().toJson},
        "lastName": ${lastNameVar.now().toJson},
        "status": ${statusVar.now().toJson}
      }"""
      executeGraphQL(createUserMutation, variables)
        .foreach { _ =>
          emailVar.set("")
          firstNameVar.set("")
          lastNameVar.set("")
          statusVar.set("")
          selectedUserId.set(None)
        }

    def updateUser(): Unit =
      selectedUserId.now().foreach { id =>
        val variables = s"""{
          "id": ${id.toJson},
          "email": ${emailVar.now().toJson},
          "firstName": ${firstNameVar.now().toJson},
          "lastName": ${lastNameVar.now().toJson},
          "status": ${statusVar.now().toJson}
        }"""
        executeGraphQL(updateUserMutation, variables)
          .foreach { _ =>
            emailVar.set("")
            firstNameVar.set("")
            lastNameVar.set("")
            statusVar.set("")
            selectedUserId.set(None)
          }
      }

    def deleteUser(id: String): Unit =
      val variables = s"""{"id": ${id.toJson}}"""
      executeGraphQL(deleteUserMutation, variables)

    def selectUser(user: User): Unit =
      emailVar.set(user.email)
      firstNameVar.set(user.firstName)
      lastNameVar.set(user.lastName)
      statusVar.set(user.status)
      selectedUserId.set(Some(user.id))

    val app = div(
      cls := "app-container",
      // Sidebar
      div(
        cls := "sidebar",
        div(
          cls := "sidebar-header",
          h1("Menu")
        ),
        ul(
          cls := "sidebar-menu",
          li(
            cls := "active",
            "Users"
          )
        )
      ),
      // Main content
      div(
        cls := "main-content",
        child <-- EventStream
          .fromFuture(executeGraphQL(usersQuery))
          .map(_.fromJson[List[User]].getOrElse(List.empty))
          .map(users =>
            div(
              h1("User Management"),
              div(
                cls := "user-form",
                div(
                  cls := "form-group",
                  label("Email"),
                  input(
                    typ := "email",
                    placeholder := "Enter email",
                    value <-- emailVar,
                    onInput.mapToValue --> emailVar
                  )
                ),
                div(
                  cls := "form-group",
                  label("First Name"),
                  input(
                    typ := "text",
                    placeholder := "Enter first name",
                    value <-- firstNameVar,
                    onInput.mapToValue --> firstNameVar
                  )
                ),
                div(
                  cls := "form-group",
                  label("Last Name"),
                  input(
                    typ := "text",
                    placeholder := "Enter last name",
                    value <-- lastNameVar,
                    onInput.mapToValue --> lastNameVar
                  )
                ),
                div(
                  cls := "form-group",
                  label("Status"),
                  input(
                    typ := "text",
                    placeholder := "Enter status",
                    value <-- statusVar,
                    onInput.mapToValue --> statusVar
                  )
                ),
                div(
                  cls := "form-actions",
                  button(
                    cls := "btn-primary",
                    "Create User",
                    onClick --> { _ => createUser() }
                  ),
                  button(
                    cls := "btn-secondary",
                    "Edit User",
                    disabled <-- selectedUserId.signal.map(_.isEmpty),
                    onClick --> { _ => updateUser() }
                  ),
                  button(
                    cls := "btn-danger",
                    "Delete User",
                    disabled <-- selectedUserId.signal.map(_.isEmpty),
                    onClick --> { _ =>
                      selectedUserId.now().foreach(deleteUser)
                      emailVar.set("")
                      firstNameVar.set("")
                      lastNameVar.set("")
                      statusVar.set("")
                      selectedUserId.set(None)
                    }
                  )
                )
              ),
              div(
                cls := "user-list",
                users.map(user =>
                  div(
                    cls := "user-item",
                    h3(s"${user.firstName} ${user.lastName}"),
                    p(s"Email: ${user.email}"),
                    p(s"Status: ${user.status}"),
                    onClick --> { _ => selectUser(user) }
                  )
                )
              )
            )
          )
      )
    )

    render(dom.document.querySelector("#app"), app)
