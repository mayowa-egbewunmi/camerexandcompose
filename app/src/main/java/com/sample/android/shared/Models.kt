package com.sample.android.shared

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.TorchState

data class PreviewState(
    @ImageCapture.FlashMode val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    @TorchState.State val torchState: Int = TorchState.OFF,
    val cameraLens: Int = CameraSelector.LENS_FACING_BACK,
    val size: Size = Size(360, 480)
)