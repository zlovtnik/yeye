package com.yeye.backend.graphql

import caliban.schema.{GenericSchema, Schema, ArgBuilder}
import caliban.{RootResolver, graphQL}
import com.yeye.shared.User
import com.yeye.backend.repository.UserRepository
import zio.*
import javax.sql.DataSource
import java.lang.System

object Schema extends GenericSchema[UserRepository & DataSource] {
  case class Queries(
      users: ZIO[UserRepository & DataSource, Throwable, List[User]],
      userById: String => ZIO[UserRepository & DataSource, Throwable, Option[
        User
      ]]
  )

  case class CreateUserInput(
      email: String,
      firstName: String,
      lastName: String,
      status: String
  )

  case class UpdateUserInput(id: String, user: User)

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

  private val queries = Queries(
    users = ZIO.serviceWithZIO[UserRepository](_.getUsers),
    userById = id => ZIO.serviceWithZIO[UserRepository](_.getUserById(id))
  )

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
  given Schema[UserRepository & DataSource, Queries] =
    Schema.gen[UserRepository & DataSource, Queries]
  given Schema[UserRepository & DataSource, Mutations] =
    Schema.gen[UserRepository & DataSource, Mutations]
  given Schema[UserRepository & DataSource, User] =
    Schema.gen[UserRepository & DataSource, User]
  given Schema[UserRepository & DataSource, CreateUserInput] =
    Schema.gen[UserRepository & DataSource, CreateUserInput]
  given Schema[UserRepository & DataSource, UpdateUserInput] =
    Schema.gen[UserRepository & DataSource, UpdateUserInput]
  given ArgBuilder[CreateUserInput] = ArgBuilder.gen[CreateUserInput]
  given ArgBuilder[UpdateUserInput] = ArgBuilder.gen[UpdateUserInput]
  given ArgBuilder[User] = ArgBuilder.gen[User]

  val api = graphQL(RootResolver(queries, mutations))
}
