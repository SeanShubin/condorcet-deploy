package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seanshubin.condorcet.deploy.domain.*
import java.net.http.HttpClient
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class DependencyInjection {
  val commandFactory: CommandFactory = CommandFactoryImpl()
  val shell: Shell = ShellImpl()
  val kotlinModule: Module = KotlinModule()
  val objectMapper: ObjectMapper = ObjectMapper().registerModule(kotlinModule)
  val charset: Charset = StandardCharsets.UTF_8
  val httpClient: HttpClient = HttpClient.newHttpClient()
  val http: Http = HttpImpl(httpClient)
  val files: FilesContract = FilesDelegate
  val configurationFactory: ConfigurationFactory =
      ConfigurationFactoryImpl(files, charset, objectMapper)
  val environmentFactory: EnvironmentFactory =
      EnvironmentFactoryImpl(
          shell,
          objectMapper,
          charset,
          http,
          configurationFactory)
  val dispatcher: Dispatcher =
      Dispatcher(commandFactory, environmentFactory)
}