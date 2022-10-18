package com.seanshubin.condorcet.deploy.aws.domain

import com.fasterxml.jackson.module.kotlin.readValue
import com.seanshubin.condorcet.deploy.aws.json.JsonMappers
import software.amazon.awscdk.*
import software.amazon.awscdk.services.apigatewayv2.alpha.DomainName
import software.amazon.awscdk.services.apigatewayv2.alpha.AddRoutesOptions
import software.amazon.awscdk.services.apigatewayv2.alpha.DomainMappingOptions
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpUrlIntegration
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.certificatemanager.CertificateProps
import software.amazon.awscdk.services.certificatemanager.CertificateValidation
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
import java.nio.file.Files
import java.nio.file.Paths

class Runner : Runnable {
    object Names {
        private const val prefix = "Condorcet"
        const val vpcStackId = "${prefix}VpcStack"
        const val databaseStackId = "${prefix}DatabaseStack"
        const val appStackId = "${prefix}AppStack"
        const val websiteStackId = "${prefix}WebsiteStack"
        const val vpcId = "${prefix}Vpc"
        const val securityGroupId = "${prefix}SecurityGroup"
        const val ec2InstanceId = "${prefix}Ec2Id"
        const val ec2InstanceName = "${prefix}Ec2Name"
        const val databaseInstanceId = "${prefix}DatabaseId"
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
        const val appDistributionName = "${prefix}AppDistribution"
        const val databasePassword = "${prefix}DatabasePassword"
        const val apiDomainId = "${prefix}ApiDomainId"
        const val hostedZoneCdkId = "${prefix}HostedZone"
        const val apiCertificateId = "${prefix}ApiCertificate"
        const val apiAlias = "${prefix}ApiAlias"
        const val appDomainId = "${prefix}AppDomainId"
        const val appCertificateId = "${prefix}AppCertificate"
        const val appAlias = "${prefix}AppAlias"
        const val emailPasswordName = "${prefix}EmailPassword"
    }

    private fun loadFromConfig(vararg pathParts:String):Any {
        val configFilePath = Paths.get("local-config", "current.json")
        val jsonText = Files.readString(configFilePath)
        val jsonObject:Map<String, Any> = JsonMappers.parser.readValue(jsonText)
        return loadFromObject(jsonObject, *pathParts)
    }

    private fun loadStringFromConfig(vararg pathParts:String):String =
        loadFromConfig(*pathParts) as String

    private fun loadBooleanFromConfig(vararg pathParts:String):Boolean =
        loadFromConfig(*pathParts) as Boolean

    private fun loadFromObject(jsonObject:Map<String, Any>, vararg pathParts:String):Any {
        val currentPathPart = pathParts[0]
        return if(pathParts.size == 1) {
            jsonObject.getValue(currentPathPart)
        } else {
            val nestedObject =jsonObject.getValue(currentPathPart) as Map<String, Any>
            val remainingPathParts = pathParts.drop(1).toTypedArray()
            loadFromObject(nestedObject,*remainingPathParts)
        }
    }

    override fun run() {
        val app = App()
        val stackProps = createStackProps()
        val baseDomainName = loadStringFromConfig("domain", "base")
        val apiDomainName = loadStringFromConfig("domain", "api")
        val appDomainName = loadStringFromConfig("domain", "app")
        val allowSsh = loadBooleanFromConfig("security", "allowSsh")
        val emailHost = loadStringFromConfig("mail", "host")
        val emailUser = loadStringFromConfig("mail", "user")
        val vpcStack = VpcStack(app, stackProps, allowSsh)
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
            stackProps,
            baseDomainName,
            emailHost,
            emailUser
        )
        val websiteStack = WebsiteStack(
            app,
            applicationStack.ec2,
            applicationStack.bucketWithFilesForWebsite,
            stackProps,
            baseDomainName,
            apiDomainName,
            appDomainName
        )
        app.synth()
    }

    fun createStackProps(): StackProps {
        val environment = createEnvironment()
        return StackProps.builder().env(environment).build()
    }

    fun createEnvironment(): Environment {
        val account = System.getenv("CDK_DEFAULT_ACCOUNT")
        val region = "us-east-1" //System.getenv("CDK_DEFAULT_REGION")
        return Environment
            .builder()
            .account(account)
            .region(region)
            .build()
    }

    class VpcStack(
        scope: Construct,
        stackProps: StackProps,
        allowSsh:Boolean
    ) : Stack(scope, Names.vpcStackId, stackProps) {
        val vpc: Vpc = createVpc()
        val securityGroup: SecurityGroup = createSecurityGroup(vpc, allowSsh)
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

        private fun createSecurityGroup(vpc: Vpc, allowSsh:Boolean): SecurityGroup {
            val securityGroup = SecurityGroup.Builder.create(this, Names.securityGroupId)
                .vpc(vpc)
                .build()
            securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(8080),
                "Allow HTTP debug from anywhere"
            )
            if(allowSsh) {
                securityGroup.addIngressRule(
                    Peer.anyIpv4(),
                    Port.tcp(22),
                    "Allow SSH from anywhere"
                )
            }
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
        stackProps: StackProps
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
        stackProps: StackProps,
        baseDomainName:String,
        emailHost:String,
        emailUser:String
    ) : Stack(scope, Names.appStackId, stackProps) {
        val bucketWithFilesForEc2 = createFilesForEc2Bucket()
        val ec2 = createEc2Instance(
            vpc,
            securityGroup,
            bucketWithFilesForEc2,
            database,
            databasePassword,
            baseDomainName,
            emailHost,
            emailUser
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

        private fun createFile(name: String, lines: List<String>, options: InitFileOptions): InitElement {
            val content = lines.joinToString("\n", "", "\n")
            return InitFile.fromString(name, content, options)
        }

        private fun createEc2Instance(
            vpc: Vpc,
            securityGroup: SecurityGroup,
            bucket: Bucket,
            database: DatabaseInstance,
            databasePassword: Secret,
            baseDomainName:String,
            emailHost:String,
            emailUser:String
        ): Instance {
            val servicePrincipal = ServicePrincipal("ec2.amazonaws.com")
            val s3ReadOnlyAccess = ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")
            val accessSecretsManager = ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite")
            val accessSessionManager = ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")
            val managedPolicyList = listOf(s3ReadOnlyAccess, accessSecretsManager, accessSessionManager)
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
            val setDatabasePasswordCommand =
                "DATABASE_PASSWORD=\$(aws secretsmanager get-secret-value --region $region --output text --query SecretString --secret-id ${databasePassword.secretName})"
            val setEmailPasswordCommand =
                "EMAIL_PASSWORD=\$(aws secretsmanager get-secret-value --region $region --output text --query SecretString --secret-id ${Names.emailPasswordName})"
            val initializeContent = createFile(
                "/home/ec2-user/initialize.sh",
                listOf(
                    "java -jar edit-json.jar local-config/configuration.json set string ${database.dbInstanceEndpointAddress} database root host",
                    "java -jar edit-json.jar local-config/configuration.json set string ${database.dbInstanceEndpointAddress} database immutable host",
                    "java -jar edit-json.jar local-config/configuration.json set string ${database.dbInstanceEndpointAddress} database mutable host",
                    "java -jar edit-json.jar local-config/configuration.json set string $emailHost mail host",
                    "java -jar edit-json.jar local-config/configuration.json set string $emailUser mail user",
                    "java -jar edit-json.jar local-config/configuration.json set string $baseDomainName mail fromDomain",
                    "java -jar edit-json.jar local-config/configuration.json set string  ",
                    setDatabasePasswordCommand,
                    setEmailPasswordCommand,
                    "java -jar edit-json.jar local-config/secrets/secret-configuration.json set string \$DATABASE_PASSWORD database root password",
                    "java -jar edit-json.jar local-config/secrets/secret-configuration.json set string \$DATABASE_PASSWORD database immutable password",
                    "java -jar edit-json.jar local-config/secrets/secret-configuration.json set string \$DATABASE_PASSWORD database mutable password",
                    "java -jar edit-json.jar local-config/secrets/secret-configuration.json set string \$EMAIL_PASSWORD mail password",
                    "java -jar condorcet-backend-console.jar restore"
                ),
                executable
            )
            val mySqlScript = createFile(
                "/home/ec2-user/run-mysql.sh",
                listOf(
                    setDatabasePasswordCommand,
                    "mysql --host=${database.dbInstanceEndpointAddress} --user=root --password=\$DATABASE_PASSWORD"
                ),
                executable
            )
            val chown =
                InitCommand.argvCommand(listOf("sudo", "chown", "-R", "ec2-user:ec2-user", "/home/ec2-user"))
            val initializeExec = InitCommand.shellCommand("./initialize.sh", userDir)
            val configElements = listOf(
                installJava,
                installMysql,
                copyJavaArchiveForServer,
                copySystemdEntry,
                initializeContent,
                mySqlScript,
                chown,
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
            val bucketDeployment = BucketDeployment.Builder
                .create(this, Names.s3BucketDeploymentNameForWebsite)
                .sources(deploySources)
                .destinationBucket(bucket)
                .build()
            return bucket
        }

    }

    class WebsiteStack(
        scope: Construct,
        ec2: Instance,
        staticSiteBucket: Bucket,
        stackProps: StackProps,
        baseDomainName:String,
        apiDomainName:String,
        appDomainName:String
    ) : Stack(scope, Names.websiteStackId, stackProps) {
        val hostedZoneProviderProps = HostedZoneProviderProps
            .builder()
            .domainName(baseDomainName)
            .build()
        val hostedZone = HostedZone.fromLookup(
            this,
            Names.hostedZoneCdkId,
            hostedZoneProviderProps
        )
        val api = createApi(ec2, apiDomainName)
        val distribution = createCloudfrontDistribution(staticSiteBucket, api, apiDomainName, appDomainName)

        private fun createApi(ec2: Instance, apiDomainName:String): HttpApi {
            val certificateValidation = CertificateValidation.fromDns(hostedZone)
            val certificateProps = CertificateProps
                .builder()
                .domainName(apiDomainName)
                .validation(certificateValidation)
                .build()
            val certificate = Certificate(this, Names.apiCertificateId, certificateProps)
            val domainName = DomainName
                .Builder
                .create(this, Names.apiDomainId)
                .domainName(apiDomainName)
                .certificate(certificate)
                .build()
            val domainProperties =
                ApiGatewayv2DomainProperties(domainName.regionalDomainName, domainName.regionalHostedZoneId)
            val recordTarget = RecordTarget.fromAlias(domainProperties)
            ARecord
                .Builder
                .create(this, Names.apiAlias)
                .zone(hostedZone)
                .recordName(apiDomainName)
                .target(recordTarget)
                .build()
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
            val url = "http://$instancePublicIp:8080/{api}"
            val integration = HttpUrlIntegration.Builder.create(Names.urlIntegration, url)
                .method(HttpMethod.ANY)
                .build()
            val addRoutesOptions = AddRoutesOptions.builder()
                .methods(listOf(HttpMethod.ANY))
                .path("/api/{api+}")
                .integration(integration)
                .build()
            httpApi.addRoutes(addRoutesOptions)
            return httpApi
        }

        private fun createCloudfrontDistribution(
            staticSiteBucket: Bucket,
            api: HttpApi,
            apiDomainName:String,
            appDomainName:String
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
            val httpOrigin = HttpOrigin.Builder.create(apiDomainName).build()
            val allCookies = OriginRequestPolicy.Builder.create(this, Names.allCookiesPolicy)
                .cookieBehavior(OriginRequestCookieBehavior.all()).build()
            val apiBehavior = BehaviorOptions
                .builder()
                .origin(httpOrigin)
                .originRequestPolicy(allCookies)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .build()
            val additionalBehaviors = mapOf("/api/*" to apiBehavior)
            val certificateValidation = CertificateValidation.fromDns(hostedZone)
            val certificateProps = CertificateProps
                .builder()
                .domainName(appDomainName)
                .validation(certificateValidation)
                .build()
            val certificate = Certificate(this, Names.appCertificateId, certificateProps)
            val domainName = DomainName
                .Builder
                .create(this, Names.appDomainId)
                .domainName(appDomainName)
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
            ARecord
                .Builder
                .create(this, Names.appAlias)
                .zone(hostedZone)
                .recordName(appDomainName)
                .target(recordTarget)
                .build()
            return distribution
        }
    }
}

