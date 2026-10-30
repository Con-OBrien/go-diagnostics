package com.conorobrien.godiagnostics

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.util.EnvironmentUtil
import org.eclipse.lsp4j.ConfigurationItem
import java.nio.file.Path

/**
 * Starts one gopls per project the first time a Go file (or go.mod / go.work) is opened.
 * IntelliJ's LSP client then sends every edit to gopls, and gopls answers with diagnostics
 * that the platform draws inline: compile and type errors as you type, plus vet analyzers.
 */
class GoplsIntegrationProvider : LspIntegrationProvider {

    override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspIntegrationProvider.LspClientStarter) {
        if (!GoFiles.isSupported(file)) return
        if (anotherGoIntegrationIsActive()) return

        val settings = GoDiagnosticsSettings.getInstance().state
        val gopls = GoplsLocator.locate(
            configuredPath = settings.goplsPath,
            env = EnvironmentUtil.getEnvironmentMap(),
            home = Path.of(System.getProperty("user.home")),
            isWindows = SystemInfo.isWindows,
        )
        if (gopls == null) {
            notifyGoplsMissingOnce(project, settings.goplsPath)
            return
        }

        clientStarter.ensureClientStarted(
            GoplsClientDescriptor(
                project = project,
                gopls = gopls,
                staticcheck = settings.staticcheck,
                diagnosticsOnly = settings.diagnosticsOnly,
            )
        )
    }

    /** JetBrains' Go plugin (GoLand, or IDEA with a subscription) or another gopls plugin already covers this. */
    private fun anotherGoIntegrationIsActive(): Boolean =
        CONFLICTING_PLUGIN_IDS.any { PluginManagerCore.isLoaded(PluginId.getId(it)) }

    private fun notifyGoplsMissingOnce(project: Project, configuredPath: String?) {
        if (project.getUserData(MISSING_NOTIFIED) == true) return
        project.putUserData(MISSING_NOTIFIED, true)

        val content = if (configuredPath.isNullOrBlank()) {
            "Install it with <code>go install golang.org/x/tools/gopls@latest</code>, then reopen a Go file."
        } else {
            "The configured path <code>$configuredPath</code> isn't an executable gopls."
        }
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification("Go diagnostics are off: gopls not found", content, NotificationType.WARNING)
            .addAction(NotificationAction.createSimpleExpiring("Open settings") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, GoDiagnosticsConfigurable::class.java)
            })
            .notify(project)
    }

    private companion object {
        const val NOTIFICATION_GROUP = "Go Diagnostics"
        val MISSING_NOTIFIED: Key<Boolean> = Key.create("com.conorobrien.godiagnostics.missingNotified")
        val CONFLICTING_PLUGIN_IDS = listOf(
            "org.jetbrains.plugins.go",            // JetBrains Go plugin
            "io.github.cag11.go-language-helper",  // Go Language Helper (also runs gopls)
        )
    }
}

internal object GoFiles {
    fun isSupported(file: VirtualFile): Boolean = languageId(file.name) != null

    /** gopls' language IDs: https://go.dev/gopls/ (go, go.mod, go.work). */
    fun languageId(fileName: String): String? = when {
        fileName.endsWith(".go") -> "go"
        fileName == "go.mod" -> "go.mod"
        fileName == "go.work" -> "go.work"
        else -> null
    }
}

private class GoplsClientDescriptor(
    project: Project,
    private val gopls: Path,
    private val staticcheck: Boolean,
    diagnosticsOnly: Boolean,
) : ProjectWideLspClientDescriptor(project, "gopls") {

    override fun isSupportedFile(file: VirtualFile): Boolean = GoFiles.isSupported(file)

    override fun getLanguageId(file: VirtualFile): String = GoFiles.languageId(file.name) ?: super.getLanguageId(file)

    override fun createCommandLine(): GeneralCommandLine {
        // The default (CONSOLE) parent environment is the login-shell environment, so gopls can
        // find `go`, GOPATH, GOFLAGS and GOPRIVATE the same way a terminal would.
        val commandLine = GeneralCommandLine(gopls.toString(), "serve")
        project.basePath?.let { commandLine.withWorkingDirectory(Path.of(it)) }
        return commandLine
    }

    // gopls reads settings from both initializationOptions and workspace/configuration("gopls").
    override fun createInitializationOptions(): Any = goplsSettings()

    override fun getWorkspaceConfiguration(item: ConfigurationItem): Any? =
        if (item.section == null || item.section == "gopls") goplsSettings() else null

    private fun goplsSettings(): Map<String, Any> = mapOf(
        "staticcheck" to staticcheck,
    )

    override val lspCustomization: LspCustomization = GoplsCustomization(diagnosticsOnly)
}
