import java.util.*

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ru.shelq.nework"
    compileSdk = 34
    defaultConfig {
        applicationId = "ru.shelq.nework"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"http://94.228.125.136:8080\"")

        val properties = Properties().apply {
            load(rootProject.file("apikey.properties").reader())
        }
        val apiKey = properties.getProperty("API_KEY") ?: ""
        buildConfigField(
            "String",
            "API_KEY",
            apiKey
        )
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["userCleartextTraffic"] = false


        }
        debug {
            manifestPlaceholders["userCleartextTraffic"] = true

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}


dependencies {
    implementation(libs.androidx.navigation.fragment.ktx.v280)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.material.v150)
    implementation(libs.circleimageview)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android.v252)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.glide)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.room.paging)


}
kapt {
    correctErrorTypes = true
}