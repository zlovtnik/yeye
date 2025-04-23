package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.frontend.api.ApiClient
import com.yeye.frontend.types.{CreateUserRequest, User}
import io.circe.parser.decode
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

object UserForm {
  case class Props(
      usersVar: Var[List[User]],
      errorVar: Var[Option[String]]
  )

  def render(props: Props): HtmlElement = {
    val nameVar = Var("")
    val emailVar = Var("")
    val ageVar = Var("")

    val $name = nameVar.signal
    val $email = emailVar.signal
    val $age = ageVar.signal

    val $isValid =
      Signal.combine($name, $email, $age).map { case (name, email, age) =>
        name.nonEmpty && email.nonEmpty && age.nonEmpty && age.forall(_.isDigit)
      }

    div(
      cls := "user-form",
      h2("Create User"),
      div(
        cls := "form-group",
        label("Name:"),
        input(
          typ := "text",
          value <-- $name,
          onInput.mapToValue --> nameVar
        )
      ),
      div(
        cls := "form-group",
        label("Email:"),
        input(
          typ := "email",
          value <-- $email,
          onInput.mapToValue --> emailVar
        )
      ),
      div(
        cls := "form-group",
        label("Age:"),
        input(
          typ := "number",
          value <-- $age,
          onInput.mapToValue --> ageVar
        )
      ),
      button(
        "Create",
        disabled <-- $isValid.map(!_),
        onClick --> { _ =>
          val request = CreateUserRequest(
            name = nameVar.now(),
            email = emailVar.now(),
            age = ageVar.now().toInt
          )

          ApiClient.createUser(request).foreach { user =>
            props.usersVar.update(_ :+ user)
            nameVar.set("")
            emailVar.set("")
            ageVar.set("")
          }
        }
      )
    )
  }
}
