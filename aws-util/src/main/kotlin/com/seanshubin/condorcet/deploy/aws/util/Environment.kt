package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.seanshubin.condorcet.deploy.domain.Http
import com.seanshubin.condorcet.deploy.domain.Shell
import java.nio.charset.Charset

interface Environment {
  val commandLineArguments: List<String>
  val shell: Shell
  val objectMapper: ObjectMapper
  val charset: Charset
  val http: Http
  val configuration: Configuration
}
