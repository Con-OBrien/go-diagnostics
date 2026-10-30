package com.conorobrien.godiagnostics

import java.nio.file.Files
import java.nio.file.Path

/**
 * Finds the gopls executable. Pure function of its inputs so it can be unit tested.
 *
 * Search order: explicit setting, PATH, GOBIN, each GOPATH entry's bin, then ~/go/bin.
 * The last three matter because an IDE started from the Start menu or Dock often lacks the
 * shell PATH that `go install` relies on.
 */
object GoplsLocator {

    fun locate(
        configuredPath: String?,
        env: Map<String, String>,
        home: Path,
        isWindows: Boolean,
        isExecutable: (Path) -> Boolean = ::defaultIsExecutable,
    ): Path? {
        configuredPath?.trim()?.removeSurrounding("\"")?.takeIf { it.isNotEmpty() }?.let { raw ->
            // An explicit setting wins; if it is wrong, report "not found" rather than silently
            // using a different gopls than the one the user chose.
            return toPath(raw)?.takeIf(isExecutable)
        }

        val exe = if (isWindows) "gopls.exe" else "gopls"
        val separator = if (isWindows) ";" else ":"

        val dirs = buildList {
            addAll(env.getIgnoreCase("PATH", isWindows).orEmpty().split(separator))
            env.getIgnoreCase("GOBIN", isWindows)?.let { add(it) }
            env.getIgnoreCase("GOPATH", isWindows).orEmpty().split(separator)
                .filter { it.isNotBlank() }
                .forEach { add(Path.of(it.trim()).resolve("bin").toString()) }
            add(home.resolve("go").resolve("bin").toString())
        }

        return dirs.asSequence()
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
            .mapNotNull { toPath(it)?.resolve(exe) }
            .firstOrNull(isExecutable)
    }

    /** Windows environment variable names are case-insensitive ("Path" vs "PATH"). */
    private fun Map<String, String>.getIgnoreCase(key: String, isWindows: Boolean): String? =
        if (!isWindows) this[key] else entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

    private fun toPath(raw: String): Path? = try {
        Path.of(raw)
    } catch (_: Exception) {
        null
    }

    private fun defaultIsExecutable(path: Path): Boolean = Files.isRegularFile(path) && Files.isExecutable(path)
}
