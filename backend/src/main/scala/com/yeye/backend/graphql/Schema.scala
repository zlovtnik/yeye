package com.yeye.backend.graphql

import caliban.schema.{GenericSchema, Schema, ArgBuilder}
import caliban.{RootResolver, graphQL}
import com.yeye.shared.User
import com.yeye.backend.repository.UserRepository
import zio.*
import javax.sql.DataSource
import java.lang.System

/** GraphQL schema definition for the application.
  *
  * This schema defines:
  *   - Queries for fetching user data
  *   - Mutations for modifying user data
  *   - Input types for mutations
  *   - Schema instances for all types
  */
object Schema extends GenericSchema[UserRepository & DataSource] {

  /** GraphQL queries available in the API.
    *
    * @param users
    *   Fetches all users
    * @param userById
    *   Fetches a specific user by ID
    */
  case class Queries(
      users: ZIO[UserRepository & DataSource, Throwable, List[User]],
      userById: String => ZIO[UserRepository & DataSource, Throwable, Option[
        User
      ]]
  )

  /** Input type for creating a new user.
    *
    * @param email
    *   User's email address
    * @param firstName
    *   User's first name
    * @param lastName
    *   User's last name
    * @param status
    *   User's status
    */
  case class CreateUserInput(
      email: String,
      firstName: String,
      lastName: String,
      status: String
  )

  /** Input type for updating an existing user.
    *
    * @param id
    *   ID of the user to update
    * @param user
    *   Updated user data
    */
  case class UpdateUserInput(id: String, user: User)

  /** GraphQL mutations available in the API.
    *
    * @param createUser
    *   Creates a new user
    * @param updateUser
    *   Updates an existing user
    * @param deleteUser
    *   Deletes a user by ID
    */
  case class Mutations(
      createUser: CreateUserInput => ZIO[
        UserRepository & DataSource,
        Throwable,
        User
      ],
      updateUser: UpdateUserInput => ZIO[
        UserRepository & DataSource,
        Throwable,
        Option[User]
      ],
      deleteUser: String => ZIO[UserRepository & DataSource, Throwable, Boolean]
  )

  /** Implementation of GraphQL queries. Maps to corresponding repository
    * methods.
    */
  private val queries = Queries(
    users = ZIO.serviceWithZIO[UserRepository](_.getUsers),
    userById = id => ZIO.serviceWithZIO[UserRepository](_.getUserById(id))
  )

  /** Implementation of GraphQL mutations. Maps to corresponding repository
    * methods.
    */
  private val mutations = Mutations(
    createUser = input =>
      ZIO.serviceWithZIO[UserRepository](
        _.createUser(
          User(
            id = java.util.UUID.randomUUID().toString,
            email = input.email,
            firstName = input.firstName,
            lastName = input.lastName,
            status = input.status,
            created = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
          )
        )
      ),
    updateUser = input =>
      ZIO.serviceWithZIO[UserRepository](_.updateUser(input.id, input.user)),
    deleteUser = id => ZIO.serviceWithZIO[UserRepository](_.deleteUser(id))
  )

  // Provide explicit schema instances
  /** Schema instance for Queries type */
  given Schema[UserRepository & DataSource, Queries] =
    Schema.gen[UserRepository & DataSource, Queries]

  /** Schema instance for Mutations type */
  given Schema[UserRepository & DataSource, Mutations] =
    Schema.gen[UserRepository & DataSource, Mutations]

  /** Schema instance for User type */
  given Schema[UserRepository & DataSource, User] =
    Schema.gen[UserRepository & DataSource, User]

  /** Schema instance for CreateUserInput type */
  given Schema[UserRepository & DataSource, CreateUserInput] =
    Schema.gen[UserRepository & DataSource, CreateUserInput]

  /** Schema instance for UpdateUserInput type */
  given Schema[UserRepository & DataSource, UpdateUserInput] =
    Schema.gen[UserRepository & DataSource, UpdateUserInput]

  /** Argument builder for CreateUserInput type */
  given ArgBuilder[CreateUserInput] = ArgBuilder.gen[CreateUserInput]

  /** Argument builder for UpdateUserInput type */
  given ArgBuilder[UpdateUserInput] = ArgBuilder.gen[UpdateUserInput]

  /** Argument builder for User type */
  given ArgBuilder[User] = ArgBuilder.gen[User]

  /** The final GraphQL API combining queries and mutations.
    */
  val api = graphQL(RootResolver(queries, mutations))
}
