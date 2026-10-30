# Go Diagnostics

Inline Go compile errors and vet checks in the free IntelliJ IDEA (2026.2+), powered by
[gopls](https://go.dev/gopls/), Go's official language server, through IntelliJ's built-in LSP client.

What you get as you type, without saving or building:

- **Syntax, type and compile errors**: undefined names, type mismatches, unused variables and imports, missing returns
- **Vet analyzers**: printf format mismatches, unreachable code, copied locks, shadowed loop variables and the rest of gopls' default set; each finding is labelled with its analyzer name
- **Optional staticcheck**: SA, S, ST and QF checks, toggled in settings
- **go.mod / go.work problems**: unknown or unused requirements, version issues
- **Quick fixes** on <kbd>Alt+Enter</kbd> wherever gopls offers one, e.g. add an import or remove an unused variable

Problems also show in the *Problems* tool window and on the editor scrollbar.

By default this plugin does diagnostics and quick fixes only, so it won't duplicate another Go
plugin's completion or navigation. Clear **Diagnostics and quick fixes only** in settings to get
gopls completion, hover, go-to-definition, rename, find usages and inlay hints as well.

## Requirements

- IntelliJ IDEA 2026.2 or newer (the free tier is fine)
- Go, plus gopls: `go install golang.org/x/tools/gopls@latest`

gopls is found via the configured path, `PATH`, `GOBIN`, each `GOPATH` entry's `bin`, then `~/go/bin`.
If it's missing you get a notification with a link to the settings.

`.go` files are highlighted by IntelliJ's bundled TextMate Go grammar.

## Settings

*Settings | Tools | Go Diagnostics*

| Setting | Default |
|---|---|
| gopls executable | found automatically |
| Run staticcheck analyzers | off |
| Diagnostics and quick fixes only | on |

Applying restarts gopls in every open project. gopls can also be restarted from the
*Language Services* widget in the status bar.

The plugin stays inactive when JetBrains' Go plugin or Go Language Helper is loaded, since both
already provide Go diagnostics.

## Build and install

```sh
./gradlew test buildPlugin
```

Then *Settings | Plugins | ⚙ | Install Plugin from Disk…* and pick `build/distributions/go-diagnostics-0.1.0.zip`.

## Layout

| File | Purpose |
|---|---|
| `GoplsIntegrationProvider.kt` | Starts gopls when a Go file opens; process command line and gopls settings |
| `GoplsCustomization.kt` | Which LSP features are on; labels analyzer diagnostics |
| `GoplsLocator.kt` | Finds the gopls executable |
| `GoDiagnosticsSettings.kt`, `GoDiagnosticsConfigurable.kt` | Settings storage and page |

## License

[MIT](LICENSE)
