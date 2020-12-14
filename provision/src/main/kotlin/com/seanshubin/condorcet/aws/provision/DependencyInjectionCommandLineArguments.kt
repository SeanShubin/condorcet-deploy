package com.seanshubin.condorcet.aws.provision

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.seanshubin.condorcet.aws.domain.ConfigurationValues
import com.seanshubin.condorcet.aws.domain.ConfigureProvision
import com.seanshubin.condorcet.deploy.contract.FilesContract
import com.seanshubin.condorcet.deploy.contract.FilesDelegate
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class DependencyInjectionCommandLineArguments(val commandLineArguments: Array<String>) {
  private val objectMapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  private val files: FilesContract = FilesDelegate
  private val propertiesPathName: String = "local-config/secrets.properties"
  private val profilePathName: String = "local-config/profile/current.json"
  private val charset: Charset = StandardCharsets.UTF_8
  private val createRunner: (ConfigurationValues) -> Runnable = { configurationValues ->
    DependencyInjectionConfigurationValues(configurationValues).runner
  }
  val runner: Runnable = ConfigureProvision(files, charset, objectMapper, profilePathName, propertiesPathName, createRunner)
}
