#!/bin/bash

input_folder=$1
glygen_jar=$2
required_file=$3

mkdir -p ./logs
pkill -f "java.*$glygen_jar"

rm -rf ./logs/download.log

# Function to start Java process
start_java_process() {
    # Start Java process in the background
    nohup java -cp "$glygen_jar" uk.ac.ebi.uniprot.glygen.GlyGenToolMain "$input_folder" >> ./logs/download.log 2>&1 &
    # Monitor the input_folder for changes every 60 seconds
    while true; do
        sleep 10  # Check every 10 seconds
        if ! find "$input_folder" -mmin -2 -print -quit | grep -q .; then
            echo "No changes detected in the last 1 minute. Killing Java process..."
            pkill -f "java.*$glygen_jar"
            break
        fi
    done
    echo "Java process finished."
}

# Continuously check for the required file and start Java process if it doesn't exist
while true; do
    echo "Checking file: ${required_file}"
    if [ -f "${required_file}" ]; then
        echo "Required file exists. Exiting loop."
        break
    else
        echo "Required file does not exist. Starting Java process..."
        start_java_process
        sleep 5 # Wait for 5 seconds before checking again
    fi
done