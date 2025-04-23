package com.yeye.frontend.components

import com.raquo.laminar.api.L.*
import com.yeye.frontend.types.File
import com.yeye.frontend.services.ApiService
import com.yeye.shared.User
import zio.json.*
import scala.concurrent.ExecutionContext.Implicits.global
import com.raquo.airstream.ownership.Owner

object App:
  def apply(owner: Owner): HtmlElement =
    // State variables
    val emailVar = Var("")
    val firstNameVar = Var("")
    val lastNameVar = Var("")
    val statusVar = Var("")
    val selectedUserId = Var[Option[String]](None)
    val searchQueryVar = Var("")
    val activeTabVar = Var("users")
    val filesVar = Var[List[File]](List.empty)

    // Filtered files signal
    val filteredFiles: Signal[List[File]] =
      filesVar.signal.combineWith(searchQueryVar.signal).map { (files, query) =>
        if (query.isEmpty) files
        else files.filter(_.name.toLowerCase.contains(query.toLowerCase))
      }

    // Fetch files when switching to files tab
    activeTabVar.signal.addObserver(Observer[String] { tab =>
      if tab == "files" then
        ApiService
          .executeGraphQL(ApiService.filesQuery)
          .foreach { response =>
            val files = response.fromJson[List[File]].getOrElse(List.empty)
            filesVar.set(files)
          }
    })(owner)

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
              UserForm(
                UserForm.Props(
                  emailVar = emailVar,
                  firstNameVar = firstNameVar,
                  lastNameVar = lastNameVar,
                  statusVar = statusVar,
                  selectedUserId = selectedUserId,
                  onUserCreated = () => (),
                  onUserUpdated = () => (),
                  onUserDeleted = () => ()
                )
              ),
              child <-- EventStream
                .fromFuture(ApiService.executeGraphQL(ApiService.usersQuery))
                .map(_.fromJson[List[User]].getOrElse(List.empty))
                .map(users =>
                  div(
                    cls := "user-list",
                    users.map(user =>
                      div(
                        cls := "user-item",
                        h3(s"${user.firstName} ${user.lastName}"),
                        p(s"Email: ${user.email}"),
                        p(s"Status: ${user.status}"),
                        onClick --> { _ =>
                          emailVar.set(user.email)
                          firstNameVar.set(user.firstName)
                          lastNameVar.set(user.lastName)
                          statusVar.set(user.status)
                          selectedUserId.set(Some(user.id))
                        }
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
