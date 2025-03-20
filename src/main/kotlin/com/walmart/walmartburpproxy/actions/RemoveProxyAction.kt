package com.walmart.walmartburpproxy.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class RemoveProxyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        removeAndroidGlobalProxy()
    }

    private fun removeAndroidGlobalProxy(): Boolean {
        try {
            val commandLine = GeneralCommandLine()
                .withExePath("adb")
                .withParameters("shell", "settings", "put", "global", "http_proxy", ":0")

            val processHandler = CapturingProcessHandler(commandLine)
            val result = processHandler.runProcess(30000) // 30 seconds timeout

            if (result.exitCode != 0) {
                Messages.showErrorDialog("Failed to remove global proxy: ${result.stderr}", "Command Failed")

                return false
            }
            Messages.showInfoMessage("Global proxy removed successfully", "Success")

            return true
        } catch (e: Exception) {
            Messages.showErrorDialog("Error removing global proxy: ${e.message}", "Error")
            return false
        }
    }
}