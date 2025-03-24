package com.walmart.walmartburpproxy.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.walmart.walmartburpproxy.utils.LogPrinter
import java.io.File


class RunCoverageAction : AnAction() {

    override fun update(e: AnActionEvent) {
        // Enable/disable the action based on whether we have a file
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val featureName = findModuleForFile(file)
        if (featureName == null) {
            showNotification(project, "Cannot determine module for this file", NotificationType.ERROR)
            return
        }

        runGradleTask(project, "${project.basePath}/gradlew.bat", featureName)
    }

    private fun findModuleForFile(file: VirtualFile): String? {
        val filePath = file.path
        val srcIndex = filePath.indexOf("src")
        if (srcIndex == -1) return null

        val moduleRootPath = filePath.substring(0, srcIndex).trimEnd { it == '/' }
        val moduleName = moduleRootPath.substringAfterLast("/")

        return moduleName
    }

    private fun runGradleTask(project: Project, gradlewPath: String, moduleName: String) {
        if (!File(gradlewPath).setExecutable(true)) {
            showNotification(project, "Failed to make gradlew executable", NotificationType.ERROR)
            return
        }

        val commandLine = GeneralCommandLine()
            .withExePath(gradlewPath)
            .withParameters("build")
            .withWorkDirectory(project.basePath)

        LogPrinter.printLog(
            project,
            "Running: ${commandLine.commandLineString} on $moduleName",
            ConsoleViewContentType.NORMAL_OUTPUT
        )

        val processHandler = OSProcessHandler(commandLine)

        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (outputType === ProcessOutputTypes.STDOUT) {
                    LogPrinter.printLog(project, event.text, ConsoleViewContentType.SYSTEM_OUTPUT)
                }
                if (outputType === ProcessOutputTypes.STDERR) {
                    LogPrinter.printLog(project, event.text, ConsoleViewContentType.ERROR_OUTPUT)
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                val exitCode = event.exitCode
                if (exitCode == 0) {
                    showNotification(project, "Code coverage completed successfully", NotificationType.INFORMATION)
                } else {
                    showNotification(project, "Code coverage failed with exit code $exitCode", NotificationType.ERROR)
                }
            }
        })

        processHandler.startNotify()
    }

    private fun showNotification(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Code Coverage")
            .createNotification(content, type)
            .notify(project)
    }
}