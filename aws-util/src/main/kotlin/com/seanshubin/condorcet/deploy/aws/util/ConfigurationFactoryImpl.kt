package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seanshubin.condorcet.deploy.domain.FilesContract
import java.nio.charset.Charset
import java.nio.file.Paths

class ConfigurationFactoryImpl(val files: FilesContract,
                               val charset: Charset,
                               val objectMapper: ObjectMapper) : ConfigurationFactory {
  override fun load(): Configuration {
    val pathName = "local-config/current.json"
    val path = Paths.get(pathName)
    val jsonText = files.readString(path, charset)
    val configuration = objectMapper.readValue<Configuration>(jsonText)
    return configuration
  }
}
