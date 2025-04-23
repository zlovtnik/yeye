package com.yeye.backend.repository

import zio.*
import com.yeye.shared.User
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource

/** Repository trait for user data access operations.
  *
  * This trait defines the contract for all user-related database operations.
  */
trait UserRepository:
  /** Creates a new user in the database.
    *
    * @param user
    *   The user to create
    * @return
    *   The created user
    */
  def createUser(user: User): ZIO[DataSource, Throwable, User]

  /** Retrieves a user by their ID.
    *
    * @param id
    *   The ID of the user to retrieve
    * @return
    *   An optional user if found
    */
  def getUserById(id: String): ZIO[DataSource, Throwable, Option[User]]

  /** Retrieves all users from the database.
    *
    * @return
    *   A list of all users
    */
  def getUsers: ZIO[DataSource, Throwable, List[User]]

  /** Updates an existing user in the database.
    *
    * @param id
    *   The ID of the user to update
    * @param user
    *   The updated user data
    * @return
    *   An optional user if the update was successful
    */
  def updateUser(
      id: String,
      user: User
  ): ZIO[DataSource, Throwable, Option[User]]

  /** Deletes a user from the database.
    *
    * @param id
    *   The ID of the user to delete
    * @return
    *   true if the user was deleted, false otherwise
    */
  def deleteUser(id: String): ZIO[DataSource, Throwable, Boolean]

/** Companion object for UserRepository providing the live implementation.
  */
object UserRepository:
  /** Live implementation of UserRepository using Quill for database access.
    */
  val live: ULayer[UserRepository] =
    ZLayer.succeed(
      new UserRepository:
        import context.*

        /** Quill query schema for the users table.
          */
        inline def users = quote {
          querySchema[User]("OKTA_USERS")
        }

        override def createUser(user: User): ZIO[DataSource, Throwable, User] =
          run {
            quote {
              users.insertValue(lift(user))
            }
          }.map(_ => user)

        override def getUserById(
            id: String
        ): ZIO[DataSource, Throwable, Option[User]] =
          run {
            quote {
              users.filter(_.id == lift(id))
            }
          }.map(_.headOption)

        override def getUsers: ZIO[DataSource, Throwable, List[User]] =
          run(users)

        override def updateUser(
            id: String,
            user: User
        ): ZIO[DataSource, Throwable, Option[User]] =
          run {
            quote {
              users
                .filter(_.id == lift(id))
                .updateValue(lift(user))
            }
          }.map(count => if (count > 0) Some(user) else None)

        override def deleteUser(
            id: String
        ): ZIO[DataSource, Throwable, Boolean] =
          run {
            quote {
              users.filter(_.id == lift(id)).delete
            }
          }.map(_ > 0)
    )
