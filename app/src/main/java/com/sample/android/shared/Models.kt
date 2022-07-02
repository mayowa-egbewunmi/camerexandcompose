package com.sample.android.shared

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture

data class PreviewState(
    @ImageCapture.FlashMode val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val cameraLens: Int = CameraSelector.LENS_FACING_BACK,
    val size: Size = Size(360, 480)
)

enum class PermissionAction {
    REQUEST_PERMISSION, SHOW_RATIONALE, SHOW_NEVER_ASK_AGAIN, NO_ACTION
}