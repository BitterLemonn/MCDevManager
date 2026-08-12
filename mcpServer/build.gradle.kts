import org.gradle.jvm.application.tasks.CreateStartScripts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

version = libs.versions.versions.name.get()

kotlin {
    jvmToolchain(11)
    compilerOptions.jvmTarget = JvmTarget.JVM_11
}

dependencies {
    implementation(projects.shared)
    implementation(libs.mcp.kotlin.server)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.filekit)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(libs.kotlin.test)
}

application {
    mainClass = "com.lemon.mcdevmanagermp.mcp.MainKt"
    applicationName = "mcdev-mcp"
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<CreateStartScripts>("startScripts") {
    classpath = files(tasks.jar, configurations.runtimeClasspath.get())
    doLast {
        windowsScript.writeText(
            Regex("^set CLASSPATH=.*$", RegexOption.MULTILINE).replace(
                windowsScript.readText(),
            ) { "set CLASSPATH=%APP_HOME%\\lib\\*" },
        )
        unixScript.writeText(
            Regex("^CLASSPATH=.*$", RegexOption.MULTILINE).replace(
                unixScript.readText(),
            ) { "CLASSPATH=${'$'}APP_HOME/lib/*" },
        )
    }
}

tasks.installDist {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.distZip {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.distTar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
