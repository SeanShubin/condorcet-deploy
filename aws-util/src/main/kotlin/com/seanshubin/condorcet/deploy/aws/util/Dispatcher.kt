package com.seanshubin.condorcet.deploy.aws.util

class Dispatcher(val commandFactory: CommandFactory,
                 val environmentFactory: EnvironmentFactory) {
  fun dispatch(args: Array<String>) {
    val commandName = args[0]
    val command = commandFactory.fromName(commandName)
    val environment = environmentFactory.create(args.toList())
    command.exec(environment)
  }
}
