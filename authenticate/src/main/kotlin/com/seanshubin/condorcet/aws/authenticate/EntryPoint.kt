package com.seanshubin.condorcet.aws.authenticate

object EntryPoint {
  @JvmStatic
  fun main(args: Array<String>) {
    DependencyInjection(args).runner.run()
  }
}