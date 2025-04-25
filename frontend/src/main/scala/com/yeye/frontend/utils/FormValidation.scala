package com.yeye.frontend.utils

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.implicits._

/** Form validation utilities using Cats Validated
  */
object FormValidation {
  // Define validation error type
  sealed trait ValidationError
  case class EmptyField(fieldName: String) extends ValidationError
  case class InvalidFormat(fieldName: String, message: String)
      extends ValidationError
  case class InvalidValue(fieldName: String, message: String)
      extends ValidationError

  // Type alias for validated result
  type ValidationResult[A] = ValidatedNel[ValidationError, A]

  // Basic validation functions
  def notEmpty(fieldName: String, value: String): ValidationResult[String] =
    if (value.trim.isEmpty)
      Validated.invalidNel(EmptyField(fieldName))
    else
      Validated.validNel(value)

  def validEmail(fieldName: String, value: String): ValidationResult[String] = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    if (emailRegex.matches(value))
      Validated.validNel(value)
    else
      Validated.invalidNel(InvalidFormat(fieldName, "Invalid email format"))
  }

  def positiveInt(fieldName: String, value: String): ValidationResult[Int] = {
    try {
      val intValue = value.toInt
      if (intValue <= 0)
        Validated.invalidNel(InvalidValue(fieldName, "Value must be positive"))
      else
        Validated.validNel(intValue)
    } catch {
      case _: NumberFormatException =>
        Validated.invalidNel(InvalidFormat(fieldName, "Must be a number"))
    }
  }

  // Format error messages from validation results
  def formatErrors(errors: NonEmptyList[ValidationError]): List[String] = {
    errors.map {
      case EmptyField(fieldName)             => s"$fieldName cannot be empty"
      case InvalidFormat(fieldName, message) => s"$fieldName: $message"
      case InvalidValue(fieldName, message)  => s"$fieldName: $message"
    }.toList
  }
}
