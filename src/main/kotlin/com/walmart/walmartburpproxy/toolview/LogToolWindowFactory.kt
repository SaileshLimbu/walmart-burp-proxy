package com.walmart.walmartburpproxy.toolview

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class LogToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val consoleView: ConsoleView = ConsoleViewImpl(project, false)
        val content = ContentFactory.getInstance().createContent(consoleView.component, "", false)
        toolWindow.contentManager.addContent(content)

        consoleView.print("Walmart Jacoco Log Window Initialized!\n", ConsoleViewContentType.SYSTEM_OUTPUT)
    }
}