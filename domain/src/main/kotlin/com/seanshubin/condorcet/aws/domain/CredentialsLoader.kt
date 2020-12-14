package com.seanshubin.condorcet.aws.domain

class CredentialsLoader(private val commandLineArguments: Array<String>,
                        private val credentialsFactory: CredentialsFactory) :Runnable {
  override fun run() {
    val profileName = commandLineArguments.getOrNull(0)
        ?: throw RuntimeException("Expected profile name as the first argument")
    val token = commandLineArguments.getOrNull(1)
        ?: throw RuntimeException("Expected mfa token name as the second argument")
    credentialsFactory.createAndStoreInCache(profileName, token)
  }
}