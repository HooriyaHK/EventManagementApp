plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.goldencarrot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.goldencarrot"
        minSdk = 32
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
        tasks.withType<Test>{
            useJUnitPlatform()
        }
    }
}

dependencies {

        implementation("com.google.firebase:firebase-auth:21.0.5")  // Use the latest version
        implementation("com.google.firebase:firebase-firestore:24.2.0")
        implementation("com.google.firebase:firebase-storage:20.1.0")
        implementation("com.google.android.gms:play-services-auth:20.3.0")
        implementation("com.google.android.gms:play-services-base:18.0.1")
       implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.espresso.intents)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.core)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("org.mockito:mockito-core:4.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")


    // junit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.1")

    //UI tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // QR code
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // IMAGE
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.yalantis:ucrop:2.2.9")

    // GEOLOCATION
    implementation ("org.osmdroid:osmdroid-android:6.1.14")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.14")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")




}
