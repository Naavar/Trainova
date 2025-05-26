plugins {
    id("com.android.application")
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
    implementation(libs.google.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Sign In
    implementation(libs.play.services.auth)

    // AndroidX y Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Otras
    implementation(libs.glide)
    implementation(libs.material.calendarview)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // LÍNEA AÑADIDA AQUÍ
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}