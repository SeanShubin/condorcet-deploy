package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.*
import software.amazon.awscdk.services.ec2.*
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
        val vpc = Vpc.Builder
                .create(stack, config.vpcName)
                .build()
        vpc.publicSubnets.forEach(::println)
        vpc.privateSubnets.forEach(::println)
        val securityGroup = SecurityGroup.Builder
                .create(stack, config.securityGroupId)
                .allowAllOutbound(true)
                .vpc(vpc)
                .build()
        val securityGroups = listOf(securityGroup)
        val instanceType = InstanceType.of(
                config.instanceClass,
                config.instanceSize)
        val password = SecretValue.plainText(config.databasePassword)
        val ec2 = Instance.Builder
                .create(stack, config.ec2InstanceId)
                .securityGroup(securityGroup)
                .vpc(vpc)
                .instanceName(config.ec2InstanceName)
                .instanceType(InstanceType(config.ec2InstanceType))
                .machineImage(MachineImage.latestAmazonLinux())
                .build()
        val subnetSelection = SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build()
        val database = DatabaseInstance.Builder.create(stack, config.databaseInstanceId)
                .masterUsername(config.databaseMasterUsername)
                .masterUserPassword(password)
                .databaseName(config.databaseName)
                .engineVersion(config.databaseEngineVersion)
                .engine(config.databaseEngine)
                .vpc(vpc)
                .vpcPlacement(subnetSelection)
                .port(config.databasePort)
                .instanceClass(instanceType)
                .removalPolicy(config.databaseRemovalPolicy)
                .deletionProtection(false)
                .securityGroups(securityGroups)
                .build()
        app.synth()
    }
}
