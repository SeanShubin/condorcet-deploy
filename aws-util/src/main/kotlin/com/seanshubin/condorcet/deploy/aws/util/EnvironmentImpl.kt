package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.seanshubin.condorcet.deploy.domain.Http
import com.seanshubin.condorcet.deploy.domain.Shell
import java.nio.charset.Charset

class EnvironmentImpl(
    override val commandLineArguments: List<String>,
    override val shell: Shell,
    override val objectMapper: ObjectMapper,
    override val charset: Charset,
    override val http: Http,
    override val configuration: Configuration,
    override val emitLine: (String) -> Unit
) : Environment
