#!/bin/bash
set -e

docker run -v "$(pwd -W):/work" -w "/work" --rm --network unpfwglygen_glygennet glygen "$@"