# Condorcet Deploy
Deploys condorcet voting application to amazon web services via the cloud deployment kit

## Environment
- account
  - 964638509728
- region
  - us-east-1

## Prerequisites

`npm install -g aws-cdk`
`aws configure`
`cdk bootstrap aws://964638509728/us-west-1 aws://964638509728/us-east-1`
`cdk --version`

## Manual Steps
- Create a keypair
- Register a domain
- Create a cloudfront origin request policy "AllCookies"

## Aws Credentials
- to list credentials
  - cat ~/.aws/credentials
- to switch credentials
  - export AWS_PROFILE=sean-personal


## Scripts
- provision
    - sets up any missing infrastructure
    - does nothing if all infrastructure is already in place
    - database would be set up here
- deploy (stop provision deploy start)
    - pushes code
    - does not touch database
- backup
    - backs up database
- restore
    - restores database
- start
    - launches all processes
- stop
    - halts all processes
    - does nothing if not running
- teardown (stop teardown)
    - removes non-data infrastructure
    - leaves database alone
- purge (backup stop teardown purge)
    - removes database
    - does not touch backups

## Todo List
- set up code pipeline
    - deploy java server application to ec2 
    - deploy javascript client application to s3 
- set up rds database
- set up ec2 instance
    - deploy java server application
- set up s3 instance
    - serve over http
    - deploy javascript client application
- bind to url with CloudFront
- ability to send emails
    - for password reset 

## Scripts
- `create.sh`
    - mvn package
    - cdk deploy
- `destroy.sh`
    - cdk destroy
