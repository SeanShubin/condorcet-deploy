package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.rds.DatabaseInstance
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import software.amazon.awscdk.services.rds.DatabaseInstanceProps

class Deployer : Runnable {
    override fun run() {
        val app = App()
        val stack = Stack(app, GlobalConstants.condorcetStackName)
        val vpc = Vpc.Builder.create(stack, GlobalConstants.vpcName).build()
        val instanceClass = InstanceType.of(
            InstanceClass.STANDARD3,
            InstanceSize.NANO)
        val databaseInstanceProps = DatabaseInstanceProps.builder().masterUsername(GlobalConstants.databaseMasterUsername).engine(DatabaseInstanceEngine.MYSQL).instanceClass(instanceClass).vpc(vpc).build()
        DatabaseInstance(stack, GlobalConstants.databaseName, databaseInstanceProps)
        app.synth()
    }
}
