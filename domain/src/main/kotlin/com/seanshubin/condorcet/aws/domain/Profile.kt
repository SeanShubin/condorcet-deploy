package com.seanshubin.condorcet.aws.domain

data class Profile(val sessionName: String,
                   val account: String,
                   val organization: String,
                   val durationSeconds: String,
                   val mfaName: String,
                   val mfaId: String,
                   val role: String? = null) {
  val roleArn: String
    get() {
      val roleSuffix = if (role == null) "" else "/$role"
      return "arn:aws:iam::$account:role/$organization$roleSuffix"
    }
  val serialNumber: String get() = "arn:aws:iam::$mfaId:mfa/$mfaName"
  fun asConsoleCommand(token: String): String {
    val commandParts = listOf(
        "aws",
        "sts",
        "assume-role",
        "--role-arn",
        roleArn,
        "--role-session-name",
        sessionName,
        "--serial-number",
        serialNumber,
        "--token-code",
        token)
    return commandParts.joinToString(" ")
  }
}
