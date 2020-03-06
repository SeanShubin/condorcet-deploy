#!/usr/bin/env bash

set -ex

cdk destroy --force
mvn clean
