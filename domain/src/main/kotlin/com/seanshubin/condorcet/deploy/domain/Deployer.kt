package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.*
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.rds.DatabaseInstance

class Deployer(private val config: ConfigurationValues) : Runnable {
    override fun run() {
        val app = App()
        val environment = Environment.builder()
                .account(config.account)
                .region(config.region)
                .build()
        val stackProps = StackProps.builder()
                .env(environment).build()
        val stack = Stack(app, config.stackName, stackProps)
        val vpc = Vpc.Builder.create(stack, config.vpcName).build()
        val instanceType = InstanceType.of(
                config.instanceClass,
                config.instanceSize)
        val password = SecretValue.plainText(config.databasePassword)
        DatabaseInstance.Builder.create(stack, config.databaseInstanceId)
                .masterUsername(config.databaseMasterUsername)
                .masterUserPassword(password)
                .databaseName(config.databaseName)
                .engineVersion(config.databaseEngineVersion)
                .engine(config.databaseEngine)
                .vpc(vpc)
                .port(config.databasePort)
                .instanceClass(instanceType)
                .removalPolicy(config.databaseRemovalPolicy)
                .deletionProtection(false).build()
        app.synth()
    }
}
