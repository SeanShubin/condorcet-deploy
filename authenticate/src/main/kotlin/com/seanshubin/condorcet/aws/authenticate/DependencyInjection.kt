package com.seanshubin.condorcet.aws.authenticate

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.seanshubin.condorcet.aws.domain.*
import com.seanshubin.condorcet.deploy.contract.FilesContract
import com.seanshubin.condorcet.deploy.contract.FilesDelegate
import software.amazon.awssdk.services.sts.StsClient
import java.net.http.HttpClient
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

class DependencyInjection(commandLineArguments: Array<String>){
  private val objectMapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  private val cacheDir: Path = Paths.get("out/cache")
  private val exportStatementsDir = Paths.get("out/export")
  private val profileDir: Path = Paths.get("local-config/profile")
  private val files: FilesContract = FilesDelegate
  private val stsClient: StsClient = StsClient.builder().build()
  private val charset: Charset = StandardCharsets.UTF_8
  private val httpClient: HttpClient = HttpClient.newHttpClient()
  private val http: Http = JavaHttp(httpClient)
  private val credentialsFactory: CredentialsFactory = AwsCredentialsFactory(
      objectMapper,
      cacheDir,
      exportStatementsDir,
      profileDir,
      files,
      stsClient,
      charset,
      http)
  val runner:Runnable = CredentialsLoader(commandLineArguments, credentialsFactory)
}
