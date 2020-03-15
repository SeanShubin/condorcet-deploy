package com.seanshubin.condorcet.deploy.domain

interface Http {
  fun getString(uriString: String): String
}
