package com.yeye.frontend.examples

import cats._
import cats.data._
import cats.implicits._

/** Examples of using Cats functional programming constructs
  */
object CatsExample {

  // --- Functor examples ---

  // Transforming values in a context with map
  def functorExamples(): Unit = {
    // Option as a functor
    val maybeInt: Option[Int] = Some(5)
    val maybeString: Option[String] = maybeInt.map(i => s"Value is $i")

    // List as a functor
    val numbers = List(1, 2, 3, 4)
    val doubles = numbers.map(_ * 2)

    // Either as a functor
    val right: Either[String, Int] = Right(42)
    val transformed: Either[String, String] = right.map(n => s"Success: $n")
  }

  // --- Applicative examples ---

  // Combining independent values
  def applicativeExamples(): Unit = {
    case class User(name: String, age: Int, email: String)

    // Validation using Validated
    def validateName(name: String): ValidatedNel[String, String] =
      if (name.nonEmpty) name.validNel
      else "Name cannot be empty".invalidNel

    def validateAge(age: Int): ValidatedNel[String, Int] =
      if (age >= 18) age.validNel
      else "Age must be at least 18".invalidNel

    def validateEmail(email: String): ValidatedNel[String, String] =
      if (email.contains("@")) email.validNel
      else "Email must contain @".invalidNel

    // Using applicative to combine validations
    def validateUser(
        name: String,
        age: Int,
        email: String
    ): ValidatedNel[String, User] =
      (validateName(name), validateAge(age), validateEmail(email)).mapN(
        User.apply
      )

    // Examples
    val validUser = validateUser("John", 25, "john@example.com")
    val invalidUser = validateUser("", 15, "not-an-email")
  }

  // --- Monad examples ---

  // Sequential operations that depend on previous results
  def monadExamples(): Unit = {
    // Option monad for handling missing values
    def findUser(id: Int): Option[String] =
      if (id > 0) Some(s"user$id") else None

    def findRole(user: String): Option[String] =
      if (user.startsWith("user")) Some("admin") else None

    def findPermissions(role: String): Option[List[String]] =
      if (role == "admin") Some(List("read", "write", "delete")) else None

    // Using flatMap for sequential operations
    val permissions = findUser(123).flatMap(user =>
      findRole(user).flatMap(role => findPermissions(role))
    )

    // Same thing with for-comprehension
    val permissionsFor = for {
      user <- findUser(123)
      role <- findRole(user)
      perms <- findPermissions(role)
    } yield perms
  }

  // --- MonadError examples ---

  // Working with effects that can fail
  def monadErrorExamples(): Unit = {
    // Using Either for error handling
    type Result[A] = Either[String, A]

    def divide(a: Int, b: Int): Result[Int] =
      if (b == 0) Left("Division by zero")
      else Right(a / b)

    def addOne(n: Int): Result[Int] = Right(n + 1)

    // Combining operations that might fail
    val result = for {
      div <- divide(10, 2)
      res <- addOne(div)
    } yield res

    val error = for {
      div <- divide(10, 0)
      res <- addOne(div)
    } yield res
  }

  // --- State monad examples ---

  // Managing state in a functional way
  def stateMonadExamples(): Unit = {
    // State represents a function S => (S, A) where S is the state type and A is the result type
    def nextInt: State[Int, Int] = State(s => (s + 1, s))

    // Define a sequence of state transitions
    val program = for {
      n1 <- nextInt
      n2 <- nextInt
      n3 <- nextInt
    } yield (n1, n2, n3)

    // Run the program with initial state
    val (finalState, result) = program.run(0).value
  }

  // --- Traverse examples ---

  // Working with effects and collections
  def traverseExamples(): Unit = {
    // Parse a list of strings into integers
    def parseInt(s: String): Option[Int] = s.toIntOption

    val strings = List("1", "2", "3")

    // Using traverse to apply the effect to each element
    val maybeInts: Option[List[Int]] = strings.traverse(parseInt)

    // This fails because one element is not a number
    val badStrings = List("1", "foo", "3")
    val failedResult: Option[List[Int]] = badStrings.traverse(parseInt)
  }
}
