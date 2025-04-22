package com.yeye.backend.repository

import zio.*
import com.yeye.shared.User
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource

trait UserRepository:
  def createUser(user: User): ZIO[DataSource, Throwable, User]
  def getUserById(id: String): ZIO[DataSource, Throwable, Option[User]]
  def getUsers: ZIO[DataSource, Throwable, List[User]]
  def updateUser(
      id: String,
      user: User
  ): ZIO[DataSource, Throwable, Option[User]]
  def deleteUser(id: String): ZIO[DataSource, Throwable, Boolean]

object UserRepository:
  val live: ULayer[UserRepository] =
    ZLayer.succeed(
      new UserRepository:
        import context.*

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
