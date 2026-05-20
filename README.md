## 🎮 Video Game Library Management API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-0064a5?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-dc382d?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker)

A robust RESTful API for a Video Game Library Tracking System (similar to MyAnimeList or Backloggd). Built with **Java 21** and **Spring Boot 4**, this system allows users to track their gaming progress, rate games, write reviews, and discover new titles. Real game data is seamlessly fetched from the external RAWG Video Games API.

## ✨ Key Features

*   **🔒 Robust Security & Auth:** Spring Security & stateless JWT Authentication, featuring Role-Based Access Control (RBAC) and an OTP-based password recovery flow (delivered via the Brevo API).
*   **⚡ Enterprise-Grade Caching:** Implemented **Redis** to intercept heavy database queries. Utilizes custom Time-To-Live (TTL) configurations for eventual consistency on global stats, and targeted `@CacheEvict` strategies to ensure real-time accuracy for personal user lists.
*   **🌍 External API Integration:** Integrated with the [RAWG API](https://rawg.io/) via Spring Cloud OpenFeign to automatically fetch and map real-world game details (covers, release dates, metacritic scores, genres, and platforms).
*   **📋 Personal Tracking Lists:** Users can add games to their personal lists, assigning statuses (`PLANNED`, `PLAYING`, `COMPLETED`, `DROPPED`) and 1-10 ratings.
*   **📊 Dynamic Global Statistics:** The system automatically aggregates user data to display global statistics for each game (e.g., total players, average community score, drop rates) utilizing Spring `@Scheduled` tasks.
*   **📝 Review System:** Users can write, edit, and delete comprehensive reviews for games.
*   **🔍 Advanced Search & Filtering:** Paginated, sortable, and heavily filterable endpoints using Spring Data JPA `@Query` (filter by genres, platforms, release years, minimum scores) protected by "Landing Page" caching strategies to prevent combinatorial explosion.
*   **⚙️ Aspect-Oriented Logging:** Custom AOP (`@Aspect`) implementation for performance monitoring (execution time) and request/response logging, with automatic masking of sensitive security payloads.
*   **🛡️ Global Exception Handling:** Clean, standardized `ApiError` JSON responses for all client and server errors using `@RestControllerAdvice`.

## 🛠️ Technology Stack

*   **Core:** Java 21, Spring Boot 4
*   **Data Access & Caching:** Spring Data JPA, Hibernate, PostgreSQL, Redis
*   **Security:** Spring Security, JWT (JSON Web Tokens), BCrypt
*   **External Communication:** Spring Cloud OpenFeign, Spring RestClient [(Brevo Email API)](https://www.brevo.com/)
*   **Documentation:** Springdoc OpenAPI (Swagger UI)
*   **Testing:** JUnit 5, Mockito, MockMvc, DataJpaTest
*   **DevOps:** Docker, Docker Compose, Maven

## 🚀 Getting Started

> **Note for Reviewers/Recruiters:** API keys and environment variables are intentionally left in the configuration files to allow for seamless, zero-config testing and portfolio review.

### Prerequisites
Depending on how you choose to run the application, ensure you have the following installed:
* **For Docker:** [Docker](https://www.docker.com/)
* **For Local Development:** [Java 21](https://jdk.java.net/21/), [PostgreSQL](https://www.postgresql.org/download/) & [Redis](https://redis.io/downloads/)

### 1. Get the Code
First, clone the repository and navigate into the project directory:
```bash
git clone https://github.com/RR-CMD/games-api.git
cd games-api
```

### 2. Run the Application
Choose one of the following methods to run the API:

#### Option A: Running with Docker (Full Stack)
Start Docker and spin up the complete ecosystem (application backend, PostgreSQL 18 database, and Redis cache) using Docker Compose:
```bash
docker compose up -d --build

```

> **Database Seeding:** On the very first startup, the `DatabaseDataFiller` will automatically activate. It will communicate with the RAWG API to fetch 10 real games and generate dummy users, reviews, and tracking lists. *(Note: This makes the initial startup take a few seconds longer).*

#### Option B: Running Locally (Hybrid or Native)

If you prefer to run the Java backend directly on your host machine (e.g., for step-by-step debugging in your IDE), you must ensure your database and caching infrastructure layers are up and running first.

##### Step 1: Start the Infrastructure Dependencies

Choose one of the two ways to provide the database and cache layers:

##### Step 1: Start the Infrastructure Dependencies
You need a running database and cache. Choose **one** of the following options:

* **Via Docker (Recommended):** Run just the data layers in the background without installing anything natively:
  ```bash
  docker compose up -d postgres_db redis_db
* **Via Native Services:** Run native instances directly on your machine using these configurations:
  * **PostgreSQL:** Port `5432` | Database: `games_db` | User: `postgres` | Password: `root`
  * **Redis:** Port `6379`

##### Step 2: Launch the Backend Application

Once your infrastructure dependencies are running, start the application using your preferred workspace environment:

* **Via an IDE (Easiest)**
1. Open the project folder inside your preferred Java IDE (IntelliJ IDEA, Eclipse, or VS Code).
2. Allow Maven to import and resolve all project dependencies.
3. Execute the `main` method located inside the `GamesApplication.java` entry point class.


* **Via the Command Line**
Open your terminal and use the included Maven wrapper to clean, compile, and boot the application context:
```bash
./mvnw clean spring-boot:run
```

### Accessing the API
Once started via Docker or your local machine, the API will be available at:
**http://localhost:8080**

## 📖 API Documentation (Swagger UI)

Interactive API documentation is automatically generated. Once the application is running, navigate to:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

From here, you can authenticate, test endpoints, and view exact request/response schemas.

## 🔑 Default Test Accounts

The application is pre-seeded with the following accounts for immediate testing:

| Role      | Email              | Password       |
| :-------- | :----------------- | :------------- |
| **ADMIN** | `admin@games.com`  | `Password1234` |
| **USER**  | `osamah@games.com` | `Password1234` |
| **USER**  | `john@games.com`   | `Password1234` |
| **USER**  | `sarah@games.com`  | `Password1234` |
| **USER**  | `mike@games.com`   | `Password1234` |
| **USER**  | `emma@games.com`   | `Password1234` |

*To test protected endpoints, log in via `/auth/login`, copy the `token` from the response, and authorize in Swagger UI using the "Authorize" button (Bearer token).*

## 📂 Architecture Overview

The project follows a standard layered architecture, divided by feature (Domain-Driven Package Structure):

*   **auth:** JWT generation, login, registration, OTP logic, and modern RestClient integration for Brevo emails.
*   **game:** Local game entity management, paginated searching, caching strategies, and scheduled global stat aggregation.
*   **external.rawg:** Feign Client configurations and DTOs to map RAWG API external JSON.
*   **usergame:** Logic handling a user's personal game tracking list.
*   **review:** CRUD operations for game reviews.
*   **user:** User profile viewing and administrative user management.
*   **common & config:** AOP Logging, Redis configurations, standard API responses, Swagger config, CORS, and DB Seeding.
*   **exception:** Global exception handler and custom exception classes.
*   **security:** JWT Filter, custom Authentication Entry Point, and UserDetailsService.

## 🧪 Quality Assurance

This project maintains high-integrity test coverage, reaching **100% line coverage** across layers of the application.

### Testing Stack
* **Framework:** JUnit 5 & AssertJ
* **Mocking:** Mockito
* **Web Layer:** MockMvc (`@WebMvcTest`)
* **Persistence Layer:** H2 Database (`@DataJpaTest`)

### Test Strategy
* **Unit Testing:** Comprehensive isolation of the Service layer to validate complex business logic, such as game status transitions, cache evictions, and user-specific data filtering.
* **Security Validation:** Full coverage of the JWT authentication flow, including edge-case handling for expired, malformed, or missing tokens.
* **Integration Slicing:** Use of Spring Boot Test Slices to verify that the Web and Persistence layers interact correctly without the overhead of a full application context.

### Execution
To run the full test suite and verify the application:
```bash
./mvnw test
```

## 💡 Highlighted Code Segments for Reviewers

If you are reviewing this code for a technical evaluation, I recommend checking out the following classes which demonstrate advanced Spring Boot concepts:

1.  **`RedisConfig.java` & `GameService.java`**: Demonstrates advanced caching architectures, including custom TTLs for eventual consistency and "Landing Page" caching to prevent memory leaks from combinatorial explosion.
2.  **`LoggingAspect.java`**: Demonstrates AOP capabilities to automatically measure performance and log data while isolating security-sensitive payloads.
3.  **`RawgClient.java`**: Demonstrates declarative external REST client integration using OpenFeign.
4.  **`JwtFilter.java` & `SecurityConfig.java`**: Demonstrates modern, stateless Spring Security 6+ implementation.
5.  **`GameRepository.java`**: Demonstrates complex, highly-dynamic Spring Data JPA `@Query` implementations for advanced filtering.

## 🗺️ Roadmap & Future Enhancements

While the core functionality of the API is complete, there are several features planned to make the ecosystem more interactive and robust:

* **Social Features:** Implementing a follow system allowing users to follow each other's gaming journeys, complete with a customized activity feed.
* **Review Interactions:** Adding impressions and helpfulness voting to user reviews to highlight community-driven content.
* **CI/CD Pipelines:** Implementing GitHub Actions to automatically trigger the JUnit test suite and build Docker images on every push to the `main` branch.

## 🔒 Path to Production

This repository is currently optimized for seamless local testing and portfolio review (hence the inclusion of default credentials and configuration files). To deploy this to a live production environment, the following security and architectural steps would be taken:

* **Secret Management:** Move all hardcoded API keys, JWT secrets, and database credentials out of `application.yml` and into environment variables or a secure vault (e.g., AWS Secrets Manager).
* **OTP Hashing:** Currently, Password Recovery OTPs are functional but handled in plain text for testing visibility. In production, these would be cryptographically hashed in the database before verification, just like passwords.
* **Rate Limiting:** Implement API rate limiting (e.g., using Bucket4j) on critical endpoints like `/auth/login` and `/auth/recover` to prevent brute-force attacks.
* **CORS Configuration:** Restrict Cross-Origin Resource Sharing (CORS) strictly to the production frontend domain instead of allowing `*`.
* **Scheduled Tasks:** Transitioning from high-frequency polling (currently set to 2-minute intervals for demonstration purposes) to production-aligned Cron expressions (e.g., nightly or weekly off-peak updates) to minimize database CPU overhead and optimize resource utilization.

## 🎨 Acknowledgements & Attribution

* **RAWG API:** All video game metadata, including titles, release dates, metacritic scores, genres, and cover images, are generously provided by the [RAWG Video Games Database API](https://rawg.io/apidocs).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

Created by Osamah Mulhem - feel free to reach out to me via email at [osamayousefmulhem@outlook.com](mailto:osamayousefmulhem@outlook.com) or connect with me on [LinkedIn](https://www.linkedin.com/in/osamah-mulhem-3821b740a/).
