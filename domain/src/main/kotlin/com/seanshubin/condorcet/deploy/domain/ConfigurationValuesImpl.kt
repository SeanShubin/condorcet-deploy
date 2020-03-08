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
    override val databasePassword: String
    override val databaseMasterUsername = "CondorcetMaster";
    override val databaseInstanceId: String = "CondorcetDatabaseId"
    override val databaseName = "CondorcetDatabase";
    override val stackName = "CondorcetStack"
    override val vpcName = "CondorcetVpc"
    override val account = "964638509728"
    override val region = "us-west-1"
    override val instanceClass = InstanceClass.MEMORY4
    override val instanceSize = InstanceSize.LARGE
    override val databaseEngine: DatabaseInstanceEngine = DatabaseInstanceEngine.MYSQL
    override val databaseEngineVersion: String = "8.0.17"
    override val databasePort: Int = 3306
    override val databaseRemovalPolicy: RemovalPolicy = RemovalPolicy.DESTROY

    init {
        val properties = Properties()
        files.newBufferedReader(propertiesPath, charset).use { bufferedReader ->
            properties.load(bufferedReader)
        }
        databasePassword = properties.getProperty("condorcet-db-password")
    }
}
