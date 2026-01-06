#!/bin/bash

# Arguments
input_folder=$1
glygen_jar=$2
required_file=$3

# Docker image name
DOCKER_IMAGE="glygen-java17"

mkdir -p ./logs
rm -f ./logs/download.log

# Kill any leftover docker-run Java processes
pkill -f "docker run .* $DOCKER_IMAGE" 2>/dev/null

# --- Function to run GlyGen using Java 17 inside Docker ---
start_java_process() {
    echo "Starting GlyGen Java 17 in Docker..."

    # Run the Java process inside Docker
    # Mount the current working directory so files are written back to host
    nohup docker run --rm \
        -v "$PWD":/work \
        -w /work \
        $DOCKER_IMAGE \
        java -cp "/work/$glygen_jar" \
        uk.ac.ebi.uniprot.glygen.GlyGenToolMain "/work/$input_folder" \
        >> ./logs/download.log 2>&1 &

    PID=$!
    echo "Started Docker-based Java process with PID $PID"
    sleep 3

    # Check if the process died immediately
    if ! kill -0 "$PID" 2>/dev/null; then
        echo "ERROR: Docker Java process exited immediately. See ./logs/download.log"
        exit 1
    fi

    # Monitor folder activity
    while kill -0 "$PID" 2>/dev/null; do
        sleep 10
        if ! find "$input_folder" -mmin -2 -print -quit | grep -q .; then
            echo "No recent changes. Stopping Docker Java process…"
            kill "$PID"
            wait "$PID" 2>/dev/null
            return
        fi
    done

    echo "Docker Java process finished."
}

# --- Main Loop ---
while true; do
    echo "Checking file: ${required_file}"

    if [ -f "${required_file}" ]; then
        echo "Required file exists. Exiting."
        break
    fi

    echo "Required file missing. Starting Docker Java 17 process..."
    start_java_process
    sleep 5
done
