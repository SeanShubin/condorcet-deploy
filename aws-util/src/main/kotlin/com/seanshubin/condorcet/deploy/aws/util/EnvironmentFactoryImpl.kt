package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.seanshubin.condorcet.deploy.domain.Http
import com.seanshubin.condorcet.deploy.domain.Shell
import java.nio.charset.Charset

class EnvironmentFactoryImpl(val shell: Shell,
                             val objectMapper: ObjectMapper,
                             val charset: Charset,
                             val http: Http,
                             val configurationFactory: ConfigurationFactory,
                             val emitLine: (String) -> Unit) : EnvironmentFactory {
  override fun create(commandLineArguments: List<String>): Environment {
    val configuration = configurationFactory.load()
    return EnvironmentImpl(commandLineArguments, shell, objectMapper, charset, http, configuration, emitLine)
  }
}
