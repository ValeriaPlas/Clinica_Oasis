plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services") // 🔥 Plugin necesario para Firebase
}

android {
    namespace = "com.example.clinicaoasis"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.clinicaoasis"
        minSdk = 29
        targetSdk = 34
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
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Firebase Core
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // 🔥 Mensajería (FCM) — NECESARIA
    implementation("com.google.firebase:firebase-messaging")

    // Librerías Android
    implementation(libs.appcompat)
    implementation(libs.material)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
