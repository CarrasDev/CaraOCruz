plugins {
    //id("com.android.application")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room) // Todo: quitar cuando migremos a firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.caraocruz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.caraocruz"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Solución al error de deprecación de kotlinOptions
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Estas líneas ahora coinciden exactamente con el [libraries] del TOML
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation(libs.play.services.location)
    implementation(libs.androidx.credentials.lib)
    implementation(libs.androidx.credentials.play.services.auth.lib)
    implementation(libs.googleid.lib)

    // Firebase
    //Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    // Firebase products
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Retrofit & Moshi
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava3)
    ksp(libs.room.compiler)

    // RxJava
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}