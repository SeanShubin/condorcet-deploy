package com.seanshubin.condorcet.deploy.aws.domain

import software.amazon.awscdk.*
import software.amazon.awscdk.services.apigatewayv2.alpha.DomainName
import software.amazon.awscdk.services.apigatewayv2.alpha.AddRoutesOptions
import software.amazon.awscdk.services.apigatewayv2.alpha.DomainMappingOptions
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpUrlIntegration
import software.amazon.awscdk.services.certificatemanager.DnsValidatedCertificate
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.cloudfront.*
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin
import software.amazon.awscdk.services.cloudfront.origins.S3Origin
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.rds.Credentials
import software.amazon.awscdk.services.rds.DatabaseInstance
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import software.amazon.awscdk.services.route53.ARecord
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneProviderProps
import software.amazon.awscdk.services.route53.RecordTarget
import software.amazon.awscdk.services.route53.targets.ApiGatewayv2DomainProperties
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.deployment.BucketDeployment
import software.amazon.awscdk.services.s3.deployment.Source
import software.amazon.awscdk.services.secretsmanager.Secret
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator
import software.constructs.Construct


class Runner : Runnable {
    object EnvironmentConstants {
        const val account = "964638509728"
        const val region = "us-east-1"
    }
    object Names {
        private const val prefix = "Condorcet"
        const val vpcStackId = "${prefix}VpcStack"
        const val databaseStackId = "${prefix}DatabaseStack"
        const val appStackId = "${prefix}AppStack"
        const val lastStackId = "${prefix}LastStack"
        const val vpcId = "${prefix}Vpc"
        const val securityGroupId = "${prefix}SecurityGroup"
        const val ec2InstanceId = "${prefix}Ec2Id"
        const val ec2InstanceName = "${prefix}Ec2Name"
        const val databaseInstanceId = "${prefix}DatabaseId"
        const val databaseName = "${prefix}DatabaseName"
        const val s3BucketNameForEc2Files = "${prefix}Ec2Bucket"
        const val s3BucketNameForWebsite = "${prefix}WebsiteBucket"
        const val s3BucketDeploymentNameForEc2Files = "${prefix}Ec2BucketDeploy"
        const val s3BucketDeploymentNameForWebsite = "${prefix}WebsiteBucketDeploy"
        const val publicSubnetName = "${prefix}PublicSubnet"
        const val privateSubnetName = "${prefix}PrivateSubnet"
        const val roleName = "${prefix}Role"
        const val keyName = "${prefix}Key"
        const val apiName = "${prefix}Api"
        const val urlIntegration = "${prefix}UrlIntegration"
        const val allCookiesPolicy = "${prefix}AllCookiesPolicy"
        const val allCookiesPolicyId = "a915f680-5340-4b60-b36f-5ec318bd9b56"
        const val apiDistributionName = "${prefix}ApiDistribution"
        const val appDistributionName = "${prefix}AppDistribution"
        const val databasePassword = "${prefix}DatabasePassword"
        const val apiDomainId = "${prefix}ApiDomainId"
        const val apiCertificateArn = "arn:aws:acm:us-east-1:964638509728:certificate/9e407996-9bc4-4bf5-a7c1-9aea15b848cf"
        const val apiHostedZoneCdkId = "${prefix}ApiHostedZone"
        const val apiDomainName = "pairwisevote.org"
        const val apiCertificateId = "${prefix}ApiCertificate"
        const val apiAlias = "${prefix}ApiAlias"
        const val apiHostedZoneId = "Z03310473GG6WYXYEU0FD"
        const val appDomainId = "${prefix}AppDomainId"
        const val appCertificateArn = "arn:aws:acm:us-east-1:964638509728:certificate/f703477d-855c-48df-bc4f-74207cb80bc7"
        const val appHostedZoneCdkId = "${prefix}AppHostedZone"
        const val appDomainName = "pairwisevote.com"
        const val appCertificateId = "${prefix}AppCertificate"
        const val appHostedZoneId = "Z09386273B4IXDJHAW23A"
        const val appAlias = "${prefix}AppAlias"
    }

    override fun run() {
        val app = App()
        val stackProps = createStackProps()
        val vpcStack = VpcStack(app, stackProps)
        val databaseStack = DatabaseStack(
            app,
            vpcStack.vpc,
            vpcStack.securityGroup,
            vpcStack.databasePassword,
            stackProps
        )
        val applicationStack = ApplicationStack(
            app,
            vpcStack.vpc,
            vpcStack.securityGroup,
            databaseStack.database,
            vpcStack.databasePassword,
            stackProps
        )
        val lastStack = LastStack(
            app,
            applicationStack.ec2,
            applicationStack.bucketWithFilesForWebsite,
            stackProps
        )
        app.synth()
    }

    fun createStackProps():StackProps {
        val environment = createEnvironment()
        return StackProps.builder().env(environment).build()
    }

    fun createEnvironment():Environment{
        return Environment
            .builder()
            .account(EnvironmentConstants.account)
            .region(EnvironmentConstants.region)
            .build()
    }

    class VpcStack(
        scope: Construct,
        stackProps:StackProps
    ) : Stack(scope, Names.vpcStackId, stackProps) {
        val vpc: Vpc = createVpc()
        val securityGroup: SecurityGroup = createSecurityGroup(vpc)
        val databasePassword: Secret = createDatabasePassword()
        fun createVpc(): Vpc {
            val publicSubnet = SubnetConfiguration.builder()
                .name(Names.publicSubnetName)
                .cidrMask(24)
                .subnetType(SubnetType.PUBLIC)
                .build()
            val privateSubnet = SubnetConfiguration.builder()
                .name(Names.privateSubnetName)
                .cidrMask(28)
                .subnetType(SubnetType.PRIVATE_ISOLATED)
                .build()
            val subnetList = listOf(publicSubnet, privateSubnet)
            val vpc = Vpc.Builder.create(this, Names.vpcId)
                .cidr("10.0.0.0/16")
                .natGateways(0)
                .subnetConfiguration(subnetList)
                .build()
            return vpc
        }

        private fun createSecurityGroup(vpc: Vpc): SecurityGroup {
            val securityGroup = SecurityGroup.Builder.create(this, Names.securityGroupId)
                .vpc(vpc)
                .build()
            securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(8080),
                "Allow HTTP debug from anywhere"
            )
            securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(22),
                "Allow SSH from anywhere"
            )
            securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(3306),
                "Allow MYSQL from anywhere"
            )
            return securityGroup
        }

        fun createDatabasePassword(): Secret {
            val secretStringGenerator = SecretStringGenerator.builder()
                .excludePunctuation(true)
                .build()
            val databasePassword = Secret.Builder.create(this, Names.databasePassword)
                .generateSecretString(secretStringGenerator)
                .build()
            return databasePassword
        }
    }

    class DatabaseStack(
        scope: Construct,
        vpc: Vpc,
        securityGroup: SecurityGroup,
        databasePassword: Secret,
        stackProps:StackProps
    ) : Stack(scope, Names.databaseStackId, stackProps) {
        val database: DatabaseInstance = createDatabase(vpc, securityGroup, databasePassword)
        private fun createDatabase(
            vpc: Vpc,
            securityGroup: SecurityGroup,
            databasePassword: Secret
        ): DatabaseInstance {
            val securityGroups = listOf(securityGroup)
            val databaseInstanceType = InstanceType.of(
                InstanceClass.BURSTABLE2,
                InstanceSize.MICRO
            )
            val credentials = Credentials.fromPassword("root", databasePassword.secretValue)
            val privateSubnets = SubnetSelection.builder()
                .subnetType(SubnetType.PRIVATE_ISOLATED)
                .build()
            val database = DatabaseInstance.Builder.create(this, Names.databaseInstanceId)
                .databaseName(Names.databaseName)
                .engine(DatabaseInstanceEngine.MYSQL)
                .credentials(Credentials.fromGeneratedSecret("root"))
                .vpc(vpc)
                .instanceType(databaseInstanceType)
                .vpcSubnets(privateSubnets)
                .port(3306)
                .credentials(credentials)
                .removalPolicy(RemovalPolicy.DESTROY)
                .deletionProtection(false)
                .securityGroups(securityGroups)
                .backupRetention(Duration.seconds(0))
                .build()
            return database
        }
    }

    class ApplicationStack(
        scope: Construct,
        vpc: Vpc,
        securityGroup: SecurityGroup,
        database: DatabaseInstance,
        databasePassword: Secret,
        stackProps:StackProps
    ) : Stack(scope, Names.appStackId, stackProps) {
        val bucketWithFilesForEc2 = createFilesForEc2Bucket()
        val ec2 = createEc2Instance(
            vpc,
            securityGroup,
            bucketWithFilesForEc2,
            database,
            databasePassword
        )
        val bucketWithFilesForWebsite = createWebsiteBucket(ec2)

        private fun createFilesForEc2Bucket(): Bucket {
            val bucket = Bucket.Builder.create(this, Names.s3BucketNameForEc2Files)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build()
            val s3Files = Source.asset("generated/s3/ec2")
            val deploySources = listOf(s3Files)
            val bucketDeployment = BucketDeployment.Builder.create(this, Names.s3BucketDeploymentNameForEc2Files)
                .sources(deploySources)
                .destinationBucket(bucket)
                .build()
            return bucket
        }

        private fun createEc2Instance(
            vpc: Vpc,
            securityGroup: SecurityGroup,
            bucket: Bucket,
            database: DatabaseInstance,
            databasePassword: Secret
        ): Instance {
            val servicePrincipal = ServicePrincipal("ec2.amazonaws.com")
            val s3ReadOnlyAccess = ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")
            val accessSecretsManager = ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite")
            val managedPolicyList = listOf(s3ReadOnlyAccess, accessSecretsManager)
            val role = Role.Builder.create(this, Names.roleName)
                .assumedBy(servicePrincipal)
                .managedPolicies(managedPolicyList)
                .build()
            val computeInstanceType = InstanceType.of(
                InstanceClass.BURSTABLE2,
                InstanceSize.NANO
            )
            val machineImage = AmazonLinuxImage.Builder.create()
                .generation(AmazonLinuxGeneration.AMAZON_LINUX_2)
                .build()
            val executable = InitFileOptions.builder().group("ec2-user").owner("ec2-user").mode("000755").build()
            val userDir = InitCommandOptions.builder().cwd("/home/ec2-user/").build()
            val installJava = InitPackage.yum("java-17-amazon-corretto")
            val installMysql = InitCommand.argvCommand(listOf("yum", "install", "-y", "mysql"))
            val copyJavaArchiveForServer = InitSource.fromS3Object("/home/ec2-user", bucket, "backend.zip")
            val copySystemdEntry = InitSource.fromS3Object("/etc/systemd/system", bucket, "systemd.zip")
            val launchServer = InitCommand.argvCommand(listOf("systemctl", "start", "condorcet-backend"))
            val lines = listOf(
                "java -jar edit-json.jar configuration.json set string ${database.dbInstanceEndpointAddress} database root host",
                "java -jar edit-json.jar configuration.json set string ${database.dbInstanceEndpointAddress} database immutable host",
                "java -jar edit-json.jar configuration.json set string ${database.dbInstanceEndpointAddress} database mutable host",
                "DATABASE_PASSWORD=\$(aws secretsmanager get-secret-value --region ${EnvironmentConstants.region} --output text --query SecretString --secret-id ${databasePassword.secretName})",
                "java -jar edit-json.jar secrets/secret-configuration.json set string \$DATABASE_PASSWORD database root password",
                "java -jar edit-json.jar secrets/secret-configuration.json set string \$DATABASE_PASSWORD database immutable password",
                "java -jar edit-json.jar secrets/secret-configuration.json set string \$DATABASE_PASSWORD database mutable password"
            )
            val content = lines.joinToString("\n", "", "\n")
            val initializeContent = InitFile.fromString("/home/ec2-user/initialize.sh", content, executable)
            val chown =
                InitCommand.argvCommand(listOf("sudo", "chown", "-R", "ec2-user:ec2-user", "/home/ec2-user"))
            val initializeExec = InitCommand.shellCommand("./initialize.sh", userDir)
            val configElements = listOf(
                installJava,
                installMysql,
                copyJavaArchiveForServer,
                copySystemdEntry,
                initializeContent,
                initializeExec,
                chown,
                launchServer
            )
            val initConfig = InitConfig(configElements)
            val cloudFormationInit = CloudFormationInit.fromConfig(initConfig)
            val publicSubnets = SubnetSelection.builder()
                .subnetType(SubnetType.PUBLIC)
                .build()
            val ec2 = Instance.Builder.create(this, Names.ec2InstanceId)
                .securityGroup(securityGroup)
                .vpc(vpc)
                .vpcSubnets(publicSubnets)
                .role(role)
                .instanceName(Names.ec2InstanceName)
                .instanceType(computeInstanceType)
                .machineImage(machineImage)
                .keyName(Names.keyName)
                .init(cloudFormationInit)
                .build()
            return ec2
        }

        private fun createWebsiteBucket(ec2: Instance): Bucket {
            val bucket = Bucket.Builder.create(this, Names.s3BucketNameForWebsite)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build()
            val s3Files = Source.asset("generated/s3/website")
            val deploySources = listOf(s3Files)
            val bucketDeployment = BucketDeployment.Builder.create(this, Names.s3BucketDeploymentNameForWebsite)
                .sources(deploySources)
                .destinationBucket(bucket)
                .build()
            return bucket
        }

    }

    class LastStack(
        scope: Construct,
        ec2: Instance,
        staticSiteBucket: Bucket,
        stackProps:StackProps
    ) : Stack(scope, Names.lastStackId, stackProps) {
        val api = createApi(ec2)
        val distribution = createCloudfrontDistribution(staticSiteBucket, api)

        private fun createApi(ec2: Instance): HttpApi {
            val hostedZoneProviderProps = HostedZoneProviderProps
                .builder()
                .domainName(Names.apiDomainName)
                .build()
            val hostedZone = HostedZone.fromLookup(this, Names.apiHostedZoneCdkId, hostedZoneProviderProps)
            val certificate = Certificate.fromCertificateArn(this, Names.apiCertificateId, Names.apiCertificateArn)
            val domainName = DomainName
                .Builder
                .create(this, Names.apiDomainId)
                .domainName(Names.apiDomainName)
                .certificate(certificate)
                .build()
            val domainProperties = ApiGatewayv2DomainProperties(domainName.regionalDomainName, domainName.regionalHostedZoneId)
            val recordTarget = RecordTarget.fromAlias(domainProperties)
            ARecord.Builder.create(this, Names.apiAlias).zone(hostedZone).target(recordTarget).build()
            val domainMappingOptions = DomainMappingOptions
                .builder()
                .domainName(domainName)
                .build()
            val httpApi = HttpApi
                .Builder
                .create(this, Names.apiName)
                .defaultDomainMapping(domainMappingOptions)
                .build()
            val instancePublicIp = ec2.instancePublicIp
            val url = "http://$instancePublicIp:8080/{proxy}"
            val integration = HttpUrlIntegration.Builder.create(Names.urlIntegration, url)
                .method(HttpMethod.ANY)
                .build()
            val addRoutesOptions = AddRoutesOptions.builder()
                .methods(listOf(HttpMethod.ANY))
                .path("/proxy/{proxy+}")
                .integration(integration)
                .build()
            httpApi.addRoutes(addRoutesOptions)
            return httpApi
        }

        private fun createCloudfrontDistribution(
            staticSiteBucket: Bucket,
            api: HttpApi
        ): Distribution {
            val staticSiteOrigin = S3Origin.Builder.create(staticSiteBucket).build()
            val staticSiteBehavior = BehaviorOptions.builder()
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .origin(staticSiteOrigin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .build()
            val errorResponse = ErrorResponse
                .builder()
                .httpStatus(403)
                .responseHttpStatus(200)
                .responsePagePath("/index.html")
                .build()
            val errorResponses = listOf(errorResponse)
            val httpOrigin = HttpOrigin.Builder.create(Names.apiDomainName).build()
            val allCookies = OriginRequestPolicy.Builder.create(this, Names.allCookiesPolicy).cookieBehavior(OriginRequestCookieBehavior.all()).build()
            val proxyBehavior = BehaviorOptions
                .builder()
                .origin(httpOrigin)
                .originRequestPolicy(allCookies)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .build()
            val additionalBehaviors = mapOf("/proxy/*" to proxyBehavior)
            val hostedZoneProviderProps = HostedZoneProviderProps
                .builder()
                .domainName(Names.appDomainName)
                .build()
            val hostedZone = HostedZone.fromLookup(this, Names.appHostedZoneCdkId, hostedZoneProviderProps)
            val certificate = Certificate.fromCertificateArn(this, Names.appCertificateId, Names.appCertificateArn)
            val domainName = DomainName
                .Builder
                .create(this, Names.appDomainId)
                .domainName(Names.appDomainName)
                .certificate(certificate)
                .build()
            val domainNames = listOf(domainName.name)
            val distribution = Distribution.Builder.create(this, Names.appDistributionName)
                .defaultBehavior(staticSiteBehavior)
                .certificate(certificate)
                .additionalBehaviors(additionalBehaviors)
                .errorResponses(errorResponses)
                .defaultRootObject("index.html")
                .domainNames(domainNames)
                .build()
            val cloudFrontTarget = CloudFrontTarget(distribution)
            val recordTarget = RecordTarget.fromAlias(cloudFrontTarget)
            ARecord.Builder.create(this, Names.appAlias).zone(hostedZone).target(recordTarget).build()
            return distribution
        }
    }
}