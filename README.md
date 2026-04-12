# E-Commerce Platform

Spring Boot microservices for an e-commerce backend: **Netflix Eureka** discovery, **Spring Cloud Gateway** (JWT validation), **Auth** (Spring Security, JWT, PostgreSQL), and domain services (product, order, payment, notification). A **Config Server** serves shared configuration from the classpath.

---

## Architecture

The diagram below shows how clients reach the API Gateway, how services register with Eureka, and how the auth flow fits in.

![E-Commerce microservices architecture](ecommerce_microservices_architecture.svg)

*Figure: E-Commerce Microservices Architecture — API Gateway, core services, messaging, databases, and infrastructure (see `ecommerce_microservices_architecture.svg` in the repository root).*

---

## Tech stack

| Area | Technology |
|------|------------|
| Runtime | Java 17+ |
| Framework | Spring Boot 4.x, Spring Cloud 2025.x |
| API edge | Spring Cloud Gateway (WebFlux) |
| Service discovery | Netflix Eureka |
| Central config | Spring Cloud Config (native profile) |
| Auth | Spring Security, JWT (JJWT), BCrypt |
| Auth persistence | PostgreSQL (`ecommerce` database) |
| Build | Maven (wrapper included) |

---

## Service ports

| Service | Port | Role |
|---------|------|------|
| API Gateway | **8080** | Public HTTP entry; JWT filter; routes `/auth/**` and discovery-based routes |
| Auth Service | **8081** | `POST /auth/register`, `POST /auth/login` |
| Eureka (Service Registry) | **8761** | Service discovery UI and registry |
| Config Server | **8888** | Optional central configuration |
| Product Service | 9102 | Catalog (stub) |
| Order Service | 9103 | Orders (stub) |
| Payment Service | 9104 | Payments (stub) |
| Notification Service | 9105 | Notifications (stub) |

Through the gateway, clients typically use **port 8080** (not direct service ports). Auth endpoints are also available as `http://localhost:8080/auth/...` when the gateway is running.

---

## Prerequisites

1. **JDK 17 or newer** (the parent POM targets Java 17).
2. **Maven** — or use the included **`./mvnw`** (Unix/macOS) / **`mvnw.cmd`** (Windows).
3. **PostgreSQL** — required by **auth-service**. Easiest: start the DB with Docker (see below). Or install PostgreSQL locally and use the same host, port, database, and credentials.
4. **Docker** (recommended) — runs PostgreSQL from `docker-compose.yml` so you avoid `Connection refused` on `localhost:5432`.

---

## Infrastructure

### PostgreSQL (auth-service and Docker)

**Recommended:** start PostgreSQL from the repo root:

```bash
docker compose up -d postgres
```

This maps **5432** on your machine to PostgreSQL **16**, user/password `postgres` / `postgres`, database **`ecommerce`**. **auth-service** uses this database for the `users` table (Hibernate `ddl-auto: update` creates it).

```bash
docker compose up -d
```

starts the same service (there is only PostgreSQL in Compose).

If you see **`Connection refused`** to `localhost:5432`, start the container above or point **`PGHOST`** / **`PGPORT`** at your own PostgreSQL instance.

---

## Environment variables

| Variable | Used by | Purpose |
|----------|---------|---------|
| `JWT_SECRET` | **auth-service**, **api-gateway** | Shared HMAC secret for signing and validating JWTs. **Must be identical** on both. Minimum length suitable for HS256 (use a long random string in production). |
| `PGHOST` | **auth-service** | PostgreSQL host (default `localhost`; use `postgres` if the app runs in the same Docker network as Compose). |
| `PGPORT` | **auth-service** | PostgreSQL port (default `5432`). |
| `PGDATABASE` | **auth-service** | Database name (default `ecommerce`, matching `docker-compose.yml`). |
| `PGUSER` / `PGPASSWORD` | **auth-service** | Credentials (defaults `postgres` / `postgres`). |

If unset, both services fall back to the default in `application.yml` (development only).

---

## Build the whole project

From the repository root:

```bash
./mvnw clean install
```

On Windows:

```bash
mvnw.cmd clean install
```

This builds all modules and runs their tests. To skip tests:

```bash
./mvnw clean install -DskipTests
```

---

## How to run (local development)

Start processes in **separate terminals** (order matters: registry first, then services that register with Eureka, then the gateway).

### 1. Eureka (Service Registry)

```bash
./mvnw -pl service-registry spring-boot:run
```

Open the dashboard: [http://localhost:8761](http://localhost:8761)

### 2. Config Server (optional)

```bash
./mvnw -pl config-server spring-boot:run
```

### 3. PostgreSQL

Start the database before auth-service (for example `docker compose up -d postgres`) and wait until it is healthy.

### 4. Auth Service

```bash
./mvnw -pl auth-service spring-boot:run
```

Direct base URL: `http://localhost:8081`

### 5. Domain services (stubs)

```bash
./mvnw -pl product-service spring-boot:run
./mvnw -pl order-service spring-boot:run
./mvnw -pl payment-service spring-boot:run
./mvnw -pl notification-service spring-boot:run
```

### 6. API Gateway (last)

```bash
./mvnw -pl api-gateway spring-boot:run
```

Public entry point: **http://localhost:8080**

---

## API quick reference

### Register (via gateway)

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password12"}'
```

### Login (via gateway)

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password12"}'
```

Response shape (fields may vary slightly):

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400
}
```

### Calling a protected downstream route

Most paths through the gateway (for example `http://localhost:8080/product-service/...`) require a valid **Bearer** token. Obtain a token from `/auth/login`, then:

```bash
curl -s "http://localhost:8080/product-service/actuator/health" \
  -H "Authorization: Bearer <accessToken>"
```

The gateway validates the JWT and forwards identity headers such as `X-User-Id` and `X-User-Email` to downstream services. Only specific public paths (for example `/auth/register`, `/auth/login`, and the gateway’s own `/actuator/**`) skip JWT validation.

### Discovery-style URLs

With Eureka’s lower-case service id, routes like `http://localhost:8080/<service-id>/...` are available (for example `http://localhost:8080/auth-service/...`). Public auth paths are whitelisted for both `/auth/**` and `/auth-service/auth/**` on the gateway.

---

## Testing

Run all module tests from the root:

```bash
./mvnw test
```

Or a single module:

```bash
./mvnw -pl auth-service test
```

---

## Repository layout

```
ecommerce-platform/
├── api-gateway/          # Spring Cloud Gateway + JWT filter
├── auth-service/         # JWT auth, PostgreSQL users
├── config-server/
├── service-registry/     # Eureka
├── product-service/
├── order-service/
├── payment-service/
├── notification-service/
├── ecommerce_microservices_architecture.svg
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Git workflow (clone, commit, push)

Clone:

```bash
git clone <your-remote-url>
cd ecommerce-platform
```

Stage and commit documentation changes:

```bash
git add README.md ecommerce_microservices_architecture.svg
git commit -m "docs: add README and architecture diagram"
git push origin main
```

Replace `main` with your default branch name if different.

---

## License

Specify your license here if applicable.
