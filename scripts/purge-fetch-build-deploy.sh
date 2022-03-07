#!/usr/bin/env bash

set -e

date
./scripts/_purge.sh
./scripts/_fetch.sh
./scripts/_build.sh
./scripts/_deploy.sh
date

say done with purge fetch build deploy
