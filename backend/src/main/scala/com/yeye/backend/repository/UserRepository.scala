package com.yeye.backend.repository

import cats.effect.IO
import com.yeye.backend.model.User
import javax.sql.DataSource
import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.util.Try

/** Repository trait for user data access operations.
  *
  * This trait defines the contract for all user-related database operations.
  */
trait UserRepository {

  /** Retrieves a user by their ID.
    *
    * @param id
    *   The ID of the user to retrieve
    * @return
    *   An optional user if found
    */
  def findById(id: Long): IO[Option[User]]

  /** Retrieves all users from the database.
    *
    * @return
    *   A list of all users
    */
  def findAll: IO[List[User]]

  /** Creates a new user in the database.
    *
    * @param user
    *   The user to create
    * @return
    *   The created user
    */
  def create(user: User): IO[User]

  /** Updates an existing user in the database.
    *
    * @param user
    *   The updated user data
    * @return
    *   The updated user
    */
  def update(user: User): IO[User]

  /** Deletes a user from the database.
    *
    * @param id
    *   The ID of the user to delete
    * @return
    *   Unit if the user was deleted
    */
  def delete(id: Long): IO[Unit]
}

/** Companion object for UserRepository providing the live implementation.
  */
object UserRepository {

  /** Live implementation of UserRepository using plain JDBC with Cats.
    */
  def live(dataSource: DataSource): UserRepository =
    new UserRepository {
      private def withConnection[A](f: Connection => A): IO[A] = {
        IO.fromTry(Try {
          val conn = dataSource.getConnection
          try f(conn)
          finally conn.close()
        })
      }

      private def executeQuery[A](sql: String, params: List[Any] = Nil)(
          f: ResultSet => A
      ): IO[A] = {
        withConnection { conn =>
          val stmt = conn.prepareStatement(sql)
          try {
            params.zipWithIndex.foreach { case (param, idx) =>
              stmt.setObject(idx + 1, param)
            }
            val rs = stmt.executeQuery()
            try f(rs)
            finally rs.close()
          } finally stmt.close()
        }
      }

      private def executeUpdate(
          sql: String,
          params: List[Any] = Nil
      ): IO[Int] = {
        withConnection { conn =>
          val stmt = conn.prepareStatement(sql)
          try {
            params.zipWithIndex.foreach { case (param, idx) =>
              stmt.setObject(idx + 1, param)
            }
            stmt.executeUpdate()
          } finally stmt.close()
        }
      }

      def findById(id: Long): IO[Option[User]] = {
        executeQuery(
          "SELECT id, name, email FROM users WHERE id = ?",
          List(id)
        ) { rs =>
          if (rs.next()) {
            Some(
              User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email")
              )
            )
          } else None
        }
      }

      def findAll: IO[List[User]] = {
        executeQuery("SELECT id, name, email FROM users") { rs =>
          Iterator
            .continually(rs)
            .takeWhile(_.next())
            .map(rs =>
              User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email")
              )
            )
            .toList
        }
      }

      def create(user: User): IO[User] = {
        executeQuery(
          "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id",
          List(user.name, user.email)
        ) { rs =>
          if (rs.next()) {
            user.copy(id = rs.getLong("id"))
          } else throw new RuntimeException("Failed to create user")
        }
      }

      def update(user: User): IO[User] = {
        executeUpdate(
          "UPDATE users SET name = ?, email = ? WHERE id = ?",
          List(user.name, user.email, user.id)
        ).flatMap { rows =>
          if (rows == 1) IO.pure(user)
          else IO.raiseError(new RuntimeException("Failed to update user"))
        }
      }

      def delete(id: Long): IO[Unit] = {
        executeUpdate("DELETE FROM users WHERE id = ?", List(id))
          .flatMap { rows =>
            if (rows == 1) IO.unit
            else IO.raiseError(new RuntimeException("Failed to delete user"))
          }
      }
    }
}

// Custom exception for repository layer
case class UserNotFoundException(id: Long, context: String)
    extends RuntimeException(s"User $id not found for $context")
