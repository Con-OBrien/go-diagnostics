import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2026.2.3")
        // The LSP API is a separate product module; without this it isn't on the compile classpath.
        bundledModule("intellij.platform.lsp")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // LspIntegrationProvider / LspClientDescriptor arrived in 2026.1.4; 2026.2 is the first
            // full release line that is guaranteed to have them.
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }
}
