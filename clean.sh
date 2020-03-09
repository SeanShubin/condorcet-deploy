#!/usr/bin/env bash

set -ex

time cdk destroy --force
mvn clean
