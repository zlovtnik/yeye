# YeYe - A Modern Web Application

YeYe is a modern web application built with Scala 3, Cats Effect, and Http4s for the backend, and Scala.js with Laminar for the frontend.

## Prerequisites

- JDK 11 or later
- sbt 1.9.x or later
- Docker (for running the Oracle database, if using the provided configuration) or a running Oracle instance.

## Setup

1.  **Database Setup:**
    *   This project is configured to connect to an Oracle database. The connection details are in `backend/src/main/resources/application.conf`.
    *   By default, it expects an Oracle instance at `jdbc:oracle:thin:@//localhost:1521/free` with user `system` and password `ora`.
    *   If you don't have a local Oracle instance, you can potentially run one using Docker. *Note: Instructions for running Oracle via Docker are not included here but can be found online.*
    *   Adjust the connection details in `application.conf` if your setup differs.
    *   Database migrations are handled automatically by Flyway using scripts in `backend/src/main/resources/db/migration`.

2.  **Build and Run:**
    *   You need to run both the backend and the frontend development server. It's easiest to use two separate terminals.

    *   **Terminal 1: Run the Backend**
        ```bash
        sbt backend/run
        ```
        The backend server will start at `http://localhost:8080`. Flyway migrations will run on startup.

    *   **Terminal 2: Run the Frontend Dev Server (with auto-recompile)**
        ```bash
        sbt "~frontend/fastOptJS;devServer/run"
        ```
        This command does two things:
        *   `~frontend/fastOptJS`: Watches for changes in the `frontend` code and recompiles the JavaScript (`yeye-frontend-fastopt.js`) to `frontend/target/scala-3.3.1/yeye-frontend-fastopt/`.
        *   `devServer/run`: Starts a simple development server on `http://localhost:8081` which serves the `index.html` from `devServer/src/main/resources/` and the compiled JavaScript from the frontend target directory.

    *   Access the application in your browser at `http://localhost:8081`.

## Development

### Backend

The backend is built with:
- Scala 3.3.1
- Cats Effect for pure functional programming
- Http4s for the HTTP server and REST API
- Circe for JSON handling
- Flyway for database migrations
- Oracle Database for persistence (via JDBC)

### Frontend

The frontend is built with:
- Scala.js
- Laminar for reactive UI (Assumed, verify if correct)
- Scala.js DOM for browser interaction (Assumed, verify if correct)

## REST API

The backend exposes a REST API at `http://localhost:8080`.

### Endpoints

*   **Health Check:**
    *   `GET /health`: Returns the server status.
        *   Response: `200 OK` with JSON body `{"status": "ok"}`

*   **Users:** (Mounted under `/users`)
    *   `GET /users`: Retrieves a list of all users.
        *   Response: `200 OK` with JSON array `[{"id": 1, "name": "...", "email": "..."}, ...]`
    *   `POST /users`: Creates a new user.
        *   Request Body: JSON `{"name": "...", "email": "..."}`
        *   Response: `201 Created` with the created user object `{"id": ..., "name": "...", "email": "..."}`
    *   `GET /users/{id}`: Retrieves a single user by ID.
        *   Response: `200 OK` with user object or `404 Not Found`.
    *   `PUT /users/{id}`: Updates a user by ID.
        *   Request Body: JSON `{"id": ..., "name": "...", "email": "..."}` (ID in body must match ID in path)
        *   Response: `200 OK` with updated user object or `404 Not Found`.
    *   `DELETE /users/{id}`: Deletes a user by ID.
        *   Response: `204 No Content` or `404 Not Found`.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 