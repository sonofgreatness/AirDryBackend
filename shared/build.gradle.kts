import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)

    
}
kotlin {
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    /*wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }*/
    
    sourceSets {
        commonMain.dependencies {

            val ktorVersion = "2.3.11"
            implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation(files("libs2/kompendium-core-3.14.1-SNAPSHOT.jar"))
            implementation(files("libs2/kompendium-json-schema-3.14.1-SNAPSHOT.jar"))
            implementation(files("libs2/kompendium-oas-3.14.1-SNAPSHOT.jar"))

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

    }
}

