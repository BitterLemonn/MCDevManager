import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    // logback
    implementation(libs.logback.classic)
}

val appName = "MCDevManager"
val appVersion = libs.versions.versions.name.get()

compose.desktop {
    application {
        mainClass = "com.lemon.mcdevmanagermp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appName
            packageVersion = appVersion

            windows {
                menuGroup = appName
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("icons/icon-windows.ico"))
            }

            macOS {
                iconFile.set(project.file("icons/icon-mac.icns"))
            }

            linux {
                iconFile.set(project.file("icons/icon-linux.png"))
            }
        }
    }
}

tasks.register<Zip>("packagePortable") {
    group = "released"
    description = "Create a portable zip archive (免安装版)"
    dependsOn("createDistributable")

    from(layout.buildDirectory.dir("compose/binaries/main/app/${appName}"))
    into(appName)
    archiveFileName.set("$appName-$appVersion-portable.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
}

tasks.register("packageInstaller") {
    group = "released"
    description = "Create an MSI installer (安装版)"
    dependsOn("packageMsi")

    doLast {
        val msiDir = layout.buildDirectory.dir("compose/binaries/main/msi").get().asFile
        val msiFile = msiDir.walkTopDown().find { it.extension == "msi" }
        if (msiFile != null) {
            println("Installer created: ${msiFile.absolutePath}")
        } else {
            println("Installer directory: ${msiDir.absolutePath}")
        }
    }
}

//// 强制指定打包使用的 JDK 路径，解决 Android Studio JBR 缺少 jpackage 的问题
//tasks.withType<AbstractJPackageTask>().configureEach {
//    val targetJdk = File("C:/Users/sqn_android/.jdks/ms-17.0.15")
//    if (targetJdk.exists()) {
//        javaHome.set(targetJdk.absolutePath)
//    }
//}
