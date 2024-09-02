plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.serialization") version "1.7.0"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lemon.mcdevmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lemon.mcdevmanager"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "0.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // r8混淆
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 只打包arm64-v8a
            ndk {
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
        }
        debug {
            ndk {
                abiFilters.clear() // 清除abiFilters，打包所有架构
            }
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../key_store.jks")
            storePassword = "bitterlemon"
            keyAlias = "lemon"
            keyPassword = "bitterlemon"
        }
        getByName("debug") {
            storeFile = file("../key_store.jks")
            storePassword = "bitterlemon"
            keyAlias = "lemon"
            keyPassword = "bitterlemon"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL3.0}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(project(":mvi-core"))
    implementation(project(":logger")) { exclude("com.google.guava") }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // room
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // permission
    implementation(libs.accompanist.permissions)

    // sm4 加密
    implementation(libs.bcprov.jdk15on)

    // coil
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // chart
    implementation(libs.compose.charts)

    // compose extend view
    implementation(libs.composeviews)
    // compose wheel picker
    implementation(libs.compose.wheel.picker)
}