package com.seanshubin.condorcet.deploy.aws.util

import com.seanshubin.condorcet.deploy.domain.IoUtil
import com.seanshubin.condorcet.deploy.domain.Shell
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class ShellImpl : Shell {
  override fun execString(command: List<String>): String {
    val processBuilder = ProcessBuilder()
    processBuilder.command(command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    val inputStream = process.inputStream
    val charset: Charset = StandardCharsets.UTF_8
    val text = IoUtil.inputStreamToString(inputStream, charset)
    val exitCode = process.waitFor()
    if (exitCode != 0) {
      val commandString = command.joinToString(" ")
      val message = "failed with exit code $exitCode\n$commandString\n$text"
      throw RuntimeException(message)
    }
    return text
  }
}
