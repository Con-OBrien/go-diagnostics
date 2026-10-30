package com.conorobrien.godiagnostics

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "GoDiagnosticsSettings", storages = [Storage("go-diagnostics.xml")])
class GoDiagnosticsSettings : SimplePersistentStateComponent<GoDiagnosticsSettings.Options>(Options()) {

    class Options : BaseState() {
        /** Blank means "find it automatically". */
        var goplsPath by string()

        /** gopls' built-in staticcheck analyzers (SA/S/ST/QF checks) on top of the default vet set. */
        var staticcheck by property(false)

        /**
         * Only diagnostics and quick fixes. Turn off to also get completion, hover, navigation,
         * rename and the rest of gopls through IntelliJ's LSP client.
         */
        var diagnosticsOnly by property(true)
    }

    companion object {
        fun getInstance(): GoDiagnosticsSettings = service()
    }
}
