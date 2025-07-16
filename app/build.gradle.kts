plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.collega"
    compileSdk = 35
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.collega"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.car.ui.lib)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.github.gayanvoice:android-animations-kotlin:1.0.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    //image picker
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    //Picasso image loader
    implementation ("com.squareup.picasso:picasso:2.8")

    // File picker
    implementation ("io.github.chochanaresh:filepicker:0.1.9")
    // exoPlayer video loader
    implementation("com.google.android.exoplayer:exoplayer:2.19.0") // Add this in build.gradle
    // volley
    implementation("com.android.volley:volley:1.2.1")
    // Google credential
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    //zoomable photoview
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation ("jp.wasabeef:blurry:4.0.1")




}