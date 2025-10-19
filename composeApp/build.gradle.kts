import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqlDelight)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
val sqldelightVersion = libs.versions.sqldelight.get()

kotlin {
    jvm()

    sourceSets.all {
        languageSettings {
            languageVersion = "2.2"
            apiVersion = "2.2"
            progressiveMode = true
        }

    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation("com.auth0:java-jwt:4.4.0")
                implementation("io.ktor:ktor-client-core:2.3.11")
                implementation("io.ktor:ktor-client-cio:2.3.11")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
                implementation("io.ktor:ktor-client-logging:2.3.11")
                implementation("org.slf4j:slf4j-api:2.0.13")
                implementation("ch.qos.logback:logback-classic:1.4.14")
                implementation("app.cash.sqldelight:runtime:${sqldelightVersion}")
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.kotlinx.datetime)

                implementation("at.favre.lib:bcrypt:0.10.2")
                implementation(projects.shared)

            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation("io.insert-koin:koin-core:3.3.3")
                implementation("io.insert-koin:koin-compose:1.1.2")
                implementation("app.cash.sqldelight:sqlite-driver:${sqldelightVersion}")
                implementation("com.github.librepdf:openpdf:1.3.32")
                implementation("org.apache.poi:poi:5.3.0")
                implementation("org.apache.poi:poi-ooxml:5.3.0")

                // optional (only if you embed images or need advanced streaming)
                implementation("org.apache.poi:poi-ooxml-full:5.3.0")
            }
        }
    }
}

tasks.register<ShadowJar>("shadowJar") {
    archiveBaseName.set("desktop")
    archiveVersion.set("1.0.9")
    archiveClassifier.set("all")

    manifest {
        attributes["Main-Class"] = "org.montigny.client.Main"
        attributes["APP_JARS"] = archiveFileName.get()
    }

    val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")
    from(jvmMainCompilation.output.classesDirs)
    from(jvmMainCompilation.output.resourcesDir)
    from(kotlin.sourceSets.getByName("commonMain").resources)

    configurations = listOf(project.configurations.getByName("jvmRuntimeClasspath"))

    mergeServiceFiles()

    // Corrected Kotlin DSL syntax for relocate()
    relocate("org.slf4j", "shadow.org.slf4j")
    relocate("ch.qos.logback", "shadow.ch.qos.logback")

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    doLast {
        println("✅ Shadow JAR built successfully at: ${archiveFile.get().asFile.absolutePath}")
    }
}

compose.desktop {
    application {
        mainClass = "org.montigny.client.Main"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "Brotherhood Office"
            packageVersion = "1.0.1"
            modules("java.desktop")
            includeAllModules = true

            description = "Brotherhood Office — Admin tool for managing sawmill operations, payroll, and workforce transparency."
            // Publisher info
            vendor = "Brotherhood of Timber Workers"
            copyright = "© 2025 Brotherhood of Timber Workers"
            licenseFile.set(project.file("LICENSE.txt"))

            // Icons
            windows {
                menuGroup = "Brotherhood of Timber Workers"
                upgradeUuid = "C54A3B01-1234-4E12-A4B1-2234ACBFD701"
                iconFile.set(project.file("src/jvmMain/resources/icons/btw_office.ico"))
                shortcut = true
            }
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("org.montigny.client.database")
            //verifyMigrations.set(false)
            // sourceFolders = listOf("sqldelight")
        }
    }
}