package com.seanshubin.condorcet.deploy.domain

interface Shell {
  fun execString(command: List<String>): String
}
