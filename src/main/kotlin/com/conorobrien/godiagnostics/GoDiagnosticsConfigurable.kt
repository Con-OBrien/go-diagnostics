package com.conorobrien.godiagnostics

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.SystemInfo
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.EnvironmentUtil
import java.nio.file.Path

/** Settings | Tools | Go Diagnostics. Applying restarts gopls in every open project. */
class GoDiagnosticsConfigurable : BoundConfigurable("Go Diagnostics") {

    private val options get() = GoDiagnosticsSettings.getInstance().state

    override fun createPanel(): DialogPanel = panel {
        row("gopls executable:") {
            textField()
                .bindText({ options.goplsPath.orEmpty() }, { options.goplsPath = it.trim().ifEmpty { null } })
                .align(AlignX.FILL)
                .applyToComponent { emptyText.setText("Find automatically") }
                .comment(detectedComment())
        }
        row {
            checkBox("Run staticcheck analyzers")
                .bindSelected({ options.staticcheck }, { options.staticcheck = it })
                .comment("Adds staticcheck's SA, S, ST and QF checks to gopls' default vet analyzers.")
        }
        row {
            checkBox("Diagnostics and quick fixes only")
                .bindSelected({ options.diagnosticsOnly }, { options.diagnosticsOnly = it })
                .comment("Clear this to also get completion, hover, go-to-definition, rename and more from gopls.")
        }
    }

    override fun apply() {
        super.apply()
        for (project in ProjectManager.getInstance().openProjects) {
            if (!project.isDisposed) {
                LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(GoplsIntegrationProvider::class.java)
            }
        }
    }

    private fun detectedComment(): String {
        val found = GoplsLocator.locate(
            configuredPath = null,
            env = EnvironmentUtil.getEnvironmentMap(),
            home = Path.of(System.getProperty("user.home")),
            isWindows = SystemInfo.isWindows,
        )
        return if (found != null) {
            "Detected: $found"
        } else {
            "Not found. Install it with <code>go install golang.org/x/tools/gopls@latest</code>."
        }
    }
}
