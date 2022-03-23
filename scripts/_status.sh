#!/usr/bin/env bash

aws cloudformation list-stacks --output text --query "StackSummaries[?StackStatus!='DELETE_COMPLETE'].[StackName, StackStatus]"
