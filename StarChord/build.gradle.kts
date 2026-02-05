plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "opensource.starchord"
    compileSdk = 34

    defaultConfig {
        applicationId = "opensource.starchord.Play"
        minSdk = 21  // SDK 21 is the min version that supports transparent rescaling of a Vector Drawable (imported from a SVG in Vector Studio inside Android Studio)
        targetSdk = 34
        versionCode = 10201    // MUST be an Integer (e.g., 1, 2, 100, 10101 - use 10101 to mimic semantic versioning by using 0 as a dot)
        versionName = "1.2.1"  // CAN be a String (e.g., "1.1.1", "2.0-beta" - can follow semantic versioning)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        // Disable lint errors for this legacy project
        abortOnError = false
        checkReleaseBuilds = false
        // Ignore specific lint issues common in legacy projects
        disable.addAll(listOf("LongLogTag", "ObsoleteSdkInt", "OldTargetApi"))
    }
}

dependencies {

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}
