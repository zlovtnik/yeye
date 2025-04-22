package com.yeye.backend

import zio.*
import zio.http.*
import com.yeye.shared.{Process, ProcessStatus}
import zio.json.*
import com.yeye.backend.model.User
import com.yeye.backend.repository.UserRepository
import com.yeye.backend.config.DatabaseConfig.{context, dataSourceLayer}
import io.getquill.*
import javax.sql.DataSource

object Server extends ZIOAppDefault:
  given JsonEncoder[Process] = DeriveJsonEncoder.gen[Process]
  given JsonEncoder[ProcessStatus] = DeriveJsonEncoder.gen[ProcessStatus]
  given JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  val app = Routes(
    Method.GET / "processes" -> handler { (_: Request) =>
      ZIO.succeed(
        Response.json(
          List(
            Process(
              "1",
              "Process 1",
              ProcessStatus.Running,
              java.lang.System.currentTimeMillis(),
              java.lang.System.currentTimeMillis()
            ),
            Process(
              "2",
              "Process 2",
              ProcessStatus.Paused,
              java.lang.System.currentTimeMillis(),
              java.lang.System.currentTimeMillis()
            )
          ).toJson
        )
      )
    },
    Method.GET / "users" -> handler { (_: Request) =>
      ZIO
        .serviceWithZIO[UserRepository](_.getAll)
        .map(users => Response.json(users.toJson))
    },
    Method.GET / "users" / string("id") -> handler { (id: String, _: Request) =>
      ZIO
        .serviceWithZIO[UserRepository](_.getById(id))
        .map {
          case Some(u) => Response.json(u.toJson)
          case None    => Response.status(Status.NotFound)
        }
    },
    Method.POST / "users" -> handler { (req: Request) =>
      for
        user <- req.body.asString.map(_.fromJson[User]).absolve
        created <- ZIO.serviceWithZIO[UserRepository](_.create(user))
      yield Response.json(created.toJson)
    },
    Method.PUT / "users" -> handler { (req: Request) =>
      for
        user <- req.body.asString.map(_.fromJson[User]).absolve
        updated <- ZIO.serviceWithZIO[UserRepository](_.update(user))
      yield Response.json(updated.toJson)
    },
    Method.DELETE / "users" / string("id") -> handler {
      (id: String, _: Request) =>
        ZIO
          .serviceWithZIO[UserRepository](_.delete(id))
          .as(Response.ok)
    }
  ).toHttpApp

  def run =
    zio.http.Server
      .serve(app)
      .provide(
        zio.http.Server.defaultWithPort(8080),
        UserRepository.live,
        dataSourceLayer
      )
