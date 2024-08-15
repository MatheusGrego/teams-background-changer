#!/bin/bash

# Define variables
REPO_NAME="MatheusVict/teams-background-changer"
GITHUB_API_URL="https://api.github.com/repos/${REPO_NAME}/releases/latest"

# Fetch the latest release info
RELEASE_INFO=$(curl -s ${GITHUB_API_URL})

# Extract the URL of the JAR file asset
# Note: This assumes the JAR file is the first asset in the array
ASSET_URL=$(echo $RELEASE_INFO | jq -r '.assets[0].browser_download_url')

# Download the JAR file
curl -L ${ASSET_URL} -o linux-images-server-0.0.1-SNAPSHOT.jar
# Run the JAR file
java -jar linux-images-server-0.0.1-SNAPSHOT.jar

