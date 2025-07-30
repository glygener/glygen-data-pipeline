#!/bin/bash

#weird scenario where it only works after first execution
FIRST_RUN_FLAG="/data/.neo4j_first_run_done"

if [ ! -f "$FIRST_RUN_FLAG" ]; then
  echo "First run detected. Starting Neo4j for initial setup..."

  /docker-entrypoint.sh neo4j &

  NEO4J_PID=$!

  echo "Waiting for Neo4j to initialize..."
  sleep 30

  echo "Stopping Neo4j after first boot..."
  kill -SIGTERM "$NEO4J_PID"

  wait "$NEO4J_PID"

  echo "Marking first run complete..."
  touch "$FIRST_RUN_FLAG"

  echo "Exiting to trigger restart..."
  exit 1
fi

echo "Second or later run. Starting Neo4j normally..."
exec /docker-entrypoint.sh neo4j
