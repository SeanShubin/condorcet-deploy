package com.seanshubin.condorcet.aws.domain

data class Credentials(
    val accessKeyId: String,
    val secretAccessKey: String,
    val sessionToken: String,
    val signInUrl: String,
    val signInConsole: String
)
