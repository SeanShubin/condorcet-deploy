package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.Environment
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.rds.DatabaseInstance
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import software.amazon.awscdk.services.rds.DatabaseInstanceProps

class Deployer : Runnable {
    override fun run() {
        val app = App()
        val environment = Environment.builder()
                .account(GlobalConstants.account)
                .region(GlobalConstants.region)
                .build()
        val stackProps = StackProps.builder().env(environment).build()
        val stack = Stack(app, GlobalConstants.condorcetStackName, stackProps)
        val vpc = Vpc.Builder.create(stack, GlobalConstants.vpcName).build()
        val instanceType = InstanceType.of(
                GlobalConstants.instanceClass,
                GlobalConstants.instanceSize)
        val databaseInstanceProps = DatabaseInstanceProps.builder().masterUsername(GlobalConstants.databaseMasterUsername).engine(DatabaseInstanceEngine.MYSQL).instanceClass(instanceType).vpc(vpc).build()
        DatabaseInstance(stack, GlobalConstants.databaseName, databaseInstanceProps)
        app.synth()
    }
}
