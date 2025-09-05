#!/bin/bash

# Oracle MCP Server Startup Script
# Provides convenient startup options for different configurations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
PROFILE="enhanced"
TRANSPORT="stdio"
ORACLE_HOST="localhost"
ORACLE_PORT="1521"
ORACLE_SID="XE"
ORACLE_USERNAME="hr"
ORACLE_PASSWORD="password"

# Function to display usage
usage() {
    echo -e "Oracle MCP Server Startup Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -p, --profile PROFILE      Set Spring profile (enhanced|enterprise|dev|prod) [default: enhanced]"
    echo "  -t, --transport TRANSPORT  Set MCP transport (stdio|rest) [default: stdio]"
    echo "  -h, --host HOST           Oracle host [default: localhost]"
    echo "  -P, --port PORT           Oracle port [default: 1521]"
    echo "  -s, --sid SID             Oracle SID [default: XE]"
    echo "  -u, --username USERNAME   Oracle username [default: hr]"
    echo "  -w, --password PASSWORD   Oracle password [default: password]"
    echo "  --help                    Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Start with default enhanced profile"
    echo "  $0 -p enterprise                     # Start with enterprise profile (75+ tools)"
    echo "  $0 -p dev -t rest                    # Start in development mode with REST API"
    echo "  $0 -h oracle-server -u system -w manager  # Connect to remote Oracle"
    echo ""
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        -t|--transport)
            TRANSPORT="$2"
            shift 2
            ;;
        -h|--host)
            ORACLE_HOST="$2"
            shift 2
            ;;
        -P|--port)
            ORACLE_PORT="$2"
            shift 2
            ;;
        -s|--sid)
            ORACLE_SID="$2"
            shift 2
            ;;
        -u|--username)
            ORACLE_USERNAME="$2"
            shift 2
            ;;
        -w|--password)
            ORACLE_PASSWORD="$2"
            shift 2
            ;;
        --help)
            usage
            exit 0
            ;;
        *)
            echo -e "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Validate profile
if [[ ! "$PROFILE" =~ ^(enhanced|enterprise|dev|prod|test)$ ]]; then
    echo -e "Error: Invalid profile '$PROFILE'. Must be one of: enhanced, enterprise, dev, prod, test"
    exit 1
fi

# Validate transport
if [[ ! "$TRANSPORT" =~ ^(stdio|rest)$ ]]; then
    echo -e "Error: Invalid transport '$TRANSPORT'. Must be one of: stdio, rest"
    exit 1
fi

# Build Oracle JDBC URL
ORACLE_URL="jdbc:oracle:thin:@$ORACLE_HOST:$ORACLE_PORT:$ORACLE_SID"

# Determine JAR file
JAR_FILE="target/mcp-oracledb-server-1.0.0-PRODUCTION.jar"
if [[ ! -f "$JAR_FILE" ]]; then
    echo -e "Error: JAR file not found: $JAR_FILE"
    echo -e "Please run 'mvn clean package' first"
    exit 1
fi

# Display startup information
echo -e " Starting Oracle MCP Server"
echo -e "Profile: $PROFILE"
echo -e "Transport: $TRANSPORT"
echo -e "Oracle URL: $ORACLE_URL"
echo -e "Username: $ORACLE_USERNAME"

# Determine tool count based on profile
case $PROFILE in
    enhanced)
        TOOL_COUNT="55+"
        ;;
    enterprise)
        TOOL_COUNT="75+"
        ;;
    *)
        TOOL_COUNT="55+"
        ;;
esac

echo -e "Tools Available: $TOOL_COUNT"
echo ""

# Set environment variables
export ORACLE_USERNAME="$ORACLE_USERNAME"
export ORACLE_PASSWORD="$ORACLE_PASSWORD"
export SPRING_DATASOURCE_URL="$ORACLE_URL"

# Build Spring profiles
if [[ "$TRANSPORT" == "stdio" ]]; then
    SPRING_PROFILES="$PROFILE,mcp"
else
    SPRING_PROFILES="$PROFILE"
fi

# Build Java command
JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dspring.profiles.active=$SPRING_PROFILES"

if [[ "$TRANSPORT" == "rest" ]]; then
    JAVA_OPTS="$JAVA_OPTS -Dserver.port=8080"
fi

# Start the server
echo -e "Starting server with profile: $SPRING_PROFILES"
echo ""

java $JAVA_OPTS -jar "$JAR_FILE"
