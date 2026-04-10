import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "gor.alaverdyan.myapplication"
    compileSdk = 36 

    defaultConfig {
        applicationId = "gor.alaverdyan.myapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")

        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        val openRouterKey = properties.getProperty("OPENROUTER_API_KEY") ?: ""
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20231013")
    implementation(libs.activity)
}
