#!/bin/bash
set -e
export MSYS_NO_PATHCONV=1
docker run -it -v $PWD/:/work -w /work --rm --network glygennet glygen "$@"
