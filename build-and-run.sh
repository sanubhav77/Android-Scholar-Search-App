#!/bin/bash
set -e

CONTAINER_NAME="ds-project-04-container"
IMAGE_NAME="ds-project-04"

echo "Starting build and deployment process..."

# Step 1: make sure the docker daemon is actually running
if ! docker info > /dev/null 2>&1; then
    echo "Step 1: Starting Docker daemon..."
    dockerd > /tmp/docker.log 2>&1 &
    for i in {1..30}; do
        if docker info > /dev/null 2>&1; then break; fi
        sleep 1
    done
fi

# Step 2: build the image
echo "Step 2: Building Docker image..."
docker build -t "$IMAGE_NAME" .

# Step 3: remove any previous container, then run fresh
echo "Step 3: Removing any previous container..."
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo "Step 4: Running Docker container..."
docker run -d --name "$CONTAINER_NAME" -p 8080:8080 "$IMAGE_NAME"

echo ""
echo "Done. Tail the Tomcat log with:  docker logs -f $CONTAINER_NAME"
