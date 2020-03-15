package com.seanshubin.condorcet.deploy.aws.util

object EntryPoint {
  @JvmStatic
  fun main(args: Array<String>) {
    DependencyInjection().dispatcher.dispatch(args)
  }

}
