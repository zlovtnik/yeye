package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.shared.User
import com.yeye.frontend.services.ApiService
import zio.json.*
import scala.concurrent.ExecutionContext.Implicits.global

object UserForm:
  case class Props(
      emailVar: Var[String],
      firstNameVar: Var[String],
      lastNameVar: Var[String],
      statusVar: Var[String],
      selectedUserId: Var[Option[String]],
      onUserCreated: () => Unit = () => (),
      onUserUpdated: () => Unit = () => (),
      onUserDeleted: () => Unit = () => ()
  )

  def apply(props: Props): HtmlElement =
    div(
      cls := "user-form",
      div(
        cls := "form-group",
        label("Email"),
        input(
          typ := "email",
          placeholder := "Enter email",
          value <-- props.emailVar,
          onInput.mapToValue --> props.emailVar
        )
      ),
      div(
        cls := "form-group",
        label("First Name"),
        input(
          typ := "text",
          placeholder := "Enter first name",
          value <-- props.firstNameVar,
          onInput.mapToValue --> props.firstNameVar
        )
      ),
      div(
        cls := "form-group",
        label("Last Name"),
        input(
          typ := "text",
          placeholder := "Enter last name",
          value <-- props.lastNameVar,
          onInput.mapToValue --> props.lastNameVar
        )
      ),
      div(
        cls := "form-group",
        label("Status"),
        input(
          typ := "text",
          placeholder := "Enter status",
          value <-- props.statusVar,
          onInput.mapToValue --> props.statusVar
        )
      ),
      div(
        cls := "form-actions",
        button(
          cls := "btn-primary",
          "Create User",
          onClick --> { _ => createUser(props) }
        ),
        button(
          cls := "btn-secondary",
          "Edit User",
          disabled <-- props.selectedUserId.signal.map(_.isEmpty),
          onClick --> { _ => updateUser(props) }
        ),
        button(
          cls := "btn-danger",
          "Delete User",
          disabled <-- props.selectedUserId.signal.map(_.isEmpty),
          onClick --> { _ =>
            props.selectedUserId.now().foreach(id => deleteUser(props, id))
            resetForm(props)
          }
        )
      )
    )

  private def createUser(props: Props): Unit =
    val variables = s"""{
      "email": ${props.emailVar.now().toJson},
      "firstName": ${props.firstNameVar.now().toJson},
      "lastName": ${props.lastNameVar.now().toJson},
      "status": ${props.statusVar.now().toJson}
    }"""
    ApiService
      .executeGraphQL(ApiService.createUserMutation, variables)
      .foreach { _ =>
        resetForm(props)
        props.onUserCreated()
      }

  private def updateUser(props: Props): Unit =
    props.selectedUserId.now().foreach { id =>
      val variables = s"""{
        "id": ${id.toJson},
        "email": ${props.emailVar.now().toJson},
        "firstName": ${props.firstNameVar.now().toJson},
        "lastName": ${props.lastNameVar.now().toJson},
        "status": ${props.statusVar.now().toJson}
      }"""
      ApiService
        .executeGraphQL(ApiService.updateUserMutation, variables)
        .foreach { _ =>
          resetForm(props)
          props.onUserUpdated()
        }
    }

  private def deleteUser(props: Props, id: String): Unit =
    val variables = s"""{"id": ${id.toJson}}"""
    ApiService
      .executeGraphQL(ApiService.deleteUserMutation, variables)
      .foreach { _ =>
        resetForm(props)
        props.onUserDeleted()
      }

  private def resetForm(props: Props): Unit =
    props.emailVar.set("")
    props.firstNameVar.set("")
    props.lastNameVar.set("")
    props.statusVar.set("")
    props.selectedUserId.set(None)
