package com.seanshubin.condorcet.aws.domain

import com.seanshubin.condorcet.aws.domain.Credentials

interface CredentialsFactory {
  fun createAndStoreInCache(profileName: String, token: String): Credentials
  fun loadFromCache(profileName: String): Credentials
}
