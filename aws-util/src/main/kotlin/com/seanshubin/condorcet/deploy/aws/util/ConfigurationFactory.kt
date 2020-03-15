package com.seanshubin.condorcet.deploy.aws.util

interface ConfigurationFactory {
  fun load(): Configuration
}