plugins {
    // AGP 9 traz suporte Kotlin embutido — não aplicamos plugin Kotlin separado.
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    // Plugin de compilador do kotlinx.serialization (igual ao compose, roda sobre o Kotlin embutido).
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.jonathaxs.gymnutshell.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
