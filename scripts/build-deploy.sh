#!/usr/bin/env bash

set -e

date
./scripts/_build.sh
./scripts/_deploy.sh
date

say done with build deploy
