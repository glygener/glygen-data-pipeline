#!/bin/bash
set -e
export MSYS_NO_PATHCONV=1
docker run -v $PWD/:/work -w /work --rm --network glygennet glygen "$@"
