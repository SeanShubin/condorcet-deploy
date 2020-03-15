package com.seanshubin.condorcet.deploy.aws.util

data class Configuration(
    val mfaId: String,
    val mfaName: String,
    val account: String,
    val organization: String,
    val role: String)