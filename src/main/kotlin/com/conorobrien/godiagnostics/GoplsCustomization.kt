package com.conorobrien.godiagnostics

import com.intellij.platform.lsp.api.customization.LspCallHierarchyCustomizer
import com.intellij.platform.lsp.api.customization.LspCallHierarchyDisabled
import com.intellij.platform.lsp.api.customization.LspCodeLensCustomizer
import com.intellij.platform.lsp.api.customization.LspCodeLensDisabled
import com.intellij.platform.lsp.api.customization.LspCompletionCustomizer
import com.intellij.platform.lsp.api.customization.LspCompletionDisabled
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspDiagnosticsCustomizer
import com.intellij.platform.lsp.api.customization.LspDiagnosticsSupport
import com.intellij.platform.lsp.api.customization.LspDocumentColorCustomizer
import com.intellij.platform.lsp.api.customization.LspDocumentColorDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentHighlightsCustomizer
import com.intellij.platform.lsp.api.customization.LspDocumentHighlightsDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentLinkCustomizer
import com.intellij.platform.lsp.api.customization.LspDocumentLinkDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentSymbolCustomizer
import com.intellij.platform.lsp.api.customization.LspDocumentSymbolDisabled
import com.intellij.platform.lsp.api.customization.LspFindReferencesCustomizer
import com.intellij.platform.lsp.api.customization.LspFindReferencesDisabled
import com.intellij.platform.lsp.api.customization.LspFoldingRangeCustomizer
import com.intellij.platform.lsp.api.customization.LspFoldingRangeDisabled
import com.intellij.platform.lsp.api.customization.LspFormattingCustomizer
import com.intellij.platform.lsp.api.customization.LspFormattingDisabled
import com.intellij.platform.lsp.api.customization.LspGoToDefinitionCustomizer
import com.intellij.platform.lsp.api.customization.LspGoToDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspGoToTypeDefinitionCustomizer
import com.intellij.platform.lsp.api.customization.LspGoToTypeDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspHoverCustomizer
import com.intellij.platform.lsp.api.customization.LspHoverDisabled
import com.intellij.platform.lsp.api.customization.LspInlayHintCustomizer
import com.intellij.platform.lsp.api.customization.LspInlayHintDisabled
import com.intellij.platform.lsp.api.customization.LspOptimizeImportsCustomizer
import com.intellij.platform.lsp.api.customization.LspOptimizeImportsDisabled
import com.intellij.platform.lsp.api.customization.LspRenameCustomizer
import com.intellij.platform.lsp.api.customization.LspRenameDisabled
import com.intellij.platform.lsp.api.customization.LspSelectionRangeCustomizer
import com.intellij.platform.lsp.api.customization.LspSelectionRangeDisabled
import com.intellij.platform.lsp.api.customization.LspSemanticTokensCustomizer
import com.intellij.platform.lsp.api.customization.LspSemanticTokensDisabled
import com.intellij.platform.lsp.api.customization.LspSignatureHelpCustomizer
import com.intellij.platform.lsp.api.customization.LspSignatureHelpDisabled
import com.intellij.platform.lsp.api.customization.LspTypeHierarchyCustomizer
import com.intellij.platform.lsp.api.customization.LspTypeHierarchyDisabled
import com.intellij.platform.lsp.api.customization.LspWorkspaceSymbolCustomizer
import com.intellij.platform.lsp.api.customization.LspWorkspaceSymbolDisabled
import org.eclipse.lsp4j.Diagnostic

/**
 * Diagnostics and code actions (quick fixes) stay on. With [diagnosticsOnly], every other gopls
 * feature is switched off so this plugin doesn't duplicate another Go plugin's completion or navigation.
 * Commands stay on because several gopls quick fixes are implemented as server commands.
 */
internal class GoplsCustomization(private val diagnosticsOnly: Boolean) : LspCustomization() {

    override val diagnosticsCustomizer: LspDiagnosticsCustomizer = GoplsDiagnostics

    override val completionCustomizer: LspCompletionCustomizer
        get() = if (diagnosticsOnly) LspCompletionDisabled else super.completionCustomizer
    override val hoverCustomizer: LspHoverCustomizer
        get() = if (diagnosticsOnly) LspHoverDisabled else super.hoverCustomizer
    override val goToDefinitionCustomizer: LspGoToDefinitionCustomizer
        get() = if (diagnosticsOnly) LspGoToDefinitionDisabled else super.goToDefinitionCustomizer
    override val goToTypeDefinitionCustomizer: LspGoToTypeDefinitionCustomizer
        get() = if (diagnosticsOnly) LspGoToTypeDefinitionDisabled else super.goToTypeDefinitionCustomizer
    override val semanticTokensCustomizer: LspSemanticTokensCustomizer
        get() = if (diagnosticsOnly) LspSemanticTokensDisabled else super.semanticTokensCustomizer
    override val formattingCustomizer: LspFormattingCustomizer
        get() = if (diagnosticsOnly) LspFormattingDisabled else super.formattingCustomizer
    override val findReferencesCustomizer: LspFindReferencesCustomizer
        get() = if (diagnosticsOnly) LspFindReferencesDisabled else super.findReferencesCustomizer
    override val optimizeImportsCustomizer: LspOptimizeImportsCustomizer
        get() = if (diagnosticsOnly) LspOptimizeImportsDisabled else super.optimizeImportsCustomizer
    override val documentColorCustomizer: LspDocumentColorCustomizer
        get() = if (diagnosticsOnly) LspDocumentColorDisabled else super.documentColorCustomizer
    override val documentLinkCustomizer: LspDocumentLinkCustomizer
        get() = if (diagnosticsOnly) LspDocumentLinkDisabled else super.documentLinkCustomizer
    override val foldingRangeCustomizer: LspFoldingRangeCustomizer
        get() = if (diagnosticsOnly) LspFoldingRangeDisabled else super.foldingRangeCustomizer
    override val inlayHintCustomizer: LspInlayHintCustomizer
        get() = if (diagnosticsOnly) LspInlayHintDisabled else super.inlayHintCustomizer
    override val documentHighlightsCustomizer: LspDocumentHighlightsCustomizer
        get() = if (diagnosticsOnly) LspDocumentHighlightsDisabled else super.documentHighlightsCustomizer
    override val signatureHelpCustomizer: LspSignatureHelpCustomizer
        get() = if (diagnosticsOnly) LspSignatureHelpDisabled else super.signatureHelpCustomizer
    override val documentSymbolCustomizer: LspDocumentSymbolCustomizer
        get() = if (diagnosticsOnly) LspDocumentSymbolDisabled else super.documentSymbolCustomizer
    override val workspaceSymbolCustomizer: LspWorkspaceSymbolCustomizer
        get() = if (diagnosticsOnly) LspWorkspaceSymbolDisabled else super.workspaceSymbolCustomizer
    override val callHierarchyCustomizer: LspCallHierarchyCustomizer
        get() = if (diagnosticsOnly) LspCallHierarchyDisabled else super.callHierarchyCustomizer
    override val typeHierarchyCustomizer: LspTypeHierarchyCustomizer
        get() = if (diagnosticsOnly) LspTypeHierarchyDisabled else super.typeHierarchyCustomizer
    override val selectionRangeCustomizer: LspSelectionRangeCustomizer
        get() = if (diagnosticsOnly) LspSelectionRangeDisabled else super.selectionRangeCustomizer
    override val codeLensCustomizer: LspCodeLensCustomizer
        get() = if (diagnosticsOnly) LspCodeLensDisabled else super.codeLensCustomizer
    override val renameCustomizer: LspRenameCustomizer
        get() = if (diagnosticsOnly) LspRenameDisabled else super.renameCustomizer
}

/**
 * Appends the analyzer name to non-compiler diagnostics, e.g. "result of fmt.Sprintf call not used (unusedresult)",
 * so it's clear whether a squiggle is a build error or a vet/staticcheck finding.
 */
internal object GoplsDiagnostics : LspDiagnosticsSupport() {
    override fun getMessage(diagnostic: Diagnostic): String =
        DiagnosticText.withSource(super.getMessage(diagnostic), diagnostic.source)
}

internal object DiagnosticText {
    /** Sources that are part of compiling the code; everything else is an analyzer and gets labelled. */
    private val buildSources = setOf("compiler", "syntax", "go list", "go command")

    fun withSource(message: String, source: String?): String {
        if (source.isNullOrBlank() || source in buildSources) return message
        val suffix = "($source)"
        return if (message.trimEnd().endsWith(suffix)) message else "$message $suffix"
    }
}
