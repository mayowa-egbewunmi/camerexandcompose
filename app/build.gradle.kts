plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = ConfigData.compileSdkVersion

    defaultConfig {
        applicationId = "com.mayowa.cameraxandcompose"
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName

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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }
}

dependencies {

    implementation(Dep.coreKtx)
    implementation(Dep.appCompat)
    implementation(Dep.materialDesign)
    implementation(Dep.cameraCore)
    implementation(Dep.camera2)
    implementation(Dep.cameraVideo)
    implementation(Dep.cameraLifecycle)
    implementation(Dep.cameraView)
    implementation(Dep.androidxLifecycleKtx)
    implementation(Dep.timber)
    implementation(Dep.concurrentFuture)
    implementation(Dep.composeUI)
    implementation(Dep.composeTooling)
    implementation(Dep.composePermission)
    implementation(Dep.composeMaterial)
    implementation(Dep.composeActivity)
    implementation(Dep.composeNavigation)
    implementation(Dep.composeNavigationAnimation)
    implementation(Dep.coil)

    testImplementation(Dep.junit)
    androidTestImplementation(Dep.androidJUnit)
    androidTestImplementation(Dep.expresso)

}