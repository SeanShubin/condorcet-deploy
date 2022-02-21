#!/usr/bin/env bash

set -e

./scripts/_build.sh
./scripts/_deploy-last.sh

say done with build deploy
