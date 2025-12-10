plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Aggiungi questa riga per il plugin KAPT
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.budgify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.budgify"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.0")

    implementation("androidx.room:room-runtime:2.7.1")
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    annotationProcessor("androidx.room:room-compiler:2.7.1") // For Java users
    // Per utenti Kotlin con KAPT
    kapt("androidx.room:room-compiler:2.7.1")
    // Moshi annotation processor
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    // Moshi Kotlin runtime
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // Per utenti Kotlin con KSP
    //ksp("androidx.room:room-compiler:$room_version")

    // Optional: support for Kotlin Coroutines and Flow
    implementation("androidx.room:room-ktx:2.7.1")

    implementation("androidx.security:security-crypto:1.1.0-alpha07")

    // Networking with Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}