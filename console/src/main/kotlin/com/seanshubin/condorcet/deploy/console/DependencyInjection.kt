package com.seanshubin.condorcet.deploy.console

import com.seanshubin.condorcet.deploy.domain.Deployer

class DependencyInjection(val commandLineArguments: Array<String>) {
    val runner: Runnable = Deployer()
}
