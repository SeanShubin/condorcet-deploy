package com.seanshubin.condorcet.aws.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seanshubin.condorcet.deploy.contract.FilesContract
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.Path

class AwsCredentialsFactory(
    private val objectMapper: ObjectMapper,
    private val cacheDir: Path,
    private val exportStatementsDir: Path,
    private val profileDir: Path,
    private val files: FilesContract,
    private val stsClient: StsClient,
    private val charset: Charset,
    private val http: Http
) : CredentialsFactory {
  override fun createAndStoreInCache(profileName: String, token: String): Credentials {
    val profile = loadProfile(profileName)
    val credentials = loadFromSts(profile, token)
    storeInCache(profile.sessionName, credentials)
    storeAsExportStatements(profile.sessionName, credentials)
    return credentials
  }

  override fun loadFromCache(profileName: String): Credentials {
    val pathForCache = cacheDir.resolve("$profileName.json")
    val jsonText = files.readString(pathForCache)
    val credentials: Credentials = objectMapper.readValue(jsonText)
    return credentials
  }

  private fun loadProfile(profileName: String): Profile {
    val profileFileName = "$profileName.json"
    val profilePath = profileDir.resolve(profileFileName)
    val jsonText = files.readString(profilePath)
    val profile = objectMapper.readValue<Profile>(jsonText)
    return profile
  }

  private fun Credentials.toAwsCredentialsProvider(): AwsCredentialsProvider =
      AwsCredentialsProvider {
        AwsSessionCredentials.create(
            accessKeyId,
            secretAccessKey,
            sessionToken)
      }

  private fun loadFromSts(profile: Profile, token: String): Credentials {
    val durationSeconds = DurationFormat.seconds.parse(profile.durationSeconds)
    val assumeRoleRequest: AssumeRoleRequest = AssumeRoleRequest
        .builder()
        .roleArn(profile.roleArn)
        .roleSessionName(profile.sessionName)
        .serialNumber(profile.serialNumber)
        .durationSeconds(durationSeconds.toInt())
        .tokenCode(token)
        .build()
    val response = stsClient.assumeRole(assumeRoleRequest)
    val accessKeyId = response.credentials().accessKeyId()
    val secretAccessKey = response.credentials().secretAccessKey()
    val sessionToken = response.credentials().sessionToken()
    val signInToken = fetchSignInToken(accessKeyId, secretAccessKey, sessionToken)
    val signInUrl = "https://signin.aws.amazon.com/federation?Action=login&Destination=https%3A%2F%2Fconsole.aws.amazon.com/console/home?region=us-east-1&SigninToken=$signInToken"
    val signInConsole = profile.asConsoleCommand(token)
    val credentials = Credentials(accessKeyId, secretAccessKey, sessionToken, signInUrl, signInConsole)
    return credentials
  }

  private fun fetchSignInToken(accessKeyId: String, secretAccessKey: String, sessionToken: String): String {
    val signInParams = objectMapper.writeValueAsString(mapOf(
        "sessionId" to accessKeyId,
        "sessionKey" to secretAccessKey,
        "sessionToken" to sessionToken))
    val signInParamsEncoded = URLEncoder.encode(signInParams, charset)
    val signInUrl = "https://signin.aws.amazon.com/federation?Action=getSigninToken&Session=$signInParamsEncoded"
    val signInTokenJson = http.getString(signInUrl)
    val signInToken = objectMapper.readValue<Map<String, Any>>(signInTokenJson).getValue("SigninToken") as String
    return signInToken
  }

  private fun storeInCache(sessionName: String, credentials: Credentials) {
    val jsonCredentials = objectMapper.writeValueAsString(credentials)
    val path = cacheDir.resolve("$sessionName.json")
    ensureCacheDirectoryExists()
    files.writeString(path, jsonCredentials)
  }

  private fun storeAsExportStatements(sessionName: String, credentials: Credentials) {
    val exportStatements = listOf(
        "export AWS_STS_COMMAND=\"${credentials.signInConsole}\"",
        "export AWS_ACCESS_KEY_ID=\"${credentials.accessKeyId}\"",
        "export AWS_SECRET_ACCESS_KEY=\"${credentials.secretAccessKey}\"",
        "export AWS_SESSION_TOKEN=\"${credentials.sessionToken}\"",
        "export AWS_SESSION_URL=\"${credentials.signInUrl}\""
    )
    val path = exportStatementsDir.resolve("$sessionName.txt")
    ensureExportStatementsDirectoryExists()
    files.write(path, exportStatements)
  }

  private fun ensureCacheDirectoryExists() {
    if (!files.exists(cacheDir)) {
      files.createDirectories(cacheDir)
    }
  }

  private fun ensureExportStatementsDirectoryExists() {
    if (!files.exists(exportStatementsDir)) {
      files.createDirectories(exportStatementsDir)
    }
  }
}
