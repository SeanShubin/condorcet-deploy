package com.seanshubin.condorcet.deploy.console

import com.seanshubin.condorcet.deploy.contract.FilesContract
import com.seanshubin.condorcet.deploy.contract.FilesDelegate
import com.seanshubin.condorcet.deploy.domain.ConfigurationValues
import com.seanshubin.condorcet.deploy.domain.ConfigurationValuesImpl
import com.seanshubin.condorcet.deploy.domain.Deployer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

class DependencyInjection(val commandLineArguments: Array<String>) {
    private val files: FilesContract = FilesDelegate
    private val propertiesPathName: String = "local-config/secrets.properties"
    private val propertiesPath: Path = Paths.get(propertiesPathName)
    private val charset: Charset = StandardCharsets.UTF_8
    private val configurationValues: ConfigurationValues = ConfigurationValuesImpl(files, propertiesPath, charset)
    val runner: Runnable = Deployer(configurationValues)
}
