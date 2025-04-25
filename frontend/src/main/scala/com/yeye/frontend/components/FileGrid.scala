package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.frontend.types.File
import com.yeye.frontend.services.ApiService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FileGrid {
  case class Props(
      files: Signal[List[File]],
      onFileDownload: File => Unit = _ => (),
      onFileDelete: File => Unit = _ => ()
  )

  def apply(props: Props): HtmlElement = {
    div(
      cls := "file-grid",
      children <-- props.files.map { files =>
        files.map { file =>
          div(
            cls := "file-card",
            div(
              cls := "file-icon"
              // You can add file type icons here
            ),
            div(
              cls := "file-info",
              h3(file.name),
              p(s"Size: ${file.size} bytes"),
              p(s"Type: ${file.fileType}"),
              p(
                s"Last Modified: ${new js.Date(file.lastModified).toLocaleString()}"
              )
            ),
            div(
              cls := "file-actions",
              button(
                cls := "btn-primary",
                "Download",
                onClick --> { _ => props.onFileDownload(file) }
              ),
              button(
                cls := "btn-danger",
                "Delete",
                onClick --> { _ => props.onFileDelete(file) }
              )
            )
          )
        }
      }
    )
  }
}
