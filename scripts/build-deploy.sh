#!/usr/bin/env bash

set -e

./scripts/_build.sh
./scripts/_deploy.sh

say done with build deploy
