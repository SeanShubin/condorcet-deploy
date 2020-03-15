package com.seanshubin.condorcet.deploy.aws.util

interface Command {
  fun exec(environment: Environment)
}
