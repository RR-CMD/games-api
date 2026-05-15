## 🎮 Video Game Library Management API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker)

A robust RESTful API for a Video Game Library Tracking System
(similar to MyAnimeList or Backloggd). Built with Java 21 and Spring Boot 4,
this system allows users to track their gaming progress, rate games, write
reviews, and discover new titles. Real game data is seamlessly fetched from the
external RAWG Video Games API.

## ✨ Key Features

  - 🔒 Robust Security & Auth: JWT-based stateless authentication, Role-Based
    Access Control (Admin/User), and a complete Password Recovery flow utilizing
    securely generated OTPs sent via Email.
  - 🌍 External API Integration: Integrated with the [RAWG API](https://rawg.io/) via Spring Cloud
    OpenFeign to automatically fetch and map real-world game details (covers,
    release dates, metacritic scores, genres, and platforms).
  - 📋 Personal Tracking Lists: Users can add games to their personal lists,
    assigning statuses (PLANNED, PLAYING, COMPLETED, DROPPED) and 1-10 ratings.
  - 📊 Dynamic Global Statistics: The system automatically aggregates user data
    to display global statistics for each game (e.g., total players, average
    community score, drop rates) utilizing Spring @Scheduled tasks.
  - 📝 Review System: Users can write, edit, and delete comprehensive reviews for
    games.
  - 🔍 Advanced Search & Filtering: Paginated, sortable, and heavily filterable
    endpoints using Spring Data JPA @Query (filter by genres, platforms, release
    years, minimum scores).
  - ⚙️ Aspect-Oriented Logging: Custom AOP (@Aspect) implementation for
    performance monitoring (execution time) and request/response logging, with
    automatic masking of sensitive security payloads.
  - 🛡️ Global Exception Handling: Clean, standardized ApiError JSON responses
    for all client and server errors using @RestControllerAdvice.

## 🛠️ Technology Stack

  - Core: Java 21, Spring Boot 4.0.5
  - Data Access: Spring Data JPA, Hibernate, PostgreSQL
  - Security: Spring Security, JWT (JSON Web Tokens), BCrypt
  - External Communication: Spring Cloud OpenFeign
  - Documentation: Springdoc OpenAPI (Swagger UI)
  - Testing: JUnit 5, Mockito, MockMvc, DataJpaTest
  - DevOps: Docker, Docker Compose, Maven

## 🚀 Getting Started

Note for Reviewers/Recruiters: API keys and environment variables are
intentionally left in the configuration files to allow for seamless, zero-config
testing and portfolio review.

### Prerequisites
Depending on how you choose to run the application, ensure you have the following installed:
* **For Docker:** [Docker](https://www.docker.com/)
* **For Local Development:** [Java 21](https://www.oracle.com/ae/java/technologies/downloads/#jdk21-windows) & [PostgreSQL](https://www.postgresql.org/download/)

### 1. Get the Code
First, clone the repository and navigate into the project directory:
```bash
git clone https://github.com/RR-CMD/games-api.git
cd games-api
```

### 2. Run the Application
Choose one of the following methods to run the API:

#### Option A: Running with Docker (Recommended)
Start Docker and spin up the application and PostgreSQL database using Docker Compose:
```bash
docker-compose up -d --build
```
> **Database Seeding:** On the very first startup, the `DatabaseDataFiller` will automatically activate. It will communicate with the RAWG API to fetch 10 real games and generate dummy users, reviews, and tracking lists. *(Note: This makes the initial startup take a few seconds longer).*

#### Option B: Running Locally (Without Docker)
If you prefer to run it locally, ensure you have a PostgreSQL instance running locally on port `5432` with a database named `games_db`, user `postgres`, and password `root`.

**Using an IDE (Easiest)**

1. Open the project in IntelliJ IDEA, Eclipse, or VS Code.
2. Let Maven download the dependencies.
3. Run the `GamesApplication.java` main class.


**Using the Command Line**

If you prefer the terminal, you can run the application using the included Maven wrapper:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Accessing the API
Once started via Docker or your local machine, the API will be available at:
**http://localhost:8080**

## 📖 API Documentation (Swagger UI)

Interactive API documentation is automatically generated. Once the application
is running, navigate to:

👉 http://localhost:8080/swagger-ui.html

From here, you can authenticate, test endpoints, and view exact request/response
schemas.

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


To test protected endpoints, log in via /auth/login, copy the token from the
response, and authorize in Swagger UI using the "Authorize" button (Bearer
token).

## 📂 Architecture Overview

The project follows a standard layered architecture, divided by feature
(Domain-Driven Package Structure):

  - auth: JWT generation, login, registration, OTP logic, and Email sending.
  - game: Local game entity management, searching, and scheduled global stat
    aggregation.
  - external.rawg: Feign Client configurations and DTOs to map RAWG API external
    JSON.
  - usergame: Logic handling a user's personal game tracking list.
  - review: CRUD operations for game reviews.
  - user: User profile viewing and administrative user management.
  - common & config: AOP Logging, standard API responses, Swagger config, CORS,
    and DB Seeding.
  - exception: Global exception handler and custom exception classes.
  - security: JWT Filter, custom Authentication Entry Point, and
    UserDetailsService.

## 🧪 Quality Assurance

This project maintains high-integrity test coverage, reaching **100% line coverage** across layers of the application.

### Testing Stack
* **Framework:** JUnit 5 & AssertJ
* **Mocking:** Mockito
* **Web Layer:** MockMvc (`@WebMvcTest`)
* **Persistence Layer:** H2 Database (`@DataJpaTest`)

### Test Strategy
* **Unit Testing:** Comprehensive isolation of the Service layer to validate complex business logic, such as game status transitions and user-specific data filtering.
* **Security Validation:** Full coverage of the JWT authentication flow, including edge-case handling for expired, malformed, or missing tokens.
* **Integration Slicing:** Use of Spring Boot Test Slices to verify that the Web and Persistence layers interact correctly without the overhead of a full application context.

### Execution
To run the full test suite and verify the application:

```bash
./mvnw test
```

## 💡 Highlighted Code Segments for Reviewers

If you are reviewing this code for a technical evaluation, I recommend checking
out the following classes which demonstrate advanced Spring Boot concepts:

1.  LoggingAspect.java: Demonstrates AOP capabilities to automatically measure
    performance and log data while isolating security-sensitive payloads.
2.  RawgClient.java: Demonstrates declarative external REST client integration
    using OpenFeign.
3.  JwtFilter.java & SecurityConfig.java: Demonstrates modern, stateless Spring
    Security 6+ implementation.
4.  GameRepository.java: Demonstrates complex, highly-dynamic Spring Data JPA
    @Query implementations for advanced filtering.
5.  GlobalExceptionHandler.java: Clean, scalable error management avoiding messy
    controller-level try/catches.
	
## 🗺️ Roadmap & Future Enhancements

While the core functionality of the API is complete, there are several features planned to make the ecosystem more interactive and robust:

* **Social Features:** Implementing a follow system allowing users to follow each other's gaming journeys, complete with a customized activity feed.
* **Review Interactions:** Adding impressions to user reviews to highlight community-driven content.
* **Caching Layer:** Introducing Redis to cache highly-requested, static data (like global game statistics or external RAWG API responses) to reduce database load and improve response times.

## 🔒 Path to Production

This repository is currently optimized for seamless local testing and portfolio review (hence the inclusion of default credentials and configuration files). To deploy this to a live production environment, the following security and architectural steps would be taken:

* **Secret Management:** Move all hardcoded API keys, JWT secrets, and database credentials out of `application.yml` and into environment variables or a secure vault (e.g., AWS Secrets Manager).
* **OTP Hashing:** Currently, Password Recovery OTPs are functional but handled in plain text. In production, these would be cryptographically hashed in the database before verification, just like passwords.
* **Rate Limiting:** Implement API rate limiting (e.g., using Bucket4j) on critical endpoints like `/auth/login` and `/auth/recover` to prevent brute-force attacks.
* **CORS Configuration:** Restrict Cross-Origin Resource Sharing (CORS) strictly to the production frontend domain instead of allowing `*`.
* **Scheduled Tasks:** Transitioning from high-frequency polling (currently set to 2-minute intervals for demonstration purposes) to production-aligned Cron expressions (e.g., nightly or weekly off-peak updates) to minimize database CPU overhead and optimize resource utilization.

## 🎨 Acknowledgements & Attribution

* **RAWG API:** All video game metadata, including titles, release dates, metacritic scores, genres, and cover images, are generously provided by the [RAWG Video Games Database API](https://rawg.io/apidocs).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

Created by Osamah Mulhem - feel free to reach out to me via email at [osamayousefosama@gmail.com](mailto:osamayousefosama@gmail.com) or connect with me on [LinkedIn](https://www.linkedin.com/in/osamah-mulhem-3821b740a/).
