plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
}

android {
    namespace = "com.svksricharan.animeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.svksricharan.animeapp"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

    // Image loading
    implementation(libs.coilcompose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.convertergson)
    implementation(libs.okhttplogging)
    implementation(libs.gson)

    // Lifecycle & ViewModel
    implementation(libs.lifecycleviewmodelcompose)
    implementation(libs.lifecycleruntimecompose)

    // Navigation
    implementation(libs.navigationcompose)

    // Room
    implementation(libs.roomruntime)
    implementation(libs.roomktx)
    kapt(libs.roomcompiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.archcoretesting)
    testImplementation(libs.coroutinestest)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.junitext)
    androidTestImplementation(libs.espressocore)
    androidTestImplementation(libs.uitestjunit4)
    debugImplementation(libs.uitooling)
    debugImplementation(libs.uitestmanifest)
}
