plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
     id ("kotlin-kapt")
    id ("com.google.devtools.ksp")
}

android {
    namespace = "com.ftg.carrepo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ftg.carrepo"
        minSdk = 26
        targetSdk = 34
        versionCode = 56
        versionName = "v1.0.22"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("androidx.work:work-runtime:2.8.1")
    implementation ("com.github.ybq:Android-SpinKit:1.4.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.github.dangiashish:StyledCardView:1.0.0")
    implementation ("com.google.dagger:hilt-android:2.48.1")
    ksp ("com.google.dagger:hilt-android-compiler:2.48.1")

    implementation ("androidx.room:room-runtime:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")
    ksp ("androidx.room:room-compiler:2.5.2")

    val paging_version = "3.2.1"

    implementation("androidx.paging:paging-runtime:$paging_version")

    // alternatively - without Android dependencies for tests
    testImplementation("androidx.paging:paging-common:$paging_version")

    // optional - RxJava2 support
    implementation("androidx.paging:paging-rxjava2:$paging_version")
    implementation("androidx.paging:paging-rxjava3:$paging_version")
    implementation("androidx.paging:paging-guava:$paging_version")
    implementation("androidx.paging:paging-compose:3.3.0-alpha04")
}