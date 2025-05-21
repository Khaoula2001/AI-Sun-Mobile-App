plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.emsi.aisun"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.emsi.aisun"
        minSdk = 31
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Location
    implementation ("com.google.android.gms:play-services-location:21.2.0")

    // Chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Permissions
    implementation ("com.karumi:dexter:6.2.3")

    implementation ("com.google.android.libraries.places:places:3.3.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.github.MKergall:osmbonuspack:6.9.0") // Pour OpenStreetMap
}