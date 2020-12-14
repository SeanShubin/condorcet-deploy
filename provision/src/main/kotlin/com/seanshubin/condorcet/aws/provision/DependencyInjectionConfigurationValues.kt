package com.seanshubin.condorcet.aws.provision

import com.seanshubin.condorcet.aws.domain.ConfigurationValues
import com.seanshubin.condorcet.aws.domain.Provision

class DependencyInjectionConfigurationValues(configurationValues: ConfigurationValues) {
  val runner: Runnable = Provision(configurationValues)
}
