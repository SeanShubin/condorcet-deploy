#!/usr/bin/env bash

aws cloudformation list-stacks --output text --query "StackSummaries[?StackStatus=='CREATE_COMPLETE'].[StackName]"
