package com.seanshubin.condorcet.deploy.console

object EntryPoint {
    @JvmStatic
    fun main(args: Array<String>) {
        DependencyInjection(args).runner.run()
    }
}
