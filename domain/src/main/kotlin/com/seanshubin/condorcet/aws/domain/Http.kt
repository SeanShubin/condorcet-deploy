package com.seanshubin.condorcet.aws.domain

interface Http {
  fun getString(uriString: String): String
}
