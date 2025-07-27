plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.0.0" // (본인의 Kotlin 버전에 맞게 조정)
}

android {
    namespace = "com.lion.wandertrip"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("C:/Users/tnsgh/keystores.jks")
            storePassword = "c1241316"
            keyAlias = "key0"
            keyPassword = "c1241316"
        }
    }


    defaultConfig {
        applicationId = "com.lion.wandertrip"
        minSdk = 26
        targetSdk = 35
        versionCode = 21
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
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
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
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
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.compose.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.1")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-functions-ktx") // functions
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation ("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("com.kakao.sdk:v2-user:2.20.1")

    implementation("com.github.skydoves:landscapist-glide:1.4.7")
    implementation ("com.github.bumptech.glide:compose:1.0.0-alpha.3")
    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.google.maps.android:maps-compose:2.14.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0") // 지도
    implementation("com.google.android.gms:play-services-location:21.0.1") // 지도
    implementation("com.google.maps.android:maps-compose:2.9.0") // 지도
    implementation("com.google.accompanist:accompanist-permissions:0.30.0") // 권한 관리 의존성

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // lifecycleScope

    // Moshi
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")


    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // ✅ JSON 직렬화 라이브러리 추가
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // ✅ OkHttp 추가
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0") // ✅ Retrofit 변환기 추가

    // Lottie 애니메이션
    implementation("com.airbnb.android:lottie-compose:5.2.0")

    // Shimmer 라이브러리
    implementation("com.valentinilk.shimmer:compose-shimmer:1.0.3")// 버전 예시

    // LazyColumn 순서 변경
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    implementation("org.mindrot:jbcrypt:0.4") // 유저 정보 해쉬 암호화


}