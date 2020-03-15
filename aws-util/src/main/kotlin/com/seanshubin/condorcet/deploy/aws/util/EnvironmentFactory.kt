package com.seanshubin.condorcet.deploy.aws.util

interface EnvironmentFactory {
  fun create(commandLineArguments: List<String>): Environment
}