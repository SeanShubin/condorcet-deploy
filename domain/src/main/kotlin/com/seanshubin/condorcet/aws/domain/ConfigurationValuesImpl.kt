package com.seanshubin.condorcet.aws.domain

import software.amazon.awscdk.core.RemovalPolicy
import software.amazon.awscdk.services.ec2.InstanceClass
import software.amazon.awscdk.services.ec2.InstanceSize
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine

class ConfigurationValuesImpl(
    override val databasePassword: String,
    override val account: String) : ConfigurationValues {
  override val databaseMasterUsername = "CondorcetMaster";
  override val databaseInstanceId = "CondorcetDatabaseId"
  override val databaseName = "CondorcetDatabase";
  override val stackName = "CondorcetStack"
  override val vpcName = "CondorcetVpc"
  override val securityGroupId = "CondorcetSecurityGroupId"
  override val ec2InstanceName = "CondorcetEc2"
  override val ec2InstanceId = "CondorcetEc2Id"
  override val s3BucketName: String = "CondorcetBucket"
  override val region = "us-west-1"
  override val instanceClass = InstanceClass.MEMORY4
  override val instanceSize = InstanceSize.LARGE
  override val databaseEngine: DatabaseInstanceEngine = DatabaseInstanceEngine.MYSQL
  override val databaseEngineVersion: String = "8.0.17"
  override val databasePort: Int = 3306
  override val databaseRemovalPolicy: RemovalPolicy = RemovalPolicy.DESTROY
  override val ec2InstanceType = "t2.micro"
}
