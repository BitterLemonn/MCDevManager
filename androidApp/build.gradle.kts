import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

fun loadKeystoreProps(): Properties? {
    // 优先从项目根目录的 keystore.properties 文件读取
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        return Properties().apply { load(propsFile.inputStream()) }
    }
    // 回退到环境变量（CI 环境使用）
    val envStore = System.getenv("KEYSTORE_FILE")
    if (envStore != null) {
        return Properties().apply {
            this["storeFile"] = envStore
            this["storePassword"] = System.getenv("KEYSTORE_PASSWORD") ?: ""
            this["keyAlias"] = System.getenv("KEY_ALIAS") ?: ""
            this["keyPassword"] = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    return null
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.slf4j.simple)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.lemon.mcdevmanagermp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lemon.mcdevmanagermp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = libs.versions.versions.name.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            val props = loadKeystoreProps()
            if (props != null) {
                storeFile = rootProject.file(props["storeFile"] as String)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            }
        }
    }

    buildTypes {
        getByName("debug") {
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile != null) {
                signingConfig = releaseConfig
            }
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile != null) {
                signingConfig = releaseConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}