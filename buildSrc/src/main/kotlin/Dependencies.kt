object Dep {
    val coreKtx by lazy { "androidx.core:core-ktx:${Versions.androidCoreKtx}" }
    val appCompat by lazy { "androidx.appcompat:appcompat:${Versions.appCompat}" }
    val materialDesign by lazy { "com.google.android.material:material:${Versions.material}" }
    val junit by lazy { "junit:junit:${Versions.jUnit}" }
    val androidJUnit by lazy { "androidx.test.ext:junit:${Versions.androidJUnit}" }
    val expresso by lazy { "androidx.test.espresso:espresso-core:${Versions.expresso}" }
    val cameraCore by lazy { "androidx.camera:camera-core:${Versions.cameraX}" }
    val camera2 by lazy { "androidx.camera:camera-camera2:${Versions.cameraX}" }
    val cameraLifecycle by lazy { "androidx.camera:camera-lifecycle:${Versions.cameraX}" }
    val cameraVideo by lazy { "androidx.camera:camera-video:${Versions.cameraX}" }
    val cameraView by lazy { "androidx.camera:camera-view:${Versions.cameraX}" }
    val androidxLifecycleKtx by lazy { "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidxLifecycleKtx}" }
    val timber by lazy { "com.jakewharton.timber:timber:${Versions.timber}"}
    val concurrentFuture by lazy { "androidx.concurrent:concurrent-futures-ktx:${Versions.concurrentFuture}" }
    val composeUI by lazy { "androidx.compose.ui:ui:${Versions.compose}" }
    val composeTooling by lazy { "androidx.compose.ui:ui-tooling:${Versions.compose}" }
    val composePermission by lazy { "com.google.accompanist:accompanist-permissions:${Versions.accompanist}" }
    val composeMaterial by lazy { "androidx.compose.material:material:${Versions.compose}" }
    val composeActivity by lazy { "androidx.activity:activity-compose:${Versions.composeActivity}" }
    val composeNavigation by lazy { "androidx.navigation:navigation-compose:${Versions.composeNavigation}" }
    val composeNavigationAnimation by lazy { "com.google.accompanist:accompanist-navigation-animation:${Versions.accompanist}" }
    val coil by lazy { "io.coil-kt:coil-compose:${Versions.coil}" }
}