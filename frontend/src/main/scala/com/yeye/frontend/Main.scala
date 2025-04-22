package com.yeye.frontend

import com.raquo.laminar.api.L.*
import com.yeye.shared.Process
import zio.json.*
import org.scalajs.dom
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global

object Main:
  def main(args: Array[String]): Unit =
    val app = div(
      h1("Process Manager"),
      div(
        cls := "process-list",
        child <-- fetchProcesses.map(processes =>
          div(
            processes.map(process =>
              div(
                cls := "process",
                h2(process.name),
                p(s"Status: ${process.status}"),
                p(s"Created: ${new java.util.Date(process.createdAt)}"),
                p(s"Updated: ${new java.util.Date(process.updatedAt)}")
              )
            )
          )
        )
      )
    )

    render(dom.document.querySelector("#app"), app)

  private def fetchProcesses: EventStream[List[Process]] =
    EventStream
      .fromFuture(
        dom
          .fetch("http://localhost:8080/processes")
          .flatMap(_.text())
          .map(_.fromJson[List[Process]].getOrElse(List.empty))
      )
      .recoverToTry
      .map(_.getOrElse(List.empty))
