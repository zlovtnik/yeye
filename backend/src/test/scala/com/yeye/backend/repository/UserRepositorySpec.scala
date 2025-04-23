package com.yeye.backend.repository

import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.ZIOSpecDefault
import com.yeye.shared.User
import javax.sql.DataSource
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import java.lang.System

object UserRepositorySpec extends ZIOSpecDefault:
  def spec = suite("UserRepository")(
    test("create and get user") {
      for {
        repo <- ZIO.service[UserRepository]
        user = User(
          id = "test-id-1",
          email = "test@example.com",
          firstName = "Test",
          lastName = "User",
          status = "ACTIVE",
          created = System.currentTimeMillis(),
          lastUpdated = System.currentTimeMillis()
        )
        created <- repo.createUser(user)
        fetched <- repo.getUserById(user.id)
      } yield assert(fetched)(isSome(equalTo(user)))
    },
    test("get all users") {
      for {
        repo <- ZIO.service[UserRepository]
        user1 = User(
          id = "test-id-2",
          email = "test1@example.com",
          firstName = "Test1",
          lastName = "User1",
          status = "ACTIVE",
          created = System.currentTimeMillis(),
          lastUpdated = System.currentTimeMillis()
        )
        user2 = User(
          id = "test-id-3",
          email = "test2@example.com",
          firstName = "Test2",
          lastName = "User2",
          status = "ACTIVE",
          created = System.currentTimeMillis(),
          lastUpdated = System.currentTimeMillis()
        )
        _ <- repo.createUser(user1)
        _ <- repo.createUser(user2)
        users <- repo.getUsers
      } yield assert(users)(hasSize(isGreaterThan(1)))
    },
    test("update user") {
      for {
        repo <- ZIO.service[UserRepository]
        user = User(
          id = "test-id-4",
          email = "test@example.com",
          firstName = "Test",
          lastName = "User",
          status = "ACTIVE",
          created = System.currentTimeMillis(),
          lastUpdated = System.currentTimeMillis()
        )
        _ <- repo.createUser(user)
        updatedUser = user.copy(firstName = "Updated")
        result <- repo.updateUser(user.id, updatedUser)
        fetched <- repo.getUserById(user.id)
      } yield assert(fetched)(isSome(equalTo(updatedUser)))
    },
    test("delete user") {
      for {
        repo <- ZIO.service[UserRepository]
        user = User(
          id = "test-id-5",
          email = "test@example.com",
          firstName = "Test",
          lastName = "User",
          status = "ACTIVE",
          created = System.currentTimeMillis(),
          lastUpdated = System.currentTimeMillis()
        )
        _ <- repo.createUser(user)
        deleted <- repo.deleteUser(user.id)
        fetched <- repo.getUserById(user.id)
      } yield assert(deleted)(isTrue) && assert(fetched)(isNone)
    }
  ).provideLayerShared(UserRepository.live ++ dataSourceLayer)
