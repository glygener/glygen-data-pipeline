#!/bin/bash
set -e
export MSYS_NO_PATHCONV=1
docker run -i -v $PWD/:/work -w /work --rm --network glygennet glygen "$@"
