plugins {
    id("com.android.library")
    `maven-publish`
}

android {
    namespace = "io.github.vikie1.linkpreview"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.github.emreesen27:Android-Nested-Progress:v1.0.2")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

publishing {
    publications {
        register<MavenPublication> ("release") {
            groupId = "com.github.vikie1.linkpreview"
            artifactId = "link-preview"
            version = "1.0.0"

//            afterEvaluate {
//                from(components["release"])
//            }
        }
    }
}