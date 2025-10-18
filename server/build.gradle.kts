import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "org.montigny.client"
version = "1.0.0"



application {
    mainClass.set("org.montigny.client.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}




val ktorVersion = "2.3.11"
val oldKtor = "3.2.2"
dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)

    implementation(libs.ktor.serVerCore)

    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation("io.ktor:ktor-server-cors:${ktorVersion}")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

   implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    //Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    //Hikari + Postgres
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.7.7")
    // Flyway
    implementation("org.flywaydb:flyway-core:9.20.0")
    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    // Koin for DI
    implementation("io.insert-koin:koin-core:3.3.3")
    implementation("io.insert-koin:koin-ktor:$oldKtor")

    implementation(files("libs/kompendium-core-3.14.1-SNAPSHOT.jar"))
    implementation(files("libs/kompendium-json-schema-3.14.1-SNAPSHOT.jar"))
    implementation(files("libs/kompendium-oas-3.14.1-SNAPSHOT.jar"))

    implementation("org.instancio:instancio-core:2.14.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
// 🔥 Exclude duplicates in packaging tasks
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Apply also to distTar and distZip explicitly
tasks.named<Zip>("distZip") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.named<Tar>("distTar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJarForServer") {
    archiveBaseName.set("server")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("all")

    manifest {
        attributes["Main-Class"] = "org.montigny.client.ApplicationKt"
    }

    // ✅ Include compiled classes directly (Kotlin JVM project)
    from(sourceSets.main.get().output)

    // ✅ Include runtime dependencies
    configurations = listOf(project.configurations.runtimeClasspath.get())

    // ✅ Merge service files and remove signatures
    mergeServiceFiles()
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // Optional relocations if you want to avoid dependency conflicts
    relocate("org.slf4j", "shadow.org.slf4j")
    relocate("ch.qos.logback", "shadow.ch.qos.logback")

    doLast {
        println("✅ Server fat JAR built successfully at: ${archiveFile.get().asFile.absolutePath}")
    }
}