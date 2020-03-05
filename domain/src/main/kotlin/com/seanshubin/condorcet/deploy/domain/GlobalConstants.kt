package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize

object GlobalConstants {
    val prefix = "condorcet"
    val databaseMasterUsername = "$prefix-master-username";
    val databaseName = "$prefix-database";
    val condorcetStackName = "$prefix-stack"
    val vpcName = "$prefix-vpc"
    val account = "964638509728"
    val region = "us-west-1"
    val instanceClass = InstanceClass.STANDARD3
    val instanceSize = InstanceSize.NANO
}
