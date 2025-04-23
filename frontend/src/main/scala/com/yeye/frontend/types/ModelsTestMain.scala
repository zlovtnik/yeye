package com.yeye.frontend.types

/** Test runner for the models
  *
  * This class provides direct testing of the models without a test framework
  */
object ModelsTestMain {

  /** Test User model creation and properties */
  def testUser(): Boolean = {
    val user = User("123", "John Doe", "john@example.com", 30)

    // Check each property
    val idOk = user.id == "123"
    val nameOk = user.name == "John Doe"
    val emailOk = user.email == "john@example.com"
    val ageOk = user.age == 30

    // All properties should be correct
    idOk && nameOk && emailOk && ageOk
  }

  /** Test File model creation and properties */
  def testFile(): Boolean = {
    val file =
      File("456", "document.pdf", 1024L, "application/pdf", 1625097600000L)

    // Check each property
    val idOk = file.id == "456"
    val nameOk = file.name == "document.pdf"
    val sizeOk = file.size == 1024L
    val typeOk = file.fileType == "application/pdf"
    val dateOk = file.lastModified == 1625097600000L

    // All properties should be correct
    idOk && nameOk && sizeOk && typeOk && dateOk
  }

  /** Test CreateUserRequest model creation and properties */
  def testCreateUserRequest(): Boolean = {
    val request = CreateUserRequest("Jane Doe", "jane@example.com", 25)

    // Check each property
    val nameOk = request.name == "Jane Doe"
    val emailOk = request.email == "jane@example.com"
    val ageOk = request.age == 25

    // All properties should be correct
    nameOk && emailOk && ageOk
  }

  /** Run all model tests */
  def runAllTests(): Unit = {
    println("Running Models Tests:")

    // User model
    val userResult = testUser()
    println(s"  User model: ${if (userResult) "PASSED" else "FAILED"}")

    // File model
    val fileResult = testFile()
    println(s"  File model: ${if (fileResult) "PASSED" else "FAILED"}")

    // CreateUserRequest model
    val requestResult = testCreateUserRequest()
    println(
      s"  CreateUserRequest model: ${if (requestResult) "PASSED" else "FAILED"}"
    )

    // Overall result
    val allPassed = userResult && fileResult && requestResult
    println(s"Models tests: ${if (allPassed) "ALL PASSED" else "SOME FAILED"}")
  }

  /** Main entry point
    *
    * @param args
    *   Command line arguments (unused)
    */
  def main(args: Array[String]): Unit = {
    runAllTests()
  }
}
