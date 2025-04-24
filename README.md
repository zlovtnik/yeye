# YeYe - A Modern Web Application

YeYe is a modern web application built with Scala 3, ZIO, Cats Effect, and Http4s for the backend, and Scala.js with Laminar for the frontend.

## Prerequisites

- JDK 17 or later
- sbt 1.9.x or later
- Docker and docker-compose/podman-compose for containerized deployment

## Setup

1.  **Database Setup:**
    *   This project is configured to connect to an Oracle database. The connection details are configured via environment variables.
    *   Default connection parameters are stored in `docker-compose.yml` and include:
      ```
      DB_URL=jdbc:oracle:thin:@//oracle:1521/FREE
      DB_USER=system
      DB_PASSWORD=ora
      DB_CONNECTION_RETRY_COUNT=30
      DB_CONNECTION_RETRY_DELAY=10
      ```
    *   Database migrations are handled automatically by Flyway using scripts in `backend/src/main/resources/db/migration`.

2.  **Local Development:**
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

3.  **Docker Deployment:**
    *   The project includes a complete Docker setup with:
      * Oracle database service
      * Scala backend service
      * Nginx webserver for static files and frontend assets
    
    *   **Build and Run All Services:**
        ```bash
        podman compose -f docker/docker-compose.yml up -d
        # or with Docker
        docker-compose -f docker/docker-compose.yml up -d
        ```
        
    *   **Access The Application:**
        * Frontend: `http://localhost:80`
        * Backend API: `http://localhost:8080`
        * Database: `localhost:1521` (Oracle FREE service)

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
- Scala.js for compiling Scala to JavaScript
- Laminar for reactive UI programming
- Circe for JSON handling
- ZIO for effect management
- Cats for functional programming

#### Frontend Structure

The frontend follows a modular architecture for scalability:

```
frontend/src/main/scala/com/yeye/frontend/
├── Main.scala                 # Application entry point
├── Router.scala               # Page routing and navigation
│
├── core/                      # Core utilities
│   ├── Config.scala           # API configuration
│   └── Style.scala            # Global styling
│
├── models/                    # Data models
│   └── User.scala             # Example: User data type
│
├── pages/                     # Page components
│   ├── LandingPage.scala      # Home page
│   └── users/                 # User management
│       └── UsersPage.scala    # Users list and CRUD
│
├── components/                # Reusable UI components
│   ├── layout/                # Structural components
│   │   └── BurgerMenu.scala   # Side navigation
│   └── common/                # Generic components
└── services/                  # API services
```

#### UI Features

- **Theme**: Dark green and purple color scheme
- **Navigation**: Collapsible side menu with hover expansion
- **Landing Page**: Welcome screen with feature highlights
- **Users Page**: CRUD interface for user management
- **Responsive Design**: Adapts to different screen sizes

## Testing

The project includes a comprehensive testing infrastructure for both backend and frontend components.

### Running All Tests

To run all tests (backend and frontend) in a single command:

```bash
sbt testAll
```

This will:
1. Run backend unit tests using ScalaTest
2. Compile the Scala.js code and run frontend tests in headless mode

### Backend Tests

Backend tests use ScalaTest with Cats Effect's testing utilities:

```bash
sbt backend/test
```

The tests cover REST API endpoints, service logic, and data repositories with simulated database interactions.

### Frontend Tests

Frontend tests can be run in two modes:

#### Interactive Mode (Browser-based)

This opens a browser window with the test runner:

```bash
./frontend/run-tests.sh
```

In this mode:
1. The Scala.js code is compiled to JavaScript
2. A browser window opens with the test page
3. Click the "Run Tests" button to execute tests
4. Test results appear on the page and in the browser console

#### Headless Mode (CI/CD)

For continuous integration environments:

```bash
./frontend/run-tests.sh --headless
```

This mode is designed for automated testing pipelines and exits with appropriate status codes.

### Test Structure

- **Backend tests**: Located in `backend/src/test/scala`
- **Frontend tests**: Browser-based tests using the DOM
  - Test implementations: `frontend/src/main/scala/com/yeye/frontend/TestMain.scala`
  - Test runner: `frontend/src/main/resources/test.html`

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

## Troubleshooting

### API Connection Issues

If you're experiencing issues connecting to the backend API:

1. **Check Backend Status**: Ensure the backend server is running (`sbt backend/run`)
2. **Verify Endpoints**: The frontend expects REST endpoints at `http://localhost:8080/users`
3. **Configuration**: Check `frontend/src/main/scala/com/yeye/frontend/core/Config.scala` to adjust API URL settings
4. **Debug Mode**: Use browser console debugging functions:
   ```javascript
   // Try different backend ports
   Config.fetchFromPort(0) // Try port 8080
   Config.fetchFromPort(1) // Try port 3000
   ```

### Docker/Podman Issues

When using Docker or Podman for deployment:

1. **Database Connection Errors**:
   - Check if the Oracle container is running: `podman ps | grep oracle`
   - Verify the Oracle container is healthy: `podman logs yeye-oracle`
   - The backend service has retry logic but may fail if Oracle takes too long to initialize

2. **Backend Build Failures**:
   - If the backend build fails, check the logs: `podman logs yeye-backend`
   - Verify environment variables are correctly passed in `docker-compose.yml`
   - Try rebuilding with no cache: `podman compose -f docker/docker-compose.yml build --no-cache backend`

3. **Nginx/Frontend Issues**:
   - Check if static assets are being served: `curl http://localhost:80`
   - Inspect Nginx logs: `podman logs yeye-nginx`
   - Verify the frontend assets were correctly built and copied to the Nginx container

4. **Running Behind Corporate Firewalls**:
   - Set appropriate proxy settings in Docker/Podman configuration
   - For Oracle images, ensure you have access to the Oracle Container Registry

### Browser Console Errors

- **404 Not Found**: Backend server may not be running, or endpoint path is incorrect
- **CORS Errors**: Backend might need CORS headers to allow frontend requests
- **Decode Errors**: Check that your data models match the API response format

## Future Improvements

Planned enhancements for the project:

- Additional entity types beyond Users
- Authentication and authorization
- Enhanced filtering and sorting capabilities
- Dashboard with data visualization
- PDF report generation

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Database Configuration

The application uses HikariCP for connection pooling to the Oracle database. The DatabaseConfig module has been enhanced with the following features:

1. **Environment Variable Configuration:**
   - Connection parameters are read from environment variables
   - Default values are provided as fallbacks
   - Key parameters include:
     - `DB_URL`: JDBC connection URL
     - `DB_USER`: Database username
     - `DB_PASSWORD`: Database password
     - `DB_CONNECTION_RETRY_COUNT`: Number of retry attempts (default: 30)
     - `DB_CONNECTION_RETRY_DELAY`: Delay between retries in seconds (default: 10)

2. **Connection Pooling:**
   - Minimum idle connections: 2
   - Maximum connections: 10
   - Connection timeout: 30 seconds
   - Connection validation via `SELECT 1 FROM DUAL`
   - Leak detection threshold: 30 seconds

3. **Retry Logic:**
   - The system implements a backoff strategy for establishing database connections
   - Failed connection attempts are logged with informative messages
   - The system will retry for the configured number of attempts before failing

4. **Resource Management:**
   - Database connections are wrapped in `Resource[IO, HikariDataSource]`
   - Proper resource cleanup is ensured via `Resource.make`
   - The `use` method safely manages connection lifecycles

Example usage:
```scala
DatabaseConfig.dataSource.use { dataSource =>
  // Use dataSource for the lifetime of this block
  // Connection will be automatically closed after use
}
``` 