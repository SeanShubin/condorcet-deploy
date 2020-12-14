package com.seanshubin.condorcet.aws.domain

interface Shell {
  fun execString(command: List<String>): String
}
