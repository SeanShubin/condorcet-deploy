## Why am I stuck in us-east-1

### When removing environment

```kotlin
val hostedZone = HostedZone.fromLookup(this, Names.apiHostedZoneCdkId, hostedZoneProviderProps)
```

```
Exception in thread "main" software.amazon.jsii.JsiiException: Cannot retrieve value from context provider hosted-zone since account/region are not specified at the stack level. Configure "env" with an account and region when you define your stack.See https://docs.aws.amazon.com/cdk/latest/guide/environments.html for more details.
Error: Cannot retrieve value from context provider hosted-zone since account/region are not specified at the stack level. Configure "env" with an account and region when you define your stack.See https://docs.aws.amazon.com/cdk/latest/guide/environments.html for more details.
at Function.getValue (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-kernel-NRB2Iu/node_modules/aws-cdk-lib/core/lib/context-provider.js:2:417)
at Function.fromLookup (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-kernel-NRB2Iu/node_modules/aws-cdk-lib/aws-route53/lib/hosted-zone.js:1:2194)
at /private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:8249:114
at Kernel._wrapSandboxCode (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:8840:24)
at /private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:8249:87
at Kernel._ensureSync (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:8821:28)
at Kernel.sinvoke (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:8249:34)
at KernelHost.processRequest (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:9761:36)
at KernelHost.run (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:9724:22)
at Immediate._onImmediate (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime2184559236728989524/lib/program.js:9725:46)
at software.amazon.jsii.JsiiRuntime.processErrorResponse(JsiiRuntime.java:124)
at software.amazon.jsii.JsiiRuntime.requestResponse(JsiiRuntime.java:95)
at software.amazon.jsii.JsiiClient.callStaticMethod(JsiiClient.java:168)
at software.amazon.jsii.JsiiObject.jsiiStaticCall(JsiiObject.java:187)
at software.amazon.jsii.JsiiObject.jsiiStaticCall(JsiiObject.java:166)
at software.amazon.awscdk.services.route53.HostedZone.fromLookup(HostedZone.java:84)
at com.seanshubin.condorcet.deploy.aws.domain.Runner$WebsiteStack.createApi(Runner.kt:378)
at com.seanshubin.condorcet.deploy.aws.domain.Runner$WebsiteStack.<init>(Runner.kt:370)
at com.seanshubin.condorcet.deploy.aws.domain.Runner.run(Runner.kt:104)
at com.seanshubin.condorcet.deploy.aws.console.EntryPoint.main(EntryPoint.kt:8)

Subprocess exited with error 1
```

## Switching to us-west-1
```kotlin
val distribution = Distribution.Builder.create(this, Names.appDistributionName)
    .defaultBehavior(staticSiteBehavior)
    .certificate(certificate)
    .additionalBehaviors(additionalBehaviors)
    .errorResponses(errorResponses)
    .defaultRootObject("index.html")
    .domainNames(domainNames)
    .build()
```

```
Error: Distribution certificates must be in the us-east-1 region and the certificate you provided is in us-west-1.
at new Distribution (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-kernel-HvBqoS/node_modules/aws-cdk-lib/aws-cloudfront/lib/distribution.js:1:1169)
at /private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:8412:58
at Kernel._wrapSandboxCode (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:8840:24)
at Kernel._create (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:8412:34)
at Kernel.create (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:8153:29)
at KernelHost.processRequest (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:9761:36)
at KernelHost.run (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:9724:22)
at Immediate._onImmediate (/private/var/folders/1r/bk8v_lv17yxblw2qlr4pnjv00000gn/T/jsii-java-runtime3782529564724984116/lib/program.js:9725:46)
at processImmediate (node:internal/timers:464:21)
at software.amazon.jsii.JsiiRuntime.processErrorResponse(JsiiRuntime.java:124)
at software.amazon.jsii.JsiiRuntime.requestResponse(JsiiRuntime.java:95)
at software.amazon.jsii.JsiiClient.createObject(JsiiClient.java:89)
at software.amazon.jsii.JsiiEngine.createNewObject(JsiiEngine.java:603)
at software.amazon.awscdk.services.cloudfront.Distribution.<init>(Distribution.java:45)
at software.amazon.awscdk.services.cloudfront.Distribution$Builder.build(Distribution.java:416)
at com.seanshubin.condorcet.deploy.aws.domain.Runner$WebsiteStack.createCloudfrontDistribution(Runner.kt:465)
at com.seanshubin.condorcet.deploy.aws.domain.Runner$WebsiteStack.<init>(Runner.kt:373)
at com.seanshubin.condorcet.deploy.aws.domain.Runner.run(Runner.kt:106)
at com.seanshubin.condorcet.deploy.aws.console.EntryPoint.main(EntryPoint.kt:8)
```

### Using certificate in us-east while deploying from us-west

```
Invalid certificate ARN: arn:aws:acm:us-east-1:964638509728:certificate/f703477d-855c-48df-bc4f-74207cb80bc7. Certifica
te must be in 'us-west-1'. (Service: AmazonApiGatewayV2; Status Code: 400; Error Code: BadRequestException; Request ID:
6b4206a8-f2da-4a18-8f57-3f5db035f9b7; Proxy: null)
```

## What should not be at the stack level
- registered domain
- certificate