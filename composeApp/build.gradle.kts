import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Opcional – mantém suporte a JS (além do Wasm)
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }
        // Se habilitou js(IR) acima, mantenha também:
        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(compose.html.svg)
            }
        }
    }
}
