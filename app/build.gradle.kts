import java.util.Properties

val properties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}


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

        val authKey = properties.getProperty("AUTH_KEY_SECRET", "")
        resValue("string", "auth_key_secret", authKey)

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
    buildFeatures {
        buildConfig = true
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

    // Retrofit para llamadas a la API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Otras
    implementation(libs.glide)
    implementation(libs.material.calendarview)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation (libs.robolectric)
    testImplementation (libs.mockito.mockito.core)
    testImplementation (libs.mockito.inline)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}