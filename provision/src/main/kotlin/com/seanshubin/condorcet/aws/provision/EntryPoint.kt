package com.seanshubin.condorcet.aws.provision

object EntryPoint {
  @JvmStatic
  fun main(args: Array<String>) {
    DependencyInjectionCommandLineArguments(args).runner.run()
  }
}
