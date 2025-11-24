#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ Environment variables loaded from .env"
else
    echo "✗ .env file not found"
    exit 1
fi

# Run the application
echo "Starting application..."
./mvnw spring-boot:run
