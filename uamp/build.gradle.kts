/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion (29)

    defaultConfig {
        versionCode( 1)
        versionName ("1.0")

        minSdkVersion (26)
        targetSdkVersion (29)

        testInstrumentationRunner ("androidx.test.runner.AndroidJUnitRunner")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation ("androidx.media:media:1.2.0")

    implementation( "com.google.code.gson:gson:2.8.6")

    // ExoPlayer dependencies

    // This allows UAMP to utilize a local version of ExoPlayer, which is particularly
    // useful for extending the MediaSession extension, as well as for testing and
    // customization. If the ":exoplayer-library-core" project is included, we assume
    // the others are included as well.
    if (findProject(":exoplayer-library-core") != null) {
        implementation ( project(":exoplayer-library-core"))
        implementation ( project(":exoplayer-library-ui"))
        implementation ( project(":exoplayer-extension-mediasession"))
        implementation ( project(":exoplayer-extension-cast"))
    } else {
        implementation ( "com.google.android.exoplayer:exoplayer-core:2.12.0")
        implementation ( "com.google.android.exoplayer:exoplayer-ui:2.12.0")
        implementation ( "com.google.android.exoplayer:extension-mediasession:2.12.0")
        implementation ( "com.google.android.exoplayer:extension-cast:2.12.0")
    }

    // Glide dependencies
    kapt("com.github.bumptech.glide:compiler:4.11.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.11.0")

}
