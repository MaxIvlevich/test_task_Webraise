# User Subscription Management Service

This project is a Spring Boot 3 microservice providing a REST API for managing users and their subscriptions to various digital services.

## Technologies Used

*   Java 17
*   Spring Boot 3.4.5
*   Spring Data JPA
*   Spring Web
*   PostgreSQL
*   Liquibase (for database migrations)
*   Maven (for project build)
*   Docker & Docker Compose (for containerization and local execution)
*   SLF4J (for logging)

## Features

### User API
*   **Create User:** `POST /users`
*   **Get User Info:** `GET /users/{id}`
*   **Update User Data:** `PUT /users/{id}`
*   **Delete User:** `DELETE /users/{id}`
*   **Get All Users with Subscriptions (Paginated):** `GET /users?page=0&size=10&sort=username,asc`

### Subscription API
*   **Add Subscription to User:** `POST /users/{userId}/subscriptions`
*   **Get User's Subscriptions:** `GET /users/{userId}/subscriptions`
*   **Remove Subscription from User:** `DELETE /users/{userId}/subscriptions/{subscriptionId}`
*   **Get Top 3 Popular Subscriptions:** `GET /subscriptions/top`

## Prerequisites

*   Docker
*   Docker Compose

## How to Run (using Docker Compose)

This is the recommended way to run the application locally as it sets up both the application and the PostgreSQL database in isolated containers.

1.  **Clone the repository:**
    ```bash
    git clone <https://github.com/MaxIvlevich/test_task_Webraise>
    cd <test_task_Webraise>
    ```

2.  **Run the services using Docker Compose:**
    From the root directory of the project (where `docker-compose.yml` is located), execute:
    ```bash
    docker-compose up --build
    ```
    *   The `--build` flag rebuilds the application's Docker image if there have been changes to the `Dockerfile` or source code. It's required for the first run.
    *   This command will download the PostgreSQL image (if not already present), build your application's image, and start both containers.
    *   The application will be accessible at: `http://localhost:8080`
    *   The PostgreSQL database will be accessible for external connections (e.g., via DBeaver or pgAdmin) at: `localhost:5433` (user: `postgres`, password: `postgres`, database: `user_subscription_db`).

3.  **Stopping the services:**
    To stop the running containers, press `Ctrl+C` in the terminal where `docker-compose up` was executed.
    To stop and remove the containers (database data will be preserved if using the named volume):
    ```bash
    docker-compose down
    ```
    To stop, remove containers, and also remove the database data volume (data will be lost!):
    ```bash
    docker-compose down -v
    ```

## Configuration Notes

*   The application connects to the PostgreSQL database service named `db` within the Docker network.
*   Database connection details (URL, username, password) for the Spring Boot application are configured via environment variables in `docker-compose.yml` and override any settings in `application.properties`.
*   Liquibase is enabled and will apply database migrations on application startup.