#!/bin/bash

set -e

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

run() {
    ./run.sh "$@"
}
rm -rf reactome/
docker compose down -v --remove-orphans
docker compose up glygen -d --build
run make setup-reactome
docker compose up neo4j -d --build
log "Generating data."
run make all
log "All tasks completed successfully."
