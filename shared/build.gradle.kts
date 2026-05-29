import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
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
    
    androidLibrary {
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
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val appName = "MCDevManager"
val appVersion = libs.versions.versions.name.get()

tasks.register<Zip>("packagePortable") {
    group = "distribution"
    description = "Create a portable zip archive (免安装版)"
    dependsOn("createDistributable")

    from(layout.buildDirectory.dir("compose/binaries/main/app"))
    into(appName)
    archiveFileName.set("$appName-$appVersion-portable.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
}

tasks.register("packageInstaller") {
    group = "distribution"
    description = "Create an installer (安装版)"
    dependsOn("packageExe")

    doLast {
        println("Installer created at: ${layout.buildDirectory.dir("compose/binaries/main/exe").get().asFile.absolutePath}")
    }
}

// 强制指定打包使用的 JDK 路径，解决 Android Studio JBR 缺少 jpackage 的问题
tasks.withType<AbstractJPackageTask>().configureEach {
    val targetJdk = File("C:/Users/sqn_android/.jdks/ms-17.0.15")
    if (targetJdk.exists()) {
        javaHome.set(targetJdk.absolutePath)
    }
}