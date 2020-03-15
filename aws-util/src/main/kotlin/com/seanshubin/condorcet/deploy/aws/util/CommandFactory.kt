package com.seanshubin.condorcet.deploy.aws.util

interface CommandFactory {
  fun fromName(name: String): Command
}
