package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.frontend.api.ApiClient
import com.yeye.frontend.types.{CreateUserRequest, User}
import io.circe.parser.decode
import org.scalajs.dom
import cats.data.{Validated, NonEmptyList}
import cats.implicits.*
import com.yeye.frontend.utils.FormValidation
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
    val validationErrors = Var(List.empty[String])

    val $name = nameVar.signal
    val $email = emailVar.signal
    val $age = ageVar.signal

    // Create a derived signal that validates the form
    val $validationResult =
      Signal.combine($name, $email, $age).map { case (name, email, age) =>
        val nameValidation = FormValidation.notEmpty("Name", name)
        val emailValidation = FormValidation.validEmail("Email", email)
        val ageValidation = FormValidation.positiveInt("Age", age)

        // Use Cats applicative to combine validations
        (nameValidation, emailValidation, ageValidation).tupled
      }

    div(
      cls := "user-form",
      h2("Create User"),

      // Update validation errors based on validation result
      $validationResult --> { validation =>
        validation match {
          case Validated.Invalid(errs) =>
            validationErrors.set(FormValidation.formatErrors(errs))
          case Validated.Valid(_) =>
            validationErrors.set(List.empty)
        }
      },

      // Display validation errors
      div(
        cls := "validation-errors",
        children <-- validationErrors.signal.map { errors =>
          errors.map(error => div(cls := "error", error))
        }
      ),
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
        disabled <-- $validationResult.map(!_.isValid),
        onClick --> { _ =>
          val name = nameVar.now()
          val email = emailVar.now()
          val age = ageVar.now()

          val nameValidation = FormValidation.notEmpty("Name", name)
          val emailValidation = FormValidation.validEmail("Email", email)
          val ageValidation = FormValidation.positiveInt("Age", age)

          val validation =
            (nameValidation, emailValidation, ageValidation).tupled

          validation match {
            case Validated.Valid((validName, validEmail, validAge)) =>
              val request = CreateUserRequest(validName, validEmail, validAge)
              ApiClient.createUser(request).foreach { user =>
                props.usersVar.update(_ :+ user)
                nameVar.set("")
                emailVar.set("")
                ageVar.set("")
                props.errorVar.set(None)
              }
            case Validated.Invalid(errs) =>
              props.errorVar.set(
                Some(FormValidation.formatErrors(errs).mkString(", "))
              )
          }
        }
      )
    )
  }
}
