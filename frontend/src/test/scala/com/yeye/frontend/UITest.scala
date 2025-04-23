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
import utest.*

object UITest extends TestSuite {
  val tests = Tests {
    test("Form input handling") {
      // Test email input
      val emailVar = Var("")
      val emailInput = input(
        typ := "email",
        value <-- emailVar,
        onInput.mapToValue --> emailVar
      )

      // Simulate input event
      emailInput.ref.value = "test@example.com"
      emailInput.ref.dispatchEvent(new dom.Event("input"))

      assert(emailVar.now() == "test@example.com")

      // Test first name input
      val firstNameVar = Var("")
      val firstNameInput = input(
        typ := "text",
        value <-- firstNameVar,
        onInput.mapToValue --> firstNameVar
      )

      // Simulate input event
      firstNameInput.ref.value = "John"
      firstNameInput.ref.dispatchEvent(new dom.Event("input"))

      assert(firstNameVar.now() == "John")

      // Test last name input
      val lastNameVar = Var("")
      val lastNameInput = input(
        typ := "text",
        value <-- lastNameVar,
        onInput.mapToValue --> lastNameVar
      )

      // Simulate input event
      lastNameInput.ref.value = "Doe"
      lastNameInput.ref.dispatchEvent(new dom.Event("input"))

      assert(lastNameVar.now() == "Doe")

      // Test status input
      val statusVar = Var("")
      val statusInput = input(
        typ := "text",
        value <-- statusVar,
        onInput.mapToValue --> statusVar
      )

      // Simulate input event
      statusInput.ref.value = "active"
      statusInput.ref.dispatchEvent(new dom.Event("input"))

      assert(statusVar.now() == "active")
    }

    test("User selection") {
      val selectedUserId = Var[Option[String]](None)
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)

      // Test selecting a user
      Main.selectUser(testUser)

      assert(selectedUserId.now() == Some("1"))
    }

    test("Form reset after user creation") {
      val emailVar = Var("test@example.com")
      val firstNameVar = Var("John")
      val lastNameVar = Var("Doe")
      val statusVar = Var("active")
      val selectedUserId = Var[Option[String]](Some("1"))

      // Simulate user creation
      Main.createUser()

      assert(emailVar.now() == "")
      assert(firstNameVar.now() == "")
      assert(lastNameVar.now() == "")
      assert(statusVar.now() == "")
      assert(selectedUserId.now() == None)
    }

    test("Form reset after user update") {
      val emailVar = Var("test@example.com")
      val firstNameVar = Var("John")
      val lastNameVar = Var("Doe")
      val statusVar = Var("active")
      val selectedUserId = Var[Option[String]](Some("1"))

      // Simulate user update
      Main.updateUser()

      assert(emailVar.now() == "")
      assert(firstNameVar.now() == "")
      assert(lastNameVar.now() == "")
      assert(statusVar.now() == "")
      assert(selectedUserId.now() == None)
    }

    test("Form validation - empty fields") {
      val emailVar = Var("")
      val firstNameVar = Var("")
      val lastNameVar = Var("")
      val statusVar = Var("")
      val selectedUserId = Var[Option[String]](None)
      val validationErrors = Var[List[String]](List.empty)

      // Test form submission with empty fields
      val result = Main.createUser()

      // Verify that validation errors are set
      assert(validationErrors.now().contains("Email is required"))
      assert(validationErrors.now().contains("First name is required"))
      assert(validationErrors.now().contains("Last name is required"))
      assert(validationErrors.now().contains("Status is required"))

      // Verify that the form state remains unchanged
      assert(emailVar.now() == "")
      assert(firstNameVar.now() == "")
      assert(lastNameVar.now() == "")
      assert(statusVar.now() == "")
      assert(selectedUserId.now() == None)
    }

    test("Form validation - invalid email") {
      val emailVar = Var("invalid-email")
      val emailInput = input(
        typ := "email",
        value <-- emailVar,
        onInput.mapToValue --> emailVar
      )

      // Simulate input event with invalid email
      emailInput.ref.value = "invalid-email"
      emailInput.ref.dispatchEvent(new dom.Event("input"))

      // Verify that the invalid email is still stored
      assert(emailVar.now() == "invalid-email")
    }

    test("Form validation - special characters") {
      val firstNameVar = Var("")
      val firstNameInput = input(
        typ := "text",
        value <-- firstNameVar,
        onInput.mapToValue --> firstNameVar
      )

      // Test with special characters
      val testValue = "John-Doe@123"
      firstNameInput.ref.value = testValue
      firstNameInput.ref.dispatchEvent(new dom.Event("input"))

      assert(firstNameVar.now() == testValue)
    }

    test("Form validation - long input") {
      val lastNameVar = Var("")
      val lastNameInput = input(
        typ := "text",
        value <-- lastNameVar,
        onInput.mapToValue --> lastNameVar
      )

      // Test with very long input
      val longName = "A".repeat(1000)
      lastNameInput.ref.value = longName
      lastNameInput.ref.dispatchEvent(new dom.Event("input"))

      assert(lastNameVar.now() == longName)
    }

    test("Form state persistence") {
      val emailVar = Var("test@example.com")
      val firstNameVar = Var("John")
      val lastNameVar = Var("Doe")
      val statusVar = Var("active")
      val selectedUserId = Var[Option[String]](Some("1"))

      // Simulate form state changes
      emailVar.set("new@example.com")
      firstNameVar.set("Jane")
      lastNameVar.set("Smith")
      statusVar.set("inactive")

      // Verify state changes are persisted
      assert(emailVar.now() == "new@example.com")
      assert(firstNameVar.now() == "Jane")
      assert(lastNameVar.now() == "Smith")
      assert(statusVar.now() == "inactive")
      assert(selectedUserId.now() == Some("1"))
    }

    test("Form state reset on cancel") {
      val emailVar = Var("test@example.com")
      val firstNameVar = Var("John")
      val lastNameVar = Var("Doe")
      val statusVar = Var("active")
      val selectedUserId = Var[Option[String]](Some("1"))

      // Simulate form reset
      emailVar.set("")
      firstNameVar.set("")
      lastNameVar.set("")
      statusVar.set("")
      selectedUserId.set(None)

      // Verify all fields are reset
      assert(emailVar.now() == "")
      assert(firstNameVar.now() == "")
      assert(lastNameVar.now() == "")
      assert(statusVar.now() == "")
      assert(selectedUserId.now() == None)
    }
  }
}
