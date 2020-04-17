package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seanshubin.condorcet.deploy.contract.FilesContract
import com.seanshubin.condorcet.deploy.contract.FilesDelegate
import com.seanshubin.condorcet.deploy.domain.Http
import com.seanshubin.condorcet.deploy.domain.HttpImpl
import com.seanshubin.condorcet.deploy.domain.Shell
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
  val emitLine: (String) -> Unit = ::println
  val environmentFactory: EnvironmentFactory =
      EnvironmentFactoryImpl(
          shell,
          objectMapper,
          charset,
          http,
          configurationFactory,
          emitLine)
  val dispatcher: Dispatcher =
      Dispatcher(commandFactory, environmentFactory)
}