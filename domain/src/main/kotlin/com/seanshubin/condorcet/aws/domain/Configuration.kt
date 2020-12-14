package com.seanshubin.condorcet.aws.domain

data class Configuration(val account: String,
                         val region: String,
                         val useSessionToken: Boolean)
