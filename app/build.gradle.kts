plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.4.0"
    id("com.google.gms.google-services")
}
val kotlinVersion = rootProject.extra["kotlinVersion"]
val composeVersion = rootProject.extra["composeVersion"]
val getProps = rootProject.extra["getProps"] as (String) -> String
val generateBuildConfig =
    rootProject.extra["generateBuildConfig"] as (File?, com.android.build.gradle.internal.dsl.BuildType) -> String

android {
    compileSdkVersion(29)
    buildToolsVersion("30.0.1")

    defaultConfig {
        applicationId = "com.zelgius.openplayer"
        minSdkVersion(26)
        targetSdkVersion(29)
        versionCode(1)
        versionName("1.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //buildConfigField("String", "KEY", getProps("key"))
            generateBuildConfig(null, this)

        }

        getByName("debug") {
            isMinifyEnabled = false
            //buildConfigField("String", "KEY", getProps("key"))
            generateBuildConfig(null, this)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "$composeVersion"
    }

}

dependencies {

    //implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")

    //Compose
    implementation ("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.ui:ui-tooling:$composeVersion")

    //Firebase
    implementation("com.google.firebase:firebase-auth:19.4.0")
    implementation("com.google.firebase:firebase-firestore:21.6.0")

    //KTX
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:21.6.0")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.core:core-ktx:1.5.0-alpha03")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")

    //External
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.beust:klaxon:5.4") // JVM dependency
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.thetransactioncompany:jsonrpc2-client:1.16.4")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.mockito")
    }
    testCompile("org.mockito:mockito-core:3.5.13")
    testCompile("org.mockito:mockito-inline:3.5.13")
    androidTestCompile("org.mockito:mockito-android:3.5.13")
    androidTestCompile("androidx.test.ext:junit:1.1.2")
    androidTestCompile("androidx.test.espresso:espresso-core:3.3.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
