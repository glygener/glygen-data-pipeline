#!/bin/bash

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

run() {
    ./run.sh "$@"
}
rm -rf reactome
docker compose down -v --remove-orphans
docker compose up -d --build
log "Generating data."
run make all
log "All tasks completed successfully."
