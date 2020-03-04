#!/usr/bin/env bash

mvn install
cdk deploy --profile condorcet
