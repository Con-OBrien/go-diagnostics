package com.conorobrien.godiagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

class GoplsLocatorTest {

    private val home = Path.of("/home/conor")

    private fun locate(
        env: Map<String, String>,
        existing: Set<String>,
        configured: String? = null,
        isWindows: Boolean = false,
    ): String? = GoplsLocator.locate(configured, env, home, isWindows) { norm(it) in existing }?.let(::norm)

    /** Tests run on Windows too, where Path.toString() uses backslashes. */
    private fun norm(path: Path): String = path.toString().replace('\\', '/')

    @Test
    fun `finds gopls on PATH first`() {
        val found = locate(
            env = mapOf("PATH" to "/usr/bin:/opt/go/bin"),
            existing = setOf("/opt/go/bin/gopls", "/home/conor/go/bin/gopls"),
        )
        assertEquals("/opt/go/bin/gopls", found)
    }

    @Test
    fun `falls back to GOBIN then GOPATH bin then home go bin`() {
        assertEquals(
            "/custom/bin/gopls",
            locate(mapOf("GOBIN" to "/custom/bin"), setOf("/custom/bin/gopls", "/home/conor/go/bin/gopls")),
        )
        assertEquals(
            "/second/bin/gopls",
            locate(mapOf("GOPATH" to "/first:/second"), setOf("/second/bin/gopls")),
        )
        assertEquals("/home/conor/go/bin/gopls", locate(emptyMap(), setOf("/home/conor/go/bin/gopls")))
    }

    @Test
    fun `explicit setting wins and is not silently replaced`() {
        assertEquals(
            "/mine/gopls",
            locate(mapOf("PATH" to "/usr/bin"), setOf("/mine/gopls", "/usr/bin/gopls"), configured = "/mine/gopls"),
        )
        assertNull(locate(mapOf("PATH" to "/usr/bin"), setOf("/usr/bin/gopls"), configured = "/wrong/gopls"))
    }

    @Test
    fun `windows uses gopls exe, semicolons and case-insensitive Path`() {
        val found = locate(
            env = mapOf("Path" to "C:/Windows;C:/Users/conor/go/bin"),
            existing = setOf("C:/Users/conor/go/bin/gopls.exe"),
            isWindows = true,
        )
        assertEquals("C:/Users/conor/go/bin/gopls.exe", found)
    }

    @Test
    fun `returns null when nothing is installed`() {
        assertNull(locate(mapOf("PATH" to "/usr/bin"), emptySet()))
    }

    @Test
    fun `language ids match what gopls expects`() {
        assertEquals("go", GoFiles.languageId("main.go"))
        assertEquals("go.mod", GoFiles.languageId("go.mod"))
        assertEquals("go.work", GoFiles.languageId("go.work"))
        assertNull(GoFiles.languageId("go.sum"))
        assertNull(GoFiles.languageId("README.md"))
    }

    @Test
    fun `analyzer diagnostics are labelled, compiler errors are not`() {
        assertEquals("declared and not used: x", DiagnosticText.withSource("declared and not used: x", "compiler"))
        assertEquals("unreachable code (unreachable)", DiagnosticText.withSource("unreachable code", "unreachable"))
        assertEquals("already (SA4006)", DiagnosticText.withSource("already (SA4006)", "SA4006"))
        assertEquals("no source", DiagnosticText.withSource("no source", null))
    }
}
