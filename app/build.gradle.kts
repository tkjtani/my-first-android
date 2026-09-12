import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) FileInputStream(f).use { fis -> load(fis) }
}
fun releaseProp(name: String): String? =
    (project.findProperty(name) as String?)
        ?: localProps.getProperty(name)
        ?: System.getenv(name)

android {
    namespace = "com.example.my_first_android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.my_first_android"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val ksPath = releaseProp("RELEASE_STORE_FILE")
            val ksPass = releaseProp("RELEASE_STORE_PASSWORD")
            val ksAlias = releaseProp("RELEASE_KEY_ALIAS")
            val ksKeyPass = releaseProp("RELEASE_KEY_PASSWORD")
            if (!ksPath.isNullOrBlank()) {
                storeFile = file(ksPath)
                storePassword = ksPass
                keyAlias = ksAlias
                keyPassword = ksKeyPass
            }
        }
    }
    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Pakai keystore release jika tersedia di local.properties / env,
            // kalau tidak ada fallback ke debug agar assembleRelease tetap jalan saat dev.
            val ksPath = releaseProp("RELEASE_STORE_FILE")
            if (!ksPath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}