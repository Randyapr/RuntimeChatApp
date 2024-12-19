plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.runtimechatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.runtimechatapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    kotlinOptions {
        jvmTarget = "1.8"

    }
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.ui.database)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.core)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.espresso.idling.resource)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.ccp)
    implementation(libs.circleimageview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.glide)
    implementation(libs.firebase.ui.firestore)
    implementation(libs.glide.v4151)
    annotationProcessor(libs.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.libphonenumber)

    testImplementation (libs.mockito.kotlin)
    testImplementation (libs.truth)
    testImplementation(libs.mockito.inline)
    testImplementation (libs.mockito.android)
    testImplementation ("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation ("org.powermock:powermock-module-junit4:2.0.9")

    // Pengujian Instrumentasi (UI)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation (libs.androidx.espresso.intents.v361)
    androidTestImplementation (libs.androidx.rules)

    implementation("androidx.test:runner:1.5.2")
    androidTestImplementation("org.mockito:mockito-core:4.0.0")

    // Firebase (testingg)
    androidTestImplementation("com.google.firebase:firebase-bom:33.6.0")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // Coroutine
    testImplementation (libs.kotlinx.coroutines.test)
//    testImplementation (libs.robolectric)
    implementation("com.google.android.gms:play-services-location:21.0.1")


}