package com.seanshubin.condorcet.aws.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seanshubin.condorcet.deploy.contract.FilesContract
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.*

class ConfigureProvision(val files: FilesContract,
                         val charset: Charset,
                         val objectMapper: ObjectMapper,
                         val profilePathName: String,
                         val propertiesPathName: String,
                         val createRunner: (ConfigurationValues) -> Runnable) : Runnable {
  override fun run() {
    val profilePath = Paths.get(profilePathName)
    val profileJson = files.readString(profilePath, charset)
    val profile = objectMapper.readValue<Profile>(profileJson)
    val propertiesPath = Paths.get(propertiesPathName)
    val properties = Properties()
    files.newBufferedReader(propertiesPath, charset).use { bufferedReader ->
      properties.load(bufferedReader)
    }
    val databasePassword = properties.getProperty("condorcet-db-password")
    val configurationValues = ConfigurationValuesImpl(databasePassword, profile.account)
    createRunner(configurationValues).run()
  }
}
