#!/usr/bin/env bash

aws-logout() {
    unset AWS_ACCESS_KEY_ID
    unset AWS_SECRET_ACCESS_KEY
    unset AWS_SESSION_TOKEN
}

aws-login() {
    java -jar aws-util/target/condorcet-deploy-aws-util.jar login "$@"
}
