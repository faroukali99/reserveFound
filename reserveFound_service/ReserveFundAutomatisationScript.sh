#!/bin/bash

# Reserve Fund Service Automation Script
# This script automates the build, test, and deployment process for the Reserve Fund Service.

PROJECT_NAME="reserveFound_service"
IMAGE_NAME="lidcoin/reserve-fund-service"
VERSION="0.0.1-SNAPSHOT"
CONTAINER_NAME="reserve-fund-service"
PORT="8080"

echo "=================================================="
echo "   Starting Automation for $PROJECT_NAME"
echo "=================================================="

# 1. Clean and Build the Project
echo "[1/5] Cleaning and Building the project..."
if ./mvnw clean package -DskipTests; then
    echo "Build successful."
else
    echo "Build failed. Exiting."
    exit 1
fi

# 2. Run Tests (Optional - can be enabled if needed)
# echo "[2/5] Running Tests..."
# if ./mvnw test; then
#     echo "Tests passed."
# else
#     echo "Tests failed. Exiting."
#     exit 1
# fi

# 3. Build Docker Image
echo "[3/5] Building Docker Image..."
if docker build -t $IMAGE_NAME:$VERSION .; then
    echo "Docker image built successfully: $IMAGE_NAME:$VERSION"
else
    echo "Docker build failed. Exiting."
    exit 1
fi

# 4. Stop and Remove Existing Container (if any)
echo "[4/5] Checking for existing container..."
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "Stopping running container..."
    docker stop $CONTAINER_NAME
fi

if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "Removing existing container..."
    docker rm $CONTAINER_NAME
fi

# 5. Run the New Container
echo "[5/5] Running the new container..."
if docker run -d -p $PORT:8080 --name $CONTAINER_NAME $IMAGE_NAME:$VERSION; then
    echo "Container started successfully."
    echo "App is running at http://localhost:$PORT"
else
    echo "Failed to start container. Exiting."
    exit 1
fi

echo "=================================================="
echo "   Automation Completed Successfully!"
echo "=================================================="
