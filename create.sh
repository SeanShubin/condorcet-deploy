#!/usr/bin/env bash

set -ex

mvn package
time cdk deploy --require-approval never
