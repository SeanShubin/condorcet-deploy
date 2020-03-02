package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.App

class Deployer : Runnable {
    override fun run() {
        val app = App()
        CondorcetStack(app, GlobalConstants.condorcetStackName)
        app.synth()
    }
}
