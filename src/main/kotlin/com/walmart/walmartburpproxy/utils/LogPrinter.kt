package com.walmart.walmartburpproxy.utils

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

object LogPrinter {
    fun printLog(project: Project, message: String, consoleViewContentType: ConsoleViewContentType) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Jococo Logs")
        toolWindow?.show {  // Ensure it's visible
            val content = toolWindow.contentManager.selectedContent
            if (content != null) {
                val consoleView = content.component as? ConsoleView
                consoleView?.print(message, consoleViewContentType)
            }
        }
    }
}