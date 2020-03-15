package com.seanshubin.condorcet.deploy.domain

import com.seanshubin.condorcet.deploy.contract.FilesContract
import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*

class ConfigurationValuesImpl(files: FilesContract,
                              propertiesPath: Path,
                              charset: Charset) : ConfigurationValues {
    override val databasePassword: String
    override val databaseMasterUsername = "CondorcetMaster";
    override val databaseInstanceId = "CondorcetDatabaseId"
    override val databaseName = "CondorcetDatabase";
    override val stackName = "CondorcetStack"
    override val vpcName = "CondorcetVpc"
    override val securityGroupId = "CondorcetSecurityGroupId"
    override val ec2InstanceName = "CondorcetEc2"
    override val ec2InstanceId = "CondorcetEc2Id"
    override val account = "964638509728"
    override val region = "us-west-1"
    override val instanceClass = InstanceClass.MEMORY4
    override val instanceSize = InstanceSize.LARGE
    override val databaseEngine: DatabaseInstanceEngine = DatabaseInstanceEngine.MYSQL
    override val databaseEngineVersion: String = "8.0.17"
    override val databasePort: Int = 3306
    override val databaseRemovalPolicy: RemovalPolicy = RemovalPolicy.DESTROY
    override val ec2InstanceType = "t2.micro"

    init {
        val properties = Properties()
        files.newBufferedReader(propertiesPath, charset).use { bufferedReader ->
            properties.load(bufferedReader)
        }
        databasePassword = properties.getProperty("condorcet-db-password")
    }
}
