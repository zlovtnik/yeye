package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.frontend.types.File
import com.yeye.frontend.services.ApiService
import com.yeye.frontend.types.User
import io.circe.parser.decode
import org.scalajs.dom
import com.raquo.airstream.ownership.Owner
import scala.concurrent.ExecutionContext.Implicits.global

object App:
  def apply(owner: Owner): HtmlElement =
    // State variables
    val searchQueryVar = Var("")
    val activeTabVar = Var("users")
    val filesVar = Var[List[File]](List.empty)
    val usersVar = Var[List[User]](List.empty)
    val errorVar = Var[Option[String]](None)

    // Filtered files signal
    val filteredFiles: Signal[List[File]] =
      filesVar.signal.combineWith(searchQueryVar.signal).map { (files, query) =>
        if (query.isEmpty) files
        else files.filter(_.name.toLowerCase.contains(query.toLowerCase))
      }

    // Fetch users when switching to users tab
    activeTabVar.signal.addObserver(Observer[String] { tab =>
      if tab == "users" then
        ApiService.getUsers.foreach { users =>
          usersVar.set(users)
        }
    })(owner)

    val $users = usersVar.signal
    val $error = errorVar.signal

    div(
      cls := "app-container",
      // Sidebar
      div(
        cls := "sidebar",
        div(
          cls := "sidebar-header",
          h1("Menu")
        ),
        ul(
          cls := "sidebar-menu",
          li(
            cls <-- activeTabVar.signal.map(tab =>
              if tab == "users" then "active" else ""
            ),
            "Users",
            onClick --> { _ => activeTabVar.set("users") }
          ),
          li(
            cls <-- activeTabVar.signal.map(tab =>
              if tab == "files" then "active" else ""
            ),
            "Files",
            onClick --> { _ => activeTabVar.set("files") }
          )
        )
      ),
      // Main content
      div(
        cls := "main-content",
        child <-- activeTabVar.signal.map { tab =>
          if tab == "users" then
            div(
              h1("User Management"),
              div(
                cls := "error",
                child <-- $error.map {
                  case Some(error) => div(error)
                  case None        => emptyNode
                }
              ),
              UserForm.render(UserForm.Props(usersVar, errorVar)),
              div(
                cls := "users-list",
                children <-- $users.map(users =>
                  users.map(user =>
                    div(
                      cls := "user-card",
                      h3(user.name),
                      p(s"Email: ${user.email}"),
                      p(s"Age: ${user.age}")
                    )
                  )
                )
              )
            )
          else
            div(
              h1("File Management"),
              div(
                cls := "search-container",
                input(
                  typ := "text",
                  placeholder := "Search files...",
                  value <-- searchQueryVar,
                  onInput.mapToValue --> searchQueryVar
                )
              ),
              FileGrid(
                FileGrid.Props(
                  files = filteredFiles,
                  onFileDownload = file => {
                    // Add download logic here
                  },
                  onFileDelete = file => {
                    // Add delete logic here
                  }
                )
              )
            )
        }
      )
    )
