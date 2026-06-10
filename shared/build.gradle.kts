import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

val generateVersionFile by tasks.registering {
    val version = libs.versions.versions.name.get()
    val outputDir = layout.buildDirectory.dir("generated/version/kotlin")
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().asFile.resolve("com/lemon/mcdevmanagermp/BuiltInVersion.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package com.lemon.mcdevmanagermp
            |
            |object BuiltInVersion {
            |    const val VERSION = "$version"
            |}
            """.trimMargin()
        )
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir(generateVersionFile.map { it.outputs.files.singleFile })
}

composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_compiler_config.conf"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    android {
       namespace = "com.lemon.mcdevmanagermp.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // material icons
            implementation(libs.compose.material.icons.extended)
            // kotlin Serialization
            implementation(libs.kotlinx.serialization.json)
            // ktorfit
            implementation(libs.ktorfit)
            // Ktor序列化
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.logging)
            // logging
            implementation(libs.logging)
            // okio
            implementation(libs.okio)
            // datetime
            implementation(libs.kotlinx.datetime)
            // bignum
            implementation(libs.bignum)
            // navigation
            implementation(libs.navigation.compose)
            // window size class
            implementation(libs.material3.window.size)
            // room
            implementation(libs.room)
            implementation(libs.sqlite.bundled)
            // sketch
            implementation(libs.sketch.http)
            implementation(libs.sketch.compose)
            implementation(libs.sketch.compose.resources)
            implementation(libs.sketch.webp)
            // vico
            implementation(libs.vico.compose.m3)
            // calf permissions
            implementation(libs.calf.permissions)
            implementation(libs.calf.permissions.notification)
            // crypto
            implementation(libs.crypto.core)
            implementation(libs.crypto.provider)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.skiko") {
                useVersion("0.144.6")
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}