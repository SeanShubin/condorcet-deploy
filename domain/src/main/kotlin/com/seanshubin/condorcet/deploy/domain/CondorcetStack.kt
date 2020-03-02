package com.seanshubin.condorcet.deploy.domain

import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.rds.DatabaseInstance
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import software.amazon.awscdk.services.rds.DatabaseInstanceProps

class CondorcetStack(scope: Construct, id: String) : Stack(scope, id) {
    init {
        val vpc = Vpc(scope, id)
        val instanceClass = InstanceType.of(
                InstanceClass.MEMORY3,
                InstanceSize.SMALL)
        val databaseInstanceProps = DatabaseInstanceProps.builder().masterUsername(GlobalConstants.databaseMasterUsername).engine(DatabaseInstanceEngine.MYSQL).instanceClass(instanceClass).vpc(vpc).build()
        DatabaseInstance(scope, id, databaseInstanceProps)
    }
}
