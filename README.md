# Blur (Racing Game) - Backend

Spring Boot backend rebuilt to follow the clinical-management backend architecture:
- JWT authentication (access + refresh tokens)
- BCrypt password hashing
- Role + permission authorization
- Global exception handling + standardized API responses
- Docker deployment

## Requirements
- Java 21
- Maven (wrapper included)

## Run locally (with local Postgres)

```bash
cd burApp
cp env.template .env
# edit .env if needed

docker-compose up -d
```

Backend runs on:
- http://localhost:8080

Swagger:
- http://localhost:8080/swagger-ui.html

## Run locally with Neon (remote DB) - recommended

The backend automatically loads a local `.env` file (not committed) via:
`spring.config.import=optional:file:.env[.properties]`

1) Create `.env` from template:

```bash
cd burApp
cp env.template .env
```

2) Edit `.env` and set the real Neon password:

```bash
DATABASE_URL=jdbc:postgresql://ep-falling-cherry-a2zt3yfx-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&channel_binding=require
DB_USERNAME=neondb_owner
DB_PASSWORD=YOUR_NEON_PASSWORD
```

3) Run:

```bash
mvn spring-boot:run
```

## Run with Docker using Neon

```bash
cd burApp
cp env.template .env
# edit DB_PASSWORD and JWT_SECRET

docker compose -f docker-compose.remote-db.yml up -d --build
```

> On Windows Git Bash, prefer `docker compose` (not `docker-compose`) and avoid copy/pasting extra characters like `│`.

## Default bootstrap
On first start, the backend creates:
- Roles: GREAT_ADMIN, PARTY_MANAGER, RACER
- Default admin:
  - username: admin
  - password: admin123

**Change the default password after first login**.

## Main endpoints
- Auth:
  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `POST /api/auth/refresh`

- Users (API prefix):
  - `GET /api/v1/users`
  - `POST /api/v1/users`
  - `GET /api/v1/users/{id}`

- Party:
  - `GET /api/v1/parties/today`

- Race:
  - `POST /api/v1/races?partyId=...&attributionType=PER_USER|ALL_USERS`
  - `POST /api/v1/races/{raceId}/start`

- Score:
  - `POST /api/v1/scores`

## Notes
- CORS allows localhost:3000 by default.
- All endpoints (except `/api/auth/**` and swagger/docs) require `Authorization: Bearer <token>`.
