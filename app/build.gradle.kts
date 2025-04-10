plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.navar.trainova"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.navar.trainova"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.analytics)

    // Google Sign In
    implementation(libs.play.services.auth)

    // AndroidX y Material
    implementation(libs.compose.ui)
    implementation(libs.appcompat)
    implementation(libs.material.v1110)
    implementation(libs.activity.v182)
    implementation(libs.constraintlayout.v214)

    // Glide para cargar imágenes
    implementation(libs.glide)
    //Calendario
    implementation(libs.material.calendarview)

      // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core)
}

configurations.all {
    resolutionStrategy.force("androidx.core:core:1.12.0")  // Usa la última versión
}