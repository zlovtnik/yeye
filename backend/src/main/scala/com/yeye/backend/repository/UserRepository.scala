package com.yeye.backend.repository

import zio.*
import com.yeye.backend.model.User
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource

class UserRepository:
  import context.*

  inline def users = quote {
    querySchema[User]("OKTA_USERS")
  }

  def create(user: User): ZIO[DataSource, Throwable, User] =
    run {
      quote {
        users.insertValue(lift(user))
      }
    }.map(_ => user)

  def getById(id: String): ZIO[DataSource, Throwable, Option[User]] =
    run {
      quote {
        users.filter(_.id == lift(id))
      }
    }.map(_.headOption)

  def getAll: ZIO[DataSource, Throwable, List[User]] =
    run(users)

  def update(user: User): ZIO[DataSource, Throwable, User] =
    run {
      quote {
        users
          .filter(_.id == lift(user.id))
          .updateValue(lift(user))
      }
    }.map(_ => user)

  def delete(id: String): ZIO[DataSource, Throwable, Unit] =
    run {
      quote {
        users.filter(_.id == lift(id)).delete
      }
    }.map(_ => ())

object UserRepository:
  val live: ULayer[UserRepository] =
    ZLayer.succeed(new UserRepository)
