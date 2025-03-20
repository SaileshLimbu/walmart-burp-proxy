package com.walmart.walmartburpproxy.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.net.NetworkInterface

class SetProxyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val localIp = getLocalIpAddress()
        if (localIp.isNullOrEmpty()) {
            Messages.showErrorDialog("Failed to get local IP address!", "Error")
            return
        }

        setAndroidGlobalProxy(localIp, 8888)
    }

    private fun getLocalIpAddress(): String? {
        try {
            // Get all network interfaces
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()

            for (networkInterface in networkInterfaces) {
                // Skip inactive interfaces
                if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual) {
                    continue
                }

                // Get addresses for this interface
                val addresses = networkInterface.inetAddresses.toList()
                for (address in addresses) {
                    // Skip IPv6 addresses and loopback addresses
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun setAndroidGlobalProxy(host: String, port: Int): Boolean {
        try {
            val proxyString = "$host:$port"
            val commandLine = GeneralCommandLine()
                .withExePath("adb")
                .withParameters("shell", "settings", "put", "global", "http_proxy", proxyString)

            val processHandler = CapturingProcessHandler(commandLine)
            val result = processHandler.runProcess(30000)

            if (result.exitCode != 0) {
                Messages.showErrorDialog( "Failed to set global proxy: ${result.stderr}", "Command Failed")
                return false
            }

            Messages.showInfoMessage( "Global proxy set to $proxyString", "Success")
            return true
        } catch (e: Exception) {
            Messages.showErrorDialog( "Error setting global proxy: ${e.message}", "Error")
            return false
        }
    }
}