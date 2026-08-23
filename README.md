# ShortURL — URL Shortening Service

A RESTful backend service built with Spring Boot that shortens long URLs, tracks click analytics, and generates QR codes. Designed with a layered architecture, Redis caching for fast redirects, and Swagger UI for interactive API documentation.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Analytics](#analytics)
- [Architecture Notes](#architecture-notes)

---

## Overview

ShortURL converts long URLs into compact, shareable short links. Each redirect is logged with metadata (device type, browser, OS, referrer) to power a per-link analytics dashboard. Redis is used as a cache layer to serve high-traffic redirects without hitting the database.

Key capabilities:

- Shorten a URL and receive a Base62-encoded short code
- Redirect visitors from short code to the original URL
- Set optional expiration dates on links
- Enable or disable links without deleting them
- Generate a QR code for any short link
- View detailed click analytics per link

---

## Tech Stack

| Layer            | Technology                          |
|------------------|--------------------------------------|
| Language         | Java 21                              |
| Framework        | Spring Boot 4.1.1                    |
| Web              | Spring MVC (spring-boot-starter-webmvc) |
| Persistence      | Spring Data JPA + Hibernate          |
| Database         | MySQL 8.0                            |
| Cache            | Redis 7 (Spring Data Redis Reactive) |
| Security         | Spring Security                      |
| Validation       | Jakarta Bean Validation              |
| Mapping          | MapStruct 1.5.5 + Lombok             |
| QR Code          | ZXing 3.5.3 (core + javase)          |
| API Docs         | SpringDoc OpenAPI (Swagger UI) 2.5.0 |
| Build Tool       | Maven (with Maven Wrapper)           |
| Containerization | Docker Compose                       |

---

## Project Structure

```
src/main/java/com/example/shorturl/
├── ShorturlApplication.java       # Application entry point
├── controller/
│   ├── UrlController.java         # CRUD and analytics endpoints
│   └── RedirectController.java    # Short code redirect handler
├── service/
│   ├── UrlShortenerService.java   # URL shortening business logic
│   ├── AnalyticsService.java      # Click analytics aggregation
│   └── impl/                      # Service implementations
├── entity/
│   ├── ShortUrl.java              # Short link entity
│   ├── ClickLog.java              # Click event log entity
│   └── BaseEntity.java            # Shared audit fields
├── dto/
│   ├── request/                   # Request DTOs (ShortenUrlRequest, UpdateUrlRequest)
│   └── response/                  # Response DTOs (ShortUrlResponse, ClickAnalyticsResponse)
├── repository/                    # Spring Data JPA repositories
├── mapper/                        # MapStruct mappers (entity <-> DTO)
├── config/
│   ├── SecurityConfig.java        # Spring Security configuration
│   ├── RedisConfig.java           # Redis serializer and template setup
│   ├── OpenApiConfig.java         # Swagger/OpenAPI metadata
│   ├── WebMvcConfig.java          # CORS and interceptor registration
│   ├── AsyncConfig.java           # Thread pool for async tasks
│   └── RateLimitInterceptor.java  # Request rate limiting
└── common/
    └── response/                  # Unified ApiResponse wrapper
```

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.x (or use the included `mvnw` wrapper)
- Docker and Docker Compose

### 1. Start Infrastructure Services

The `compose.yaml` file defines MySQL and Redis containers. Start them with:

```bash
docker compose up -d
```

This will start:
- MySQL 8.0 on port `3306` with database `shorturl_db`
- Redis 7 on port `6379`

### 2. Configure Database Credentials

Open `src/main/resources/application.yaml` and update the datasource credentials if needed:

```yaml
spring:
  datasource:
    username: root
    password: root
```

### 3. Run the Application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```cmd
mvnw.cmd spring-boot:run
```

The server starts at: `http://localhost:8080`

### 4. Access Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

---

## Configuration

All configuration is in `src/main/resources/application.yaml`.

| Property                  | Default Value                        | Description                              |
|---------------------------|---------------------------------------|------------------------------------------|
| `server.port`             | `8080`                                | HTTP server port                         |
| `spring.datasource.url`   | `jdbc:mysql://localhost:3306/shorturl_db` | MySQL connection URL                |
| `spring.datasource.username` | `root`                             | MySQL username                           |
| `spring.datasource.password` | `root`                             | MySQL password                           |
| `spring.jpa.hibernate.ddl-auto` | `update`                       | Schema update strategy                   |
| `spring.data.redis.host`  | `localhost`                           | Redis host                               |
| `spring.data.redis.port`  | `6379`                                | Redis port                               |
| `app.base-url`            | `http://localhost:8080/`              | Base URL prepended to generated short codes |

---

## API Reference

Base path: `/api/v1/urls`

### Shorten a URL

```
POST /api/v1/urls/shorten
```

Request body:

```json
{
  "originalUrl": "https://example.com/very/long/path",
  "expiresInDays": 30
}
```

Response: Returns the created short link with its short code.

---

### Redirect

```
GET /{shortCode}
```

Redirects the browser to the original URL. Logs the click event asynchronously.

---

### Get Link Details

```
GET /api/v1/urls/{shortCode}
```

Returns metadata for a specific short link.

---

### Get Batch Links

```
POST /api/v1/urls/batch
```

Accepts a list of short codes (used to restore link history from client-side LocalStorage) and returns their details in bulk.

Request body:

```json
["abc123", "def456", "ghi789"]
```

---

### Update a Link

```
PUT /api/v1/urls/{shortCode}
```

Supports partial updates. Fields are optional.

Request body:

```json
{
  "originalUrl": "https://new-destination.com",
  "expiresInDays": 7,
  "active": true
}
```

---

### Delete a Link

```
DELETE /api/v1/urls/{shortCode}
```

Removes the link from the database and evicts it from Redis cache.

---

### Get Click Analytics

```
GET /api/v1/urls/{shortCode}/analytics
```

Returns aggregated analytics for a link. See the [Analytics](#analytics) section for response details.

---

## Analytics

The analytics endpoint aggregates click log data and returns:

| Field             | Description                                        |
|-------------------|----------------------------------------------------|
| `totalClicks`     | Total number of clicks recorded                    |
| `uniqueVisitors`  | Count of distinct visitors (by IP or fingerprint)  |
| `deviceBreakdown` | Clicks grouped by device type (desktop, mobile, etc.) |
| `osBrakdown`      | Clicks grouped by operating system                 |
| `browserBreakdown`| Clicks grouped by browser                         |
| `referrerBreakdown`| Clicks grouped by referrer domain                 |
| `clickTimeline`   | Click counts grouped by date for time-series charts |

---

## Architecture Notes

**Caching strategy**: On redirect, the system first checks Redis for the target URL by short code. On a cache miss, it queries MySQL and warm-ups the cache for subsequent requests. On update or delete, the corresponding Redis key is evicted to prevent stale data.

**Base62 encoding**: Short codes are generated by encoding the database record ID using a Base62 alphabet (0-9, a-z, A-Z), producing compact, URL-safe identifiers.

**Asynchronous click logging**: Click events are recorded asynchronously using a dedicated thread pool (`AsyncConfig`) to avoid adding latency to the redirect response.

**Rate limiting**: A custom `RateLimitInterceptor` is registered in `WebMvcConfig` to throttle abusive clients per IP address.

**Schema management**: `ddl-auto: update` is set for development convenience. For production deployments, switch to `validate` and manage schema changes with a migration tool such as Flyway or Liquibase.

---

## License

This project is for educational and portfolio purposes.
