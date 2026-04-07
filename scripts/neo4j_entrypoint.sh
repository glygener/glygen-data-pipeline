#!/bin/bash

# Validates reactome.lock exists - ensures setup-reactome was run
# Checks neostore file - ensures database files are present
# Validates database size - warns if less than 500MB (incomplete)
# Automatically cleans transaction logs - prevents store ID mismatch
# Removes checkpoint files - ensures clean startup
# Fails fast with clear error messages - stops container if validation fails
# Adds timestamps and detailed logging - easier debugging



set -e  # Exit on any error

FIRST_RUN_FLAG="/data/.neo4j_first_run_done"
REACTOME_LOCK="/data/databases/graph.db/reactome.lock"
NEOSTORE="/data/databases/graph.db/neostore"
TRANSACTION_DIR="/data/transactions/graph.db"

# Function to log with timestamp
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

# Function to validate Reactome database
validate_reactome() {
    log "Validating Reactome database..."

    # Check if the reactome.lock file exists
    if [ ! -f "$REACTOME_LOCK" ]; then
        log "ERROR: Reactome database not found at /data/databases/graph.db/"
        log "ERROR: reactome.lock file is missing"
        log "ERROR: Please run 'make setup-reactome' first"
        return 1
    fi

    # Check that store files are present and non-empty
    if [ ! -s "$NEOSTORE" ]; then
        log "ERROR: Reactome store files are missing or empty"
        log "ERROR: neostore file not found or has zero size"
        log "ERROR: Database needs to be properly initialized"
        return 1
    fi

    # Check store file size (Reactome should be at least 500MB)
    STORE_SIZE=$(du -sb /data/databases/graph.db 2>/dev/null | cut -f1)
    MIN_SIZE=524288000  # 500MB in bytes
    
    if [ -n "$STORE_SIZE" ] && [ "$STORE_SIZE" -lt "$MIN_SIZE" ]; then
        log "WARNING: Reactome database size is only $(($STORE_SIZE / 1024 / 1024))MB"
        log "WARNING: Expected at least $(($MIN_SIZE / 1024 / 1024))MB"
        log "WARNING: Database may be incomplete"
    else
        log "✓ Store file validation passed (size: $(($STORE_SIZE / 1024 / 1024))MB)"
    fi

    # Clean up transaction logs to prevent store ID mismatch
    if [ -d "$TRANSACTION_DIR" ] && [ "$(ls -A $TRANSACTION_DIR 2>/dev/null)" ]; then
        log "WARNING: Found existing transaction logs"
        log "WARNING: Removing to prevent store ID mismatch..."
        rm -rf ${TRANSACTION_DIR}/*
        log "✓ Transaction logs cleaned"
    fi

    # Remove checkpoint and transaction files from database directory
    if [ -f "/data/databases/graph.db/checkpoint.0" ]; then
        log "Removing old checkpoint file..."
        rm -f /data/databases/graph.db/checkpoint.0
    fi
    
    if [ -f "/data/databases/graph.db/neostore.transaction.db.0" ]; then
        log "Removing old transaction file..."
        rm -f /data/databases/graph.db/neostore.transaction.db.0
    fi

    log "✓ Reactome validation completed successfully"
    return 0
}

# First run initialization
if [ ! -f "$FIRST_RUN_FLAG" ]; then
    log "First run detected. Starting Neo4j for initial setup..."

    /docker-entrypoint.sh neo4j &
    NEO4J_PID=$!

    log "Waiting for Neo4j to initialize..."
    sleep 30

    log "Stopping Neo4j after first boot..."
    kill -SIGTERM "$NEO4J_PID"
    wait "$NEO4J_PID"

    log "Marking first run complete..."
    touch "$FIRST_RUN_FLAG"

    log "Exiting to trigger restart..."
    exit 1
fi

# Validate Reactome database before starting
log "Second or later run. Validating Reactome database..."
if ! validate_reactome; then
    log "FATAL: Reactome database validation failed"
    log "FATAL: Cannot start Neo4j without valid Reactome data"
    log "FATAL: Please fix the issues above and restart"
    exit 1
fi

log "Starting Neo4j normally..."
exec /docker-entrypoint.sh neo4j