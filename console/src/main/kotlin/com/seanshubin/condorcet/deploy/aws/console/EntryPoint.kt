package com.seanshubin.condorcet.deploy.aws.console

import com.seanshubin.condorcet.deploy.aws.domain.Runner

object EntryPoint {
    @JvmStatic
    fun main(args: Array<String>) {
        Runner().run()
    }
}
