plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)

    id("kotlin-kapt") // Pour Room
}

android {
    namespace = "com.example.projetmediassist"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projetmediassist"
        minSdk = 24
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.play.services.maps)
    kapt("androidx.room:room-compiler:2.6.1")

    // Maps
    implementation(libs.play.services.maps)

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Optionnel : Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp pour requêtes HTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // org.json pour parser le JSON
    implementation("org.json:json:20210307")

    // Firebase Auth + Google Sign-In
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // 🔁 Google Calendar API + OAuth + JSON
    implementation("com.google.api-client:google-api-client-android:1.35.0") {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }

    implementation("com.google.http-client:google-http-client-android:1.43.3")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0")

    implementation("com.google.guava:guava:31.1-jre")

    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
