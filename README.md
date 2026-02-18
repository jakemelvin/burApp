# Blur Racing Game - Backend API

A production-ready Spring Boot backend for the Blur multiplayer racing party game, featuring JWT authentication, role-based access control, and comprehensive race management capabilities.

---

## 🎯 Vision & Open-Source Mission

Blur Backend powers a collaborative racing game experience where players can create daily parties, compete in races, and track their performance. This project is built with scalability, security, and community contribution in mind.

**Our mission**: Provide a robust, well-documented API that enables developers to build engaging multiplayer racing experiences while learning modern Spring Boot best practices.

---

## 🚀 Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Spring Boot 3.4.3 |
| **Language** | Java 21 |
| **Database** | PostgreSQL 17 (MySQL supported) |
| **Security** | Spring Security + JWT (access & refresh tokens) |
| **ORM** | Spring Data JPA + Hibernate |
| **API Documentation** | Swagger/OpenAPI (SpringDoc) |
| **Build Tool** | Maven 3.x |
| **Password Hashing** | BCrypt |
| **Object Mapping** | MapStruct 1.5.5 |
| **Validation** | Hibernate Validator |
| **Containerization** | Docker + Docker Compose |

---

## 📁 Project Structure

```
burApp/
├── src/main/java/com/packt/blurApp/
│   ├── config/                    # Application configuration
│   │   ├── security/              # JWT, filters, security config
│   │   └── DataInitializer.java  # Bootstrap data on startup
│   ├── controller/                # REST API endpoints
│   │   ├── auth/                  # Authentication endpoints
│   │   ├── CarController.java    # Vehicle management
│   │   ├── CardController.java   # Map/track management
│   │   ├── PartyController.java  # Party/session management
│   │   ├── RaceController.java   # Race lifecycle management
│   │   ├── ScoreController.java  # Score tracking
│   │   └── UserController.java   # User management
│   ├── dto/                       # Data transfer objects
│   ├── exceptions/                # Custom exceptions + global handler
│   ├── mapper/                    # MapStruct entity ↔DTO mappers
│   ├── model/                     # JPA entities
│   │   └── enums/                 # Enumerations (RaceStatus, RoleType, etc.)
│   ├── repository/                # Spring Data JPA repositories
│   ├── response/                  # Standardized API response wrappers
│   └── service/                   # Business logic layer
│       ├── car/                   # Car service
│       ├── party/                 # Party service
│       ├── race/                  # Race orchestration
│       ├── score/                 # Scoring logic
│       ├── security/              # Auth & JWT services
│       └── user/                  # User management
├── src/main/resources/
│   └── application.properties     # Spring configuration
├── scripts/                       # Database utilities
│   ├── data/                      # CSV seed data
│   │   ├── cars.csv              # 15 racing vehicles
│   │   ├── cards.csv             # 30 maps/tracks
│   │   └── race_parameters.csv   # 13 power-ups/bonuses
│   ├── init-game-data.sh         # Interactive game data import script
│   └── init-fresh-database.sql   # Fresh database setup
├── docker-compose.yml             # Local PostgreSQL + app stack
├── Dockerfile                     # Multi-stage production build
├── env.template                   # Environment variables template
└── pom.xml                        # Maven dependencies
```

---

## 🛠️ Local Development Setup

### Prerequisites

- **Java 21** (use [SDKMAN](https://sdkman.io/) for easy version management)
- **Maven 3.8+** (included via Maven Wrapper `./mvnw`)
- **PostgreSQL 17** (or use Docker Compose)
- **Git**

### Step-by-Step Setup

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd burApp
```

#### 2. Configure Environment Variables

```bash
# Copy the template
cp env.template .env

# Edit .env with your database credentials
nano .env  # or use your preferred editor
```

**Important**: The `.env` file is automatically loaded by Spring Boot via:
```properties
spring.config.import=optional:file:.env[.properties]
```

#### 3. Choose Your Database Option

**Option A: Docker Compose (Recommended for quick start)**

```bash
docker-compose up -d blur-db
```

This starts PostgreSQL on `localhost:5432` with credentials from `.env`.

**Option B: Use Remote Database (Neon, AWS RDS, etc.)**

Edit `.env` to point to your remote PostgreSQL instance:

```bash
DATABASE_URL=jdbc:postgresql://your-db-host:5432/blurdb?sslmode=require
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

**Option C: Local PostgreSQL**

Install PostgreSQL locally and create the database:

```bash
psql -U postgres
CREATE DATABASE blurdb;
\q
```

#### 4. Run the Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using system Maven
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

#### 5. Initialize Game Data

Before creating races, populate the database with game assets:

```bash
cd scripts
chmod +x init-game-data.sh
./init-game-data.sh
```

**Interactive Menu:**
- Import ALL data (cars, maps, power-ups)
- Import specific data types
- Check current database counts

**Direct Import:**
```bash
./init-game-data.sh all              # Import everything
./init-game-data.sh cars             # Cars only
./init-game-data.sh cards            # Maps/tracks only
./init-game-data.sh race_parameters  # Power-ups only
```

#### 6. Access API Documentation

**Swagger UI**: http://localhost:8080/swagger-ui.html  
**OpenAPI Spec**: http://localhost:8080/v3/api-docs

---

## 🔐 Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `DATABASE_URL` | JDBC connection string | `jdbc:postgresql://localhost:5432/blurdb` | Yes |
| `DB_USERNAME` | Database username | `postgres` | Yes |
| `DB_PASSWORD` | Database password | `postgres` | Yes |
| `JWT_SECRET` | Secret key for JWT signing | Auto-generated | **Yes (Production)** |
| `JWT_EXPIRATION` | Access token expiration (ms) | `86400000` (24h) | No |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7d) | No |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins | `http://localhost:3000,http://localhost:3001` | No |
| `CORS_ALLOWED_METHODS` | Allowed HTTP methods | `GET,POST,PUT,DELETE,PATCH,OPTIONS` | No |
| `CORS_ALLOW_CREDENTIALS` | Allow credentials | `true` | No |

**⚠️ Security Warning**: Always use a strong, unique `JWT_SECRET` in production. Generate one with:

```bash
openssl rand -base64 64
```

---

## 🏃 Running the Project

### Development Mode

```bash
./mvnw spring-boot:run
```

Hot-reload enabled with Spring Boot DevTools (modify `.properties` if needed).

### Production Build

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/blurApp-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

**Build and run entire stack:**

```bash
docker-compose up --build
```

**Run backend only (with external DB):**

```bash
docker build -t blur-backend .
docker run -p 8080:8080 --env-file .env blur-backend
```

### Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw clean test jacoco:report
```

---

## 🌿 Branching Strategy

We follow a **Git Flow** variant optimized for collaborative development:

| Branch | Purpose | Protected | Merge Target |
|--------|---------|-----------|--------------|
| `main` | **Production-ready code**. Deployed to production. Always stable. | ✅ Yes | - |
| `develop` | **Integration branch**. Latest development changes. Pre-production testing. | ✅ Yes | `main` |
| `feature/*` | **New features**. Branch from `develop`. E.g., `feature/race-leaderboard` | ❌ No | `develop` |
| `fix/*` | **Bug fixes**. Branch from `develop`. E.g., `fix/score-calculation` | ❌ No | `develop` |
| `hotfix/*` | **Urgent production fixes**. Branch from `main`. E.g., `hotfix/jwt-expiration` | ❌ No | `main` + `develop` |
| `release/*` | **Release preparation**. Branch from `develop`. E.g., `release/v1.2.0` | ❌ No | `main` + `develop` |

### Why This Structure?

- **`main`**: Single source of truth for what's in production
- **`develop`**: Ongoing work that's ready for QA/staging
- **`feature/*`**: Isolate new development without breaking `develop`
- **`fix/*`**: Clearly distinguish fixes from features
- **`hotfix/*`**: Fast-track critical fixes without waiting for `develop`
- **`release/*`**: Final QA, version bumps, changelog updates before production

### Example Workflow

```bash
# Start a new feature
git checkout develop
git pull origin develop
git checkout -b feature/add-race-replays

# Work on feature, commit changes
git add .
git commit -m "feat: add race replay storage and retrieval"

# Push and open PR to develop
git push origin feature/add-race-replays
```

---

## 🤝 Contribution Workflow

We welcome contributions! Follow these steps to contribute effectively:

### 1. Use GitHub Issues

Before writing code, **create or find an issue** describing the feature or bug:

- **Bug reports**: Include steps to reproduce, expected vs actual behavior
- **Feature requests**: Describe the problem it solves and proposed solution
- **Discussions**: For architectural decisions or major changes

**Labels we use:**
- `bug` - Something isn't working
- `enhancement` - New feature or improvement
- `documentation` - Documentation improvements
- `good first issue` - Beginner-friendly tasks
- `help wanted` - Community input needed
- `priority:high` - Critical issues

### 2. Fork the Repository

```bash
# Fork via GitHub UI, then clone your fork
git clone https://github.com/YOUR_USERNAME/blur-backend.git
cd blur-backend
git remote add upstream https://github.com/ORIGINAL_OWNER/blur-backend.git
```

### 3. Create a Branch

```bash
# Sync with upstream first
git checkout develop
git pull upstream develop

# Create feature/fix branch
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-description
```

### 4. Make Changes

- Write clean, well-documented code
- Follow existing code style (see Code Standards below)
- Add tests for new functionality
- Update relevant documentation

### 5. Commit Your Changes

Use [Conventional Commits](https://www.conventionalcommits.org/):

```bash
git commit -m "feat: add race replay endpoint"
git commit -m "fix: resolve score calculation rounding error"
git commit -m "docs: update API authentication section"
git commit -m "test: add unit tests for PartyService"
```

**Commit types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation only
- `style:` - Code style changes (formatting, no logic change)
- `refactor:` - Code restructuring without changing behavior
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks (dependencies, build config)

### 6. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a PR on GitHub targeting the `develop` branch.

---

## 🔍 Pull Request & Review Process

### PR Requirements

✅ **Your PR must:**
- Reference the related issue (e.g., "Closes #123")
- Include a clear description of changes
- Pass all CI checks (tests, linting)
- Not introduce breaking changes without discussion
- Update documentation if needed
- Add tests for new features

### PR Template

```markdown
## Description
Brief summary of changes and motivation.

## Related Issue
Closes #123

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing performed
- [ ] All tests pass locally

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-reviewed code
- [ ] Commented complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
```

### Review Process

1. **Automated Checks**: CI must pass (builds, tests, linting)
2. **Code Review**: At least **one maintainer approval** required
3. **Discussion**: Address review comments and requested changes
4. **Merge**: Maintainer merges into `develop` using **squash merge**

### Merge Strategy

- **Feature/Fix PRs → `develop`**: Squash merge (clean history)
- **`develop` → `main`**: Merge commit (preserve release history)
- **Hotfix PRs → `main`**: Merge commit, then cherry-pick to `develop`

---

## 📐 Code Standards & Collaboration Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Naming conventions**:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`
- **Annotations**: Place above method/class with no blank line
- **JavaDoc**: Required for public APIs and complex methods

### Architecture Principles

- **Service Layer**: Business logic belongs in services, not controllers
- **DTOs**: Never expose entities directly in APIs
- **Validation**: Use `@Valid` annotations on DTOs
- **Error Handling**: Throw custom exceptions, let `GlobalExceptionHandler` handle responses
- **Transactions**: Annotate service methods with `@Transactional` where needed
- **Repository Naming**: Use descriptive query method names (`findByUserNameAndEnabled`)

### Testing Guidelines

- **Unit Tests**: Test services in isolation with mocked dependencies
- **Integration Tests**: Test controller → service → repository flow
- **Naming**: `testMethodName_Scenario_ExpectedResult()`
- **Coverage Target**: Aim for 80%+ on service layer

### Database Migrations

- Schema changes are managed via `spring.jpa.hibernate.ddl-auto=update` (development)
- For production, consider versioned migration tools (Flyway/Liquibase)
- Always test migrations on a copy of production data

### API Design

- **Endpoints**: Use plural nouns (`/api/v1/parties`, not `/api/v1/party`)
- **Versioning**: Prefix with `/api/v1/`
- **Status Codes**: 
  - `200` - Success
  - `201` - Created
  - `204` - No Content
  - `400` - Bad Request
  - `401` - Unauthorized
  - `403` - Forbidden
  - `404` - Not Found
  - `409` - Conflict
  - `500` - Internal Server Error
- **Response Format**: Wrap in `ApiResponse<T>` for consistency

---

## 💬 Community & Communication

### Get Involved

- **Telegram Community**: [Join our community chat](https://t.me/+b7cUePP1Q8BlMTlk)
  - Ask questions
  - Share ideas
  - Get help troubleshooting
  - Connect with other contributors

### Contact Maintainers

- **GitHub Issues**: For bug reports and feature requests
- **GitHub Discussions**: For architecture discussions and Q&A
- **Email**: mailtoteam48@gmail.com

### Recognition

We value all contributions! Contributors will be:
- Listed in `CONTRIBUTORS.md`
- Mentioned in release notes for significant features
- Credited in documentation

---

## 📜 License

[Add license information here - MIT, Apache 2.0, GPL, etc.]

If no license is specified, the project defaults to **All Rights Reserved**.

Example (if MIT):
```
MIT License - See LICENSE file for details
```

---

## 🎮 Game Flow & API Usage

### Default Users

On first startup, the backend creates:

- **Roles**: `GREAT_ADMIN`, `RACER`
- **Default Admin**:
  - Username: `admin`
  - Password: `admin123`
  - Role: `GREAT_ADMIN`

**⚠️ Change the default password immediately after first login!**

Use environment variable `RESET_ADMIN_PASSWORD=true` to reset the password back to `admin123` (development only).

### Authentication Flow

**1. Register a new user** (public endpoint):
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "player1",
  "email": "player1@example.com",
  "password": "securePassword123"
}
```

**2. Login** (public endpoint):
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**3. Use the access token** for authenticated requests:
```http
GET /api/v1/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**4. Refresh token** when access token expires:
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Party & Race Workflow

**1. Get or create today's party**:
```http
GET /api/v1/parties/today
Authorization: Bearer <token>
```

- Returns existing party if one exists for today's date
- Creates a new party if none exists
- Only one party allowed per day (enforced by unique date constraint)

**2. Join the party**:
```http
POST /api/v1/parties/{partyId}/join
Authorization: Bearer <token>
```

**3. Create a race** (party host/manager only):
```http
POST /api/v1/races?partyId=123&attributionType=PER_USER
Authorization: Bearer <token>
```

Attribution types:
- `PER_USER` - Each participant gets a random car
- `ALL_USERS` - All participants share the same random car

**4. Add participants**:
```http
POST /api/v1/races/{raceId}/participants/{userId}
Authorization: Bearer <token>
```

**5. Start the race** (assigns map, cars, score collector):
```http
POST /api/v1/races/{raceId}/start
Authorization: Bearer <token>
```

Backend automatically:
- Assigns a random map (card) from the database
- Assigns cars based on attribution type
- Randomly selects one participant as the score collector

**6. Submit scores** (only assigned score collector):
```http
POST /api/v1/scores
Authorization: Bearer <token>
Content-Type: application/json

{
  "raceId": 123,
  "userId": 456,
  "position": 1,
  "points": 100
}
```

**7. Complete the race**:
```http
POST /api/v1/races/{raceId}/complete
Authorization: Bearer <token>
```

### Role-Based Permissions

| Role | Permissions |
|------|-------------|
| `GREAT_ADMIN` | Full system access: manage users, roles, parties, races |
| `RACER` | Join parties, participate in races, view own scores |

Permissions are enforced via Spring Security `@PreAuthorize` annotations in controllers.

---

## 🐛 Troubleshooting

### Common Issues

**1. "roles_name_check constraint violation" error**

The application automatically removes this legacy constraint on startup via `DataInitializer.repairSchemaConstraints()`. If you still see this error:

```bash
# Manually drop the constraint
psql -U postgres -d blurdb
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_check;
\q
```

**2. Database connection refused**

- Verify PostgreSQL is running: `docker-compose ps` or `systemctl status postgresql`
- Check credentials in `.env` match your database
- Test connection: `psql -h localhost -U postgres -d blurdb`

**3. Port 8080 already in use**

Change the port in `.env`:
```bash
PORT=8081
BACKEND_PORT=8081
```

**4. JWT token expired**

Refresh the token using `/api/auth/refresh` endpoint with your refresh token.

**5. CORS errors in browser**

Update `CORS_ALLOWED_ORIGINS` in `.env`:
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend-domain.com
```

### Logs

Application logs are controlled via `application.properties`:

```properties
logging.level.com.packt.blurApp=INFO
```

Change to `DEBUG` for verbose output:
```properties
logging.level.com.packt.blurApp=DEBUG
```

---

## 🚢 Deployment

### Environment-Specific Configurations

Create separate `.env` files for each environment:

```bash
.env.development
.env.staging
.env.production
```

Load the appropriate file:
```bash
# Development
cp .env.development .env
./mvnw spring-boot:run

# Production
cp .env.production .env
java -jar target/blurApp-0.0.1-SNAPSHOT.jar
```

### Production Checklist

- [ ] Strong `JWT_SECRET` (64+ characters)
- [ ] Unique `DB_PASSWORD`
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (don't use `update` in prod)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS for production domain only
- [ ] Set up database backups
- [ ] Enable application monitoring (Prometheus, Grafana)
- [ ] Configure logging to external service (ELK, Splunk)
- [ ] Change default admin password
- [ ] Review and restrict API rate limits

### Docker Production Deployment

```bash
# Build optimized image
docker build -t blur-backend:v1.0.0 .

# Run with production env
docker run -d \
  --name blur-backend \
  -p 8080:8080 \
  --env-file .env.production \
  --restart unless-stopped \
  blur-backend:v1.0.0
```

---

## 📊 API Endpoints Summary

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | ❌ |
| POST | `/login` | Login and get tokens | ❌ |
| POST | `/refresh` | Refresh access token | ❌ |

### Users (`/api/v1/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all users | ✅ Admin |
| GET | `/me` | Get current user | ✅ |
| GET | `/{userId}` | Get user by ID | ✅ |
| POST | `/` | Create user | ✅ Admin |
| PUT | `/{userId}` | Update user | ✅ Admin |
| PUT | `/{userId}/role` | Assign role | ✅ Admin |
| DELETE | `/{userId}` | Delete user | ✅ Admin |

### Parties (`/api/v1/parties`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/today` | Get or create today's party | ✅ |
| GET | `/{id}` | Get party by ID | ✅ |
| GET | `/` | List all parties | ✅ |
| POST | `/{partyId}/join` | Join party | ✅ |
| POST | `/{partyId}/leave` | Leave party | ✅ |
| DELETE | `/{partyId}` | Delete party | ✅ Admin |

### Races (`/api/v1/races`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/{id}` | Get race by ID | ✅ |
| GET | `/party/{partyId}` | List races for party | ✅ |
| POST | `/` | Create race | ✅ Manager |
| POST | `/{raceId}/start` | Start race | ✅ Manager |
| POST | `/{raceId}/complete` | Complete race | ✅ Manager |
| POST | `/{raceId}/participants/{userId}` | Add participant | ✅ Manager |
| DELETE | `/{raceId}/participants/{userId}` | Remove participant | ✅ Manager |

### Scores (`/api/v1/scores`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/{id}` | Get score by ID | ✅ |
| GET | `/race/{raceId}` | List scores for race | ✅ |
| POST | `/` | Create score | ✅ Collector |
| PUT | `/{scoreId}` | Update score | ✅ Collector |
| DELETE | `/{scoreId}` | Delete score | ✅ Admin |

### Cars (`/api/v1/cars`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all cars | ✅ |

### Cards (Maps) (`/api/v1/cards`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all maps/tracks | ✅ |

### Race Parameters (`/api/v1/race-parameters`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all power-ups/bonuses | ✅ |
| GET | `/{id}` | Get parameter by ID | ✅ |

---

## 🙏 Acknowledgments

- **Spring Boot Team** - For the excellent framework
- **PostgreSQL Community** - For the robust database
- **MapStruct** - For simplifying object mapping
- **All Contributors** - Thank you for making this project better!

---

**Happy Coding! 🎮🏁**

If you find this project useful, please give it a ⭐ on GitHub!
