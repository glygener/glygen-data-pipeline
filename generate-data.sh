#!/bin/bash

set -e

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

run() {
    ./run.sh "$@"
}

log "Ensuring network exists..."
docker network inspect glygennet >/dev/null 2>&1 || \
    docker network create --driver bridge glygennet

log "Building glygen image locally..."
docker compose build glygen

log "Starting glygen service..."
docker compose up glygen -d

log "Running setup-reactome..."
run make setup-reactome

log "Starting neo4j service..."
docker compose up neo4j -d --build

log "Updating java code"
run make generate-glygenjar -B

log "Testing neo4j connection"
run java -cp ./glygen/target/glygen-2024.6-SNAPSHOT.jar  uk.ac.ebi.uniprot.glygen.TestNeo4jConnection

log "Generating data..."
run make all