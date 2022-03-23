# Condorcet Deploy
Deploys condorcet voting application to amazon web services via the cloud deployment kit

## Todo List
- do I need an ingress rule to access the database if I am in the same security group
- test to see if RemovalPolicy.RETAIN connects back to the same database
- can I generate the certificate dynamically?
- get it working from a single domain
- figure out cross region stacks
- implement backup/restore
- password reset feature

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
- Get Certificates

## Generate Public Key
- ssh-keygen -y -f CondorcetKey.pem > CondorcetKey.pub

## Aws Credentials
- to list credentials
  - cat ~/.aws/credentials
- to switch credentials
  - export AWS_PROFILE=sean-personal

## Scripts
- deploy
    - provisions if necessary
    - deploys the application
    - starts the application
- backup
    - backs up database
- restore
    - restores database
- teardown
    - removes non-data infrastructure
    - leaves database alone
- purge
    - removes database
    - does not touch backups
