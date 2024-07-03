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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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

    // logger
    implementation(libs.logger)

    // sm4
    implementation(libs.bcprov.jdk15on)

    // j2v8 js引擎
    implementation ("com.eclipsesource.j2v8:j2v8:6.2.0@aar")
    
    // coil
    implementation (libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // chart
    implementation (libs.mpandroidchart)

    // compose extend view
    implementation(libs.composeviews)
}