import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidapplication)
    alias(libs.plugins.legacykapt)
    alias(libs.plugins.kotlincompose)
}

android {
    namespace = "com.svksricharan.animeapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.svksricharan.animeapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("Boolean", "SHOW_IMAGES", "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidxktx)
    implementation(libs.androidxactivity)
    implementation(platform(libs.composebom))
    implementation(libs.composeui)
    implementation(libs.composeuigraphics)
    implementation(libs.composetoolingpreview)
    implementation(libs.composematerial3)
    implementation(libs.materialiconsextended)

    implementation(libs.coilcompose)

    implementation(libs.retrofit)
    implementation(libs.convertergson)
    implementation(libs.okhttplogging)
    implementation(libs.gson)

    implementation(libs.lifecycleviewmodelcompose)
    implementation(libs.lifecycleruntimecompose)

    implementation(libs.navigationcompose)

    implementation(libs.roomruntime)
    implementation(libs.roomktx)
    kapt(libs.roomcompiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.archcoretesting)
    testImplementation(libs.coroutinestest)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.composebom))
    androidTestImplementation(libs.composeui)
    androidTestImplementation(libs.junitext)
    androidTestImplementation(libs.espressocore)
    androidTestImplementation(libs.uitestjunit4)
    debugImplementation(libs.uitooling)
    debugImplementation(libs.uitestmanifest)
}
