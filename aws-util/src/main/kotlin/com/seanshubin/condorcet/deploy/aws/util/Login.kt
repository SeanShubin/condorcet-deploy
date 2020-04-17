package com.seanshubin.condorcet.deploy.aws.util

import com.fasterxml.jackson.module.kotlin.readValue
import com.seanshubin.condorcet.deploy.domain.TimeUnitUtil.hoursToSeconds
import java.net.URLEncoder

class Login : Command {
  override fun exec(environment: Environment) {
    val commandLineArguments = environment.commandLineArguments
    val shell = environment.shell
    val objectMapper = environment.objectMapper
    val charset = environment.charset
    val http = environment.http
    val configuration = environment.configuration
    val emitLine = environment.emitLine
    val mfaId = configuration.mfaId
    val mfaName = configuration.mfaName
    val account = configuration.account
    val organization = configuration.organization
    val role = configuration.role
    val mfaCode = commandLineArguments.getOrNull(1) ?: throw RuntimeException("mfa code expected")
    val command = listOf(
        "aws",
        "sts",
        "assume-role",
        "--role-arn", "arn:aws:iam::$account:role/$organization/$role",
        "--role-session-name", "bootstrap",
        "--serial-number", "arn:aws:iam::$mfaId:mfa/$mfaName",
        "--duration-seconds", hoursToSeconds(12).toString(),
        "--output", "json",
        "--token-code", mfaCode)
    val jsonStsResult = shell.execString(command)
    val stsResult = objectMapper.readValue<Map<String, Any>>(jsonStsResult)
    val credentials = stsResult.getValue("Credentials") as Map<String, Any>
    val sessionId = credentials.getValue("AccessKeyId") as String
    val sessionKey = credentials.getValue("SecretAccessKey") as String
    val sessionToken = credentials.getValue("SessionToken") as String
    val signInParams = objectMapper.writeValueAsString(mapOf(
        "sessionId" to sessionId,
        "sessionKey" to sessionKey,
        "sessionToken" to sessionToken))
    val signInParamsEncoded = URLEncoder.encode(signInParams, charset)
    val signInUrl = "https://signin.aws.amazon.com/federation?Action=getSigninToken&Session=$signInParamsEncoded"
    val signInTokenJson = http.getString(signInUrl)
    val signInToken = objectMapper.readValue<Map<String, Any>>(signInTokenJson).getValue("SigninToken")
    val logInUrl = "https://signin.aws.amazon.com/federation?Action=login&Destination=https%3A%2F%2Fconsole.aws.amazon.com/console/home?region=us-east-1&SigninToken=$signInToken"
    emitLine("export AWS_ACCESS_KEY_ID=$sessionId")
    emitLine("export AWS_SECRET_ACCESS_KEY=$sessionKey")
    emitLine("export AWS_SESSION_TOKEN=$sessionToken")
    emitLine("export AWS_SESSION_URL=$logInUrl")
  }
}