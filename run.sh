#!/bin/bash
set -e

docker run -v $PWD/:/work -w /work --rm --network unpfwglygen_glygennet glygen "$@"
