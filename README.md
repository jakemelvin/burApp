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

## Default bootstrap
On first start, the backend creates:
- Roles: GREAT_ADMIN, PARTY_MANAGER, RACER
- Default admin:
  - username: admin
  - password: admin123

**Change the default password after first login**.

## Initialize Game Data (Cars, Maps, Race Parameters)

Before you can create races, you need to populate the database with game data. The data is stored in CSV files under `scripts/data/`:

```
scripts/
├── data/
│   ├── cars.csv            # 15 racing vehicles
│   ├── cards.csv           # 30 maps/tracks
│   └── race_parameters.csv # 13 game modifiers/power-ups
└── init-game-data.sh       # Import script
```

### Using the initialization script

```bash
cd burApp/scripts

# Make the script executable (first time only)
chmod +x init-game-data.sh

# Interactive menu (recommended)
./init-game-data.sh

# Or import specific data directly:
./init-game-data.sh all              # Import all data
./init-game-data.sh cars             # Import only cars
./init-game-data.sh cards            # Import only cards (maps/tracks)
./init-game-data.sh race_parameters  # Import only race parameters

# With custom database connection:
./init-game-data.sh -h localhost -p 5432 -d blurdb -U postgres all

# Using environment variables:
DB_PASSWORD=yourpassword ./init-game-data.sh all
```

### Interactive Menu

When running without arguments, the script shows an interactive menu:

```
Select what to import:

  1) Import ALL data (cars, cards, race_parameters)
  2) Import Cars only
  3) Import Cards (maps/tracks) only
  4) Import Race Parameters only
  5) Show current database counts
  6) Exit
```

### Adding new game data

To add new cars, maps, or race parameters:

1. Edit the corresponding CSV file in `scripts/data/`:
   - `cars.csv` - Format: `"id","image_url","name"`
   - `cards.csv` - Format: `"id","image_url","location","track"`
   - `race_parameters.csv` - Format: `"id","download_url","is_active","name"`

2. Run the import script for that data type:
   ```bash
   ./init-game-data.sh cars  # or cards, race_parameters, all
   ```

The script is idempotent - it won't create duplicates if run multiple times (checks by name).

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
- CORS is configurable via environment variables in `.env`:
  - `CORS_ALLOWED_ORIGINS` (comma-separated)
  - `CORS_ALLOWED_METHODS`
  - `CORS_ALLOWED_HEADERS`
  - `CORS_EXPOSED_HEADERS`
  - `CORS_ALLOW_CREDENTIALS`
  - `CORS_MAX_AGE`
- Defaults allow localhost:3000,3001. Override in your `.env` as needed.
- All endpoints (except `/api/auth/**` and swagger/docs) require `Authorization: Bearer <token>`.
