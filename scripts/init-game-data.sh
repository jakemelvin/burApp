#!/bin/bash

# ============================================
# Blur Racing Game - Database Initialization Script
# ============================================
# This script initializes the database with game data from CSV files:
# - Cars (vehicles for racing)
# - Cards (maps/tracks for racing)
# - Race Parameters (game modifiers/power-ups)
# ============================================

# Don't use set -e as we handle errors manually

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="$SCRIPT_DIR/data"

# CSV files
CARS_CSV="$DATA_DIR/cars.csv"
CARDS_CSV="$DATA_DIR/cards.csv"
RACE_PARAMS_CSV="$DATA_DIR/race_parameters.csv"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  Blur Racing Game - Database Initializer${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Check if data directory exists
if [ ! -d "$DATA_DIR" ]; then
    echo -e "${RED}Error: Data directory not found at $DATA_DIR${NC}"
    exit 1
fi

# Load environment variables from .env file
ENV_FILE="$SCRIPT_DIR/../.env"
if [ -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}Loading environment from $ENV_FILE${NC}"
    
    # Read only the required variables from .env file
    while IFS='=' read -r key value; do
        # Skip comments and empty lines
        [[ "$key" =~ ^#.*$ ]] && continue
        [[ -z "$key" ]] && continue
        
        # Remove leading/trailing whitespace and quotes
        key=$(echo "$key" | tr -d ' \r')
        value=$(echo "$value" | sed 's/^["'"'"']//;s/["'"'"']$//' | tr -d '\r')
        
        case "$key" in
            DATABASE_URL)
                DATABASE_URL="$value"
                ;;
            DB_USERNAME)
                DB_USERNAME="$value"
                ;;
            DB_PASSWORD)
                DB_PASSWORD="$value"
                ;;
        esac
    done < "$ENV_FILE"
    
    echo -e "${GREEN}✓ Loaded DATABASE_URL, DB_USERNAME, DB_PASSWORD from .env${NC}"
    echo ""
else
    echo -e "${RED}Warning: .env file not found at $ENV_FILE${NC}"
    echo -e "${YELLOW}Please create a .env file with DATABASE_URL, DB_USERNAME, and DB_PASSWORD${NC}"
    echo ""
fi

# Validate required environment variables
if [ -z "$DATABASE_URL" ]; then
    echo -e "${RED}Error: DATABASE_URL is not set${NC}"
    echo -e "${YELLOW}Please set DATABASE_URL in your .env file${NC}"
    exit 1
fi

if [ -z "$DB_USERNAME" ]; then
    echo -e "${RED}Error: DB_USERNAME is not set${NC}"
    echo -e "${YELLOW}Please set DB_USERNAME in your .env file${NC}"
    exit 1
fi

if [ -z "$DB_PASSWORD" ]; then
    echo -e "${RED}Error: DB_PASSWORD is not set${NC}"
    echo -e "${YELLOW}Please set DB_PASSWORD in your .env file${NC}"
    exit 1
fi

# Parse DATABASE_URL to extract host, port, database
# Format: jdbc:postgresql://host:port/database?params or jdbc:postgresql://host/database?params
parse_database_url() {
    # Remove jdbc: prefix if present
    local CLEAN_URL="${DATABASE_URL#jdbc:}"
    # Remove postgresql:// prefix
    local URL_WITHOUT_PROTOCOL="${CLEAN_URL#postgresql://}"
    
    # Extract host (everything before the first / or :)
    # Handle format: host/database or host:port/database
    if [[ "$URL_WITHOUT_PROTOCOL" == *":"* ]]; then
        # Has port: host:port/database
        DB_HOST=$(echo "$URL_WITHOUT_PROTOCOL" | cut -d':' -f1)
        local PORT_AND_REST=$(echo "$URL_WITHOUT_PROTOCOL" | cut -d':' -f2)
        DB_PORT=$(echo "$PORT_AND_REST" | cut -d'/' -f1)
    else
        # No port: host/database
        DB_HOST=$(echo "$URL_WITHOUT_PROTOCOL" | cut -d'/' -f1)
        DB_PORT="5432"
    fi
    
    # Extract database name (between first / and ? or end)
    DB_NAME=$(echo "$URL_WITHOUT_PROTOCOL" | cut -d'/' -f2 | cut -d'?' -f1)
}

parse_database_url

echo -e "${CYAN}Database Configuration:${NC}"
echo "  Host:     $DB_HOST"
echo "  Port:     $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User:     $DB_USERNAME"
echo ""

# Function to display usage
usage() {
    echo -e "${YELLOW}Usage:${NC}"
    echo "  $0 [COMMAND]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  all             Import all data (cars, cards, race_parameters)"
    echo "  cars            Import only cars"
    echo "  cards           Import only cards (maps/tracks)"
    echo "  race_parameters Import only race parameters"
    echo "  menu            Show interactive menu (default)"
    echo "  --help          Show this help message"
    echo ""
    echo -e "${YELLOW}Required Environment Variables (from .env file):${NC}"
    echo "  DATABASE_URL    JDBC connection URL (e.g., jdbc:postgresql://host/db?sslmode=require)"
    echo "  DB_USERNAME     Database username"
    echo "  DB_PASSWORD     Database password"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0                    # Interactive menu"
    echo "  $0 all                # Import all data"
    echo "  $0 cars               # Import only cars"
    echo "  $0 cards              # Import only cards"
    echo "  $0 race_parameters    # Import only race parameters"
}

# Parse command line arguments
COMMAND=""
while [[ $# -gt 0 ]]; do
    case $1 in
        --help|-h)
            usage
            exit 0
            ;;
        all|cars|cards|race_parameters|menu)
            COMMAND="$1"
            shift
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Check if psql is installed
check_psql() {
    if ! command -v psql &> /dev/null; then
        echo -e "${RED}Error: psql (PostgreSQL client) is not installed.${NC}"
        echo ""
        echo -e "${YELLOW}Installation instructions:${NC}"
        echo "  macOS:    brew install postgresql"
        echo "  Ubuntu:   sudo apt-get install postgresql-client"
        echo "  Windows:  Install PostgreSQL or use WSL"
        exit 1
    fi
}


# Build psql connection string
get_psql_cmd() {
    local SSL_MODE=""
    if [[ "$DB_HOST" == *"neon.tech"* ]] || [[ "$DB_HOST" == *"neon"* ]]; then
        SSL_MODE="?sslmode=require"
    fi
    echo "postgresql://$DB_USERNAME:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME$SSL_MODE"
}

# Test database connection
test_connection() {
    echo -e "${CYAN}Testing database connection...${NC}"
    local conn_str=$(get_psql_cmd)
    # Hide password in display
    local display_str=$(echo "$conn_str" | sed "s/:$DB_PASSWORD@/:****@/")
    echo -e "${YELLOW}Connection: $display_str${NC}"
    
    if psql "$conn_str" -c "SELECT 1;" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Connection successful${NC}"
        echo ""
        return 0
    else
        echo -e "${RED}✗ Connection failed${NC}"
        echo -e "${YELLOW}Trying to connect with verbose output...${NC}"
        psql "$conn_str" -c "SELECT 1;" 2>&1 || true
        return 1
    fi
}

# Count rows in a CSV file (excluding header)
count_csv_rows() {
    local file="$1"
    if [ -f "$file" ]; then
        echo $(($(wc -l < "$file") - 1))
    else
        echo 0
    fi
}

# Import cars from CSV
import_cars() {
    echo -e "${CYAN}Importing cars...${NC}"
    
    if [ ! -f "$CARS_CSV" ]; then
        echo -e "${RED}Error: Cars CSV not found at $CARS_CSV${NC}"
        return 1
    fi
    
    local conn_str=$(get_psql_cmd)
    local count=0
    local errors=0
    local total=$(count_csv_rows "$CARS_CSV")
    
    # Read file without pipe to avoid subshell
    while IFS=',' read -r id image_url name; do
        # Skip header
        if [ "$id" = '"id"' ] || [ "$id" = 'id' ]; then
            continue
        fi
        
        # Remove quotes from fields
        id=$(echo "$id" | tr -d '"' | tr -d '\r')
        image_url=$(echo "$image_url" | tr -d '"' | tr -d '\r')
        name=$(echo "$name" | tr -d '"' | tr -d '\r')
        
        # Insert if not exists
        if psql "$conn_str" -q -c "
            INSERT INTO car (name, image_url) 
            SELECT '$name', '$image_url'
            WHERE NOT EXISTS (SELECT 1 FROM car WHERE name = '$name');
        " 2>&1; then
            ((count++))
        else
            echo -e "${RED}  Error inserting car: $name${NC}"
            ((errors++))
        fi
    done < "$CARS_CSV"
    
    echo -e "${GREEN}✓ Processed $count/$total cars ($errors errors)${NC}"
}

# Import cards from CSV
import_cards() {
    echo -e "${CYAN}Importing cards (maps/tracks)...${NC}"
    
    if [ ! -f "$CARDS_CSV" ]; then
        echo -e "${RED}Error: Cards CSV not found at $CARDS_CSV${NC}"
        return 1
    fi
    
    local conn_str=$(get_psql_cmd)
    local count=0
    local errors=0
    local total=$(count_csv_rows "$CARDS_CSV")
    
    # Read file without pipe to avoid subshell
    while IFS=',' read -r id image_url location track; do
        # Skip header
        if [ "$id" = '"id"' ] || [ "$id" = 'id' ]; then
            continue
        fi
        
        # Remove quotes and carriage returns from fields
        id=$(echo "$id" | tr -d '"' | tr -d '\r')
        image_url=$(echo "$image_url" | tr -d '"' | tr -d '\r')
        location=$(echo "$location" | tr -d '"' | tr -d '\r')
        track=$(echo "$track" | tr -d '"' | tr -d '\r')
        
        # Escape single quotes for SQL
        location=$(echo "$location" | sed "s/'/''/g")
        track=$(echo "$track" | sed "s/'/''/g")
        
        # Insert if not exists
        if psql "$conn_str" -q -c "
            INSERT INTO card (location, track, image_url) 
            SELECT '$location', '$track', '$image_url'
            WHERE NOT EXISTS (SELECT 1 FROM card WHERE location = '$location' AND track = '$track');
        " 2>&1; then
            ((count++))
        else
            echo -e "${RED}  Error inserting card: $location - $track${NC}"
            ((errors++))
        fi
    done < "$CARDS_CSV"
    
    echo -e "${GREEN}✓ Processed $count/$total cards ($errors errors)${NC}"
}

# Import race parameters from CSV
import_race_parameters() {
    echo -e "${CYAN}Importing race parameters...${NC}"
    
    if [ ! -f "$RACE_PARAMS_CSV" ]; then
        echo -e "${RED}Error: Race parameters CSV not found at $RACE_PARAMS_CSV${NC}"
        return 1
    fi
    
    local conn_str=$(get_psql_cmd)
    local count=0
    local errors=0
    local total=$(count_csv_rows "$RACE_PARAMS_CSV")
    
    # Read file without pipe to avoid subshell
    while IFS=',' read -r id download_url is_active name; do
        # Skip header
        if [ "$id" = '"id"' ] || [ "$id" = 'id' ]; then
            continue
        fi
        
        # Remove quotes and carriage returns from fields
        id=$(echo "$id" | tr -d '"' | tr -d '\r')
        download_url=$(echo "$download_url" | tr -d '"' | tr -d '\r')
        is_active=$(echo "$is_active" | tr -d '"' | tr -d '\r')
        name=$(echo "$name" | tr -d '"' | tr -d '\r')
        
        # Convert is_active to boolean
        if [ "$is_active" = "true" ]; then
            is_active_bool="true"
        else
            is_active_bool="false"
        fi
        
        # Insert if not exists
        if psql "$conn_str" -q -c "
            INSERT INTO race_parameters (name, is_active, download_url) 
            SELECT '$name', $is_active_bool, '$download_url'
            WHERE NOT EXISTS (SELECT 1 FROM race_parameters WHERE name = '$name');
        " 2>&1; then
            ((count++))
        else
            echo -e "${RED}  Error inserting race parameter: $name${NC}"
            ((errors++))
        fi
    done < "$RACE_PARAMS_CSV"
    
    echo -e "${GREEN}✓ Processed $count/$total race parameters ($errors errors)${NC}"
}

# Import all data
import_all() {
    echo -e "${BLUE}Importing all game data...${NC}"
    echo ""
    
    if ! test_connection; then
        echo -e "${RED}Cannot proceed without database connection.${NC}"
        return 1
    fi
    
    import_cars
    import_cards
    import_race_parameters
}

# Show current data counts
show_data_counts() {
    echo -e "${CYAN}Current database counts:${NC}"
    
    local cars=$(psql "$(get_psql_cmd)" -t -c "SELECT COUNT(*) FROM car;" 2>/dev/null | tr -d ' ')
    local cards=$(psql "$(get_psql_cmd)" -t -c "SELECT COUNT(*) FROM card;" 2>/dev/null | tr -d ' ')
    local params=$(psql "$(get_psql_cmd)" -t -c "SELECT COUNT(*) FROM race_parameters;" 2>/dev/null | tr -d ' ')
    
    echo "  Cars:            ${cars:-0}"
    echo "  Cards (Maps):    ${cards:-0}"
    echo "  Race Parameters: ${params:-0}"
    echo ""
}

# Show CSV data counts
show_csv_counts() {
    echo -e "${CYAN}Available data in CSV files:${NC}"
    echo "  Cars:            $(count_csv_rows "$CARS_CSV")"
    echo "  Cards (Maps):    $(count_csv_rows "$CARDS_CSV")"
    echo "  Race Parameters: $(count_csv_rows "$RACE_PARAMS_CSV")"
    echo ""
}

# Interactive menu
show_menu() {
    echo -e "${YELLOW}Database Connection:${NC}"
    echo "  Host:     $DB_HOST"
    echo "  Port:     $DB_PORT"
    echo "  Database: $DB_NAME"
    echo "  User:     $DB_USERNAME"
    echo ""
    
    show_csv_counts
    
    echo -e "${YELLOW}Select what to import:${NC}"
    echo ""
    echo "  1) Import ALL data (cars, cards, race_parameters)"
    echo "  2) Import Cars only"
    echo "  3) Import Cards (maps/tracks) only"
    echo "  4) Import Race Parameters only"
    echo "  5) Show current database counts"
    echo "  6) Exit"
    echo ""
    read -p "Enter choice [1-6]: " choice
    
    case $choice in
        1)
            import_all
            ;;
        2)
            if test_connection; then
                import_cars
            fi
            ;;
        3)
            if test_connection; then
                import_cards
            fi
            ;;
        4)
            if test_connection; then
                import_race_parameters
            fi
            ;;
        5)
            if test_connection; then
                show_data_counts
            fi
            show_menu
            ;;
        6)
            echo -e "${GREEN}Goodbye!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid choice. Please try again.${NC}"
            show_menu
            ;;
    esac
}

# Main execution
check_psql

case "$COMMAND" in
    all)
        import_all
        ;;
    cars)
        if test_connection; then
            import_cars
        fi
        ;;
    cards)
        if test_connection; then
            import_cards
        fi
        ;;
    race_parameters)
        if test_connection; then
            import_race_parameters
        fi
        ;;
    menu|"")
        show_menu
        ;;
esac

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  Import completed successfully!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Start/restart the backend: mvn spring-boot:run"
echo "  2. Start the frontend: cd ../blur && npm run dev"
echo "  3. Create a party and start racing!"
