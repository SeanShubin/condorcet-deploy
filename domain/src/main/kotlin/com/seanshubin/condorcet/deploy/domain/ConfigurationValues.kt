package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine

interface ConfigurationValues {
    val databaseMasterUsername: String
    val databaseName: String
    val databaseEngineVersion: String
    val databaseEngine: DatabaseInstanceEngine
    val databasePassword: String
    val databasePort: Int
    val databaseRemovalPolicy: RemovalPolicy
    val databaseInstanceId: String
    val stackName: String
    val vpcName: String
    val account: String
    val region: String
    val instanceClass: InstanceClass
    val instanceSize: InstanceSize
}
