import org.gradle.api.tasks.Exec

plugins {
    base
}

val npmExecutable = if (System.getProperty("os.name").startsWith("Windows")) {
    "npm.cmd" // Windows needs the .cmd extension
} else {
    "npm"
}

val npmInstall by tasks.registering(Exec::class) {
    workingDir = file("${project.projectDir}/superuser-ui")
    executable = npmExecutable
    args("install")
}

val npmBuild by tasks.registering(Exec::class) {
    workingDir = file("${project.projectDir}/superuser-ui")
    executable = npmExecutable
    args("run", "build")
    dependsOn(npmInstall)
}

val copyWebDist by tasks.registering(Copy::class) {
    from("${project.projectDir}/superuser-ui/dist")
    into("${rootProject.projectDir}/server/src/main/resources/static")
    dependsOn(npmBuild)
}

// Make sure building the project also builds and copies the web assets
tasks.named("build") {
    dependsOn(copyWebDist)
}
