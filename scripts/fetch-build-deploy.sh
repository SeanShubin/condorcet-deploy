#!/usr/bin/env bash

set -e

date
./scripts/_fetch.sh
./scripts/_build.sh
./scripts/_deploy.sh
date

say done with fetch build deploy
