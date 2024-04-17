@Suppress("DSL_SCOPE_VIOLATION") /* TODO: Remove once KTIJ-19369 is fixed */

plugins {


    id("com.android.application")
    id("com.google.gms.google-services")
    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin)


}


android {

    namespace = "com.example.rey"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rey"
        minSdk = 33

        34.also { targetSdk = it }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.firebase:geofire-android:3.2.0")
    implementation("com.firebase:geofire-android-common:3.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.firestore)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)



}


