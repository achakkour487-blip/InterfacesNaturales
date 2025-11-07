// build.gradle.kts


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // <-- USA ESTA LÍNEA EN SU LUGAR
}


android {
    namespace = "com.example.recocnocimientopostural"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.recocnocimientopostural"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }


    buildTypes {
        // En Kotlin DSL, se usa getByName para configurar tipos existentes
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }


    buildFeatures {
        viewBinding = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    kotlinOptions {
        jvmTarget = "17"
    }
}


// ⚠️ El repositorio 'maven("https://jitpack.io")' NO va aquí.
// Ya está correctamente definido en settings.gradle.kts.


dependencies {
    // ML Kit Pose Detection (accurate)
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")

    // ARCore + SceneView (asegurate de que SceneView esté en una versión compatible)
    implementation("com.google.ar:core:1.47.0")  // Puede ser que haya una versión más nueva
    implementation("io.github.sceneview:sceneview:2.3.0") // Asegúrate de que esta es la última estable

    // CameraX (no olvides CameraX para compatibilidad con AR)
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // AndroidX y Material (mantener actualizadas estas dependencias)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity:1.9.3")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
