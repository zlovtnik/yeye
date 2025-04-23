package com.yeye.frontend

import com.yeye.shared.User
import zio.json.*
import utest.{TestSuite, Tests, test}
import scala.scalajs.js
import scala.scalajs.js.DynamicImplicits.truthValue
import com.yeye.frontend.Main
import org.scalajs.dom
import scala.concurrent.Future

object GraphQLTest extends TestSuite {
  val tests = Tests {
    test("Users query validation") {
      val query = Main.usersQuery.trim()

      // Verify query structure
      assert(query.startsWith("query {"))
      assert(query.contains("users {"))
      assert(query.contains("id"))
      assert(query.contains("email"))
      assert(query.contains("firstName"))
      assert(query.contains("lastName"))
      assert(query.contains("status"))
      assert(query.contains("created"))
      assert(query.contains("lastUpdated"))
      assert(query.endsWith("}"))
    }

    test("Create user mutation validation") {
      val mutation = Main.createUserMutation.trim()

      // Verify mutation structure
      assert(mutation.startsWith("mutation CreateUser("))
      assert(mutation.contains("$email: String!"))
      assert(mutation.contains("$firstName: String!"))
      assert(mutation.contains("$lastName: String!"))
      assert(mutation.contains("$status: String!"))
      assert(mutation.contains("createUser(input: {"))
      assert(mutation.contains("email: $email"))
      assert(mutation.contains("firstName: $firstName"))
      assert(mutation.contains("lastName: $lastName"))
      assert(mutation.contains("status: $status"))
      assert(mutation.endsWith("}"))
    }

    test("Update user mutation validation") {
      val mutation = Main.updateUserMutation.trim()

      // Verify mutation structure
      assert(mutation.startsWith("mutation UpdateUser("))
      assert(mutation.contains("$id: String!"))
      assert(mutation.contains("$email: String!"))
      assert(mutation.contains("$firstName: String!"))
      assert(mutation.contains("$lastName: String!"))
      assert(mutation.contains("$status: String!"))
      assert(mutation.contains("updateUser(input: {"))
      assert(mutation.contains("id: $id"))
      assert(mutation.contains("user: {"))
      assert(mutation.contains("email: $email"))
      assert(mutation.contains("firstName: $firstName"))
      assert(mutation.contains("lastName: $lastName"))
      assert(mutation.contains("status: $status"))
      assert(mutation.endsWith("}"))
    }

    test("Delete user mutation validation") {
      val mutation = Main.deleteUserMutation.trim()

      // Verify mutation structure
      assert(mutation.startsWith("mutation DeleteUser("))
      assert(mutation.contains("$id: String!"))
      assert(mutation.contains("deleteUser(id: $id)"))
      assert(mutation.endsWith("}"))
    }

    test("Query variables formatting") {
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)

      // Test create user variables
      val createVariables = s"""{
        "email": ${testUser.email.toJson},
        "firstName": ${testUser.firstName.toJson},
        "lastName": ${testUser.lastName.toJson},
        "status": ${testUser.status.toJson}
      }"""

      assert(createVariables.contains(testUser.email))
      assert(createVariables.contains(testUser.firstName))
      assert(createVariables.contains(testUser.lastName))
      assert(createVariables.contains(testUser.status))

      // Test update user variables
      val updateVariables = s"""{
        "id": ${testUser.id.toJson},
        "email": ${testUser.email.toJson},
        "firstName": ${testUser.firstName.toJson},
        "lastName": ${testUser.lastName.toJson},
        "status": ${testUser.status.toJson}
      }"""

      assert(updateVariables.contains(testUser.id))
      assert(updateVariables.contains(testUser.email))
      assert(updateVariables.contains(testUser.firstName))
      assert(updateVariables.contains(testUser.lastName))
      assert(updateVariables.contains(testUser.status))

      // Test delete user variables
      val deleteVariables = s"""{"id": ${testUser.id.toJson}}"""
      assert(deleteVariables.contains(testUser.id))
    }

    test("Query response parsing") {
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)
      val response = s"""{"data": {"users": [${testUser.toJson}]}}"""

      // Test parsing of user list
      val parsedUsers = response.fromJson[List[User]].getOrElse(List.empty)
      assert(parsedUsers.length == 1)
      assert(parsedUsers.head == testUser)

      // Test parsing of single user
      val singleUserResponse =
        s"""{"data": {"createUser": ${testUser.toJson}}}"""
      val parsedUser = singleUserResponse.fromJson[User].getOrElse(User.empty)
      assert(parsedUser == testUser)
    }

    test("GraphQL error response parsing") {
      val errorResponse = """{
        "errors": [
          {
            "message": "Invalid input",
            "path": ["createUser"],
            "extensions": {
              "code": "BAD_USER_INPUT",
              "exception": {
                "stacktrace": ["..."]
              }
            }
          }
        ]
      }"""

      // Test parsing of error response
      val json = js.JSON.parse(errorResponse)
      assert(json.hasOwnProperty("errors"))
      assert(js.typeOf(json.errors) == "object")
      assert(js.typeOf(json.errors.length) == "number")
      assert(json.errors.length.asInstanceOf[Int] == 1)
      assert(js.typeOf(json.errors(0).message) == "string")
      assert(json.errors(0).message.asInstanceOf[String] == "Invalid input")
      assert(js.typeOf(json.errors(0).path) == "object")
      assert(json.errors(0).path(0).asInstanceOf[String] == "createUser")
      assert(js.typeOf(json.errors(0).extensions.code) == "string")
      assert(
        json.errors(0).extensions.code.asInstanceOf[String] == "BAD_USER_INPUT"
      )
    }

    test("Partial success response parsing") {
      val partialResponse = """{
        "data": {
          "users": [{"id": "1", "email": "test@example.com"}]
        },
        "errors": [
          {
            "message": "Failed to fetch some data",
            "path": ["users", 1]
          }
        ]
      }"""

      // Test parsing of partial success response
      val json = js.JSON.parse(partialResponse)
      assert(json.hasOwnProperty("data"))
      assert(json.hasOwnProperty("errors"))
      assert(js.typeOf(json.data.users) == "object")
      assert(js.typeOf(json.data.users.length) == "number")
      assert(json.data.users.length.asInstanceOf[Int] == 1)
      assert(js.typeOf(json.data.users(0).id) == "string")
      assert(json.data.users(0).id.asInstanceOf[String] == "1")
      assert(js.typeOf(json.data.users(0).email) == "string")
      assert(
        json.data.users(0).email.asInstanceOf[String] == "test@example.com"
      )
      assert(js.typeOf(json.errors) == "object")
      assert(js.typeOf(json.errors.length) == "number")
      assert(json.errors.length.asInstanceOf[Int] == 1)
      assert(js.typeOf(json.errors(0).message) == "string")
      assert(
        json
          .errors(0)
          .message
          .asInstanceOf[String] == "Failed to fetch some data"
      )
    }

    test("Invalid response format handling") {
      val invalidResponses = List(
        "", // Empty response
        "invalid json", // Invalid JSON
        "{}", // Empty object
        """{"data": null}""", // Null data
        """{"errors": null}""", // Null errors
        """{"data": {}, "errors": []}""" // Empty data and errors
      )

      invalidResponses.foreach { response =>
        val result = response.fromJson[List[User]].getOrElse(List.empty)
        assert(result.isEmpty)
      }
    }

    test("Response field validation") {
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)

      // Test with missing fields
      val incompleteResponse = s"""{
        "data": {
          "users": [{
            "id": ${testUser.id.toJson},
            "email": ${testUser.email.toJson}
            // Missing firstName, lastName, status, created, lastUpdated
          }]
        }
      }"""

      val parsedUsers =
        incompleteResponse.fromJson[List[User]].getOrElse(List.empty)
      assert(parsedUsers.isEmpty) // Should fail to parse due to missing fields

      // Test with extra fields
      val extraFieldsResponse = s"""{
        "data": {
          "users": [{
            "id": ${testUser.id.toJson},
            "email": ${testUser.email.toJson},
            "firstName": ${testUser.firstName.toJson},
            "lastName": ${testUser.lastName.toJson},
            "status": ${testUser.status.toJson},
            "created": ${testUser.created},
            "lastUpdated": ${testUser.lastUpdated},
            "extraField": "should be ignored"
          }]
        }
      }"""

      val parsedUsersWithExtra =
        extraFieldsResponse.fromJson[List[User]].getOrElse(List.empty)
      assert(parsedUsersWithExtra.length == 1)
      assert(
        parsedUsersWithExtra.head == testUser
      ) // Extra field should be ignored
    }

    test("Response type validation") {
      val testUser =
        User("1", "test@example.com", "John", "Doe", "active", 0, 0)

      // Test with wrong field types
      val wrongTypesResponse = s"""{
        "data": {
          "users": [{
            "id": 123,  // Should be string
            "email": ${testUser.email.toJson},
            "firstName": ${testUser.firstName.toJson},
            "lastName": ${testUser.lastName.toJson},
            "status": ${testUser.status.toJson},
            "created": "not a number",  // Should be number
            "lastUpdated": "not a number"  // Should be number
          }]
        }
      }"""

      val parsedUsers =
        wrongTypesResponse.fromJson[List[User]].getOrElse(List.empty)
      assert(parsedUsers.isEmpty) // Should fail to parse due to wrong types
    }

    test("GraphQL queries and mutations") {
      test("usersQuery") {
        val query = Main.usersQuery
        assert(query.contains("query"))
        assert(query.contains("users"))
      }

      test("createUserMutation") {
        val mutation = Main.createUserMutation
        assert(mutation.contains("mutation"))
        assert(mutation.contains("createUser"))
      }

      test("updateUserMutation") {
        val mutation = Main.updateUserMutation
        assert(mutation.contains("mutation"))
        assert(mutation.contains("updateUser"))
      }

      test("deleteUserMutation") {
        val mutation = Main.deleteUserMutation
        assert(mutation.contains("mutation"))
        assert(mutation.contains("deleteUser"))
      }

      test("executeGraphQL") {
        // Test successful query
        val result = Main.executeGraphQL("query { users { id name } }")
        assert(result.isInstanceOf[Future[String]])

        // Test error handling
        val errorResult = Main.executeGraphQL("invalid query")
        errorResult.failed.foreach { error =>
          assert(error.isInstanceOf[Exception])
          assert(error.getMessage.contains("GraphQL errors"))
        }

        // Test HTTP error handling
        val httpErrorResult =
          Main.executeGraphQL("query { users { id name } }", "invalid json")
        httpErrorResult.failed.foreach { error =>
          assert(error.isInstanceOf[Exception])
          assert(error.getMessage.contains("HTTP error"))
        }
      }
    }
  }
}
