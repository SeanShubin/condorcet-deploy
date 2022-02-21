#!/usr/bin/env bash

set -e

./scripts/_teardown-last.sh
./scripts/_build.sh
./scripts/_deploy-last.sh

say done with teardown build deploy
