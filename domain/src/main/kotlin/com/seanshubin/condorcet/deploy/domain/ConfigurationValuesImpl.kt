package com.seanshubin.condorcet.deploy.domain

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
    private val prefix = "condorcet"
    override val databasePassword: String
    override val databaseMasterUsername = "$prefix-master-username";
    override val databaseName = "$prefix-database";
    override val stackName = "$prefix-stack"
    override val vpcName = "$prefix-vpc"
    override val account = "964638509728"
    override val region = "us-west-1"
    override val instanceClass = InstanceClass.STANDARD3
    override val instanceSize = InstanceSize.NANO
    override val databaseEngine: DatabaseInstanceEngine = DatabaseInstanceEngine.MYSQL
    override val databaseEngineVersion: String = "8.0.16"
    override val databasePort: Int = 3306
    override val databaseRemovalPolicy: RemovalPolicy = RemovalPolicy.DESTROY
    override val databaseInstanceId: String = "$prefix-database-instance"

    init {
        val properties = Properties()
        files.newBufferedReader(propertiesPath, charset).use { bufferedReader ->
            properties.load(bufferedReader)
        }
        databasePassword = properties.getProperty("condorcet-db-password")
    }
}
