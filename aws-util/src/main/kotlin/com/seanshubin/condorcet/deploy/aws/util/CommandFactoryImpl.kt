package com.seanshubin.condorcet.deploy.aws.util

class CommandFactoryImpl : CommandFactory {
  override fun fromName(name: String): Command =
      when (name) {
        "login" -> Login()
        "logout" -> Logout()
        else -> throw UnsupportedOperationException("command named '$name' is not supported")
      }
}
