package com.sample.android.shared.composables

import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sample.android.R

@Composable
fun CameraCaptureIcon(modifier: Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.ic_capture),
                contentDescription = null
            )
        }
    )
}

@Composable
fun CameraPauseIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_pause),
                contentDescription = null
            )
        }
    )
}


@Composable
fun CameraPlayIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraRecordIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_record),
                contentDescription = null,
            )
        })
}

@Composable
fun CameraStopIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_stop),
                contentDescription = null,
            )
        }
    )
}

@Composable
fun CameraFlipIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_rotate),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraCloseIcon(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraFlashIcon(modifier: Modifier = Modifier, @ImageCapture.FlashMode flashMode: Int, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            val drawable = when(flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_off
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                else -> R.drawable.ic_flash_off
            }
            Image(
                painter = painterResource(id = drawable),
                contentDescription = ""
            )
        }
    )
}

@Composable
internal fun RequestPermission(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onClick) {
            Text(text = stringResource(id = R.string.request_permission))
        }
    }
}

@Composable
internal fun CaptureHeader(
    modifier: Modifier = Modifier,
    showFlashIcon: Boolean,
    flashMode: Int,
    onFlashTapped: () -> Unit,
    onCloseTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .then(modifier)
    ) {
        if (showFlashIcon) {
            CameraFlashIcon(flashMode = flashMode, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
internal fun CaptureFooter(
    modifier: Modifier = Modifier,
    showFlipIcon: Boolean,
    onCaptureTapped: () -> Unit,
    onFlipTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .then(modifier)
    ) {
        CameraCaptureIcon(modifier = Modifier.align(Alignment.Center), onTapped = onCaptureTapped)
        if (showFlipIcon) {
            CameraFlipIcon(modifier = Modifier.align(Alignment.CenterEnd), onTapped = onFlipTapped)
        }
    }
}

@Composable
internal fun RecordFooter(
    modifier: Modifier = Modifier,
    recording: Boolean,
    showFlipIcon: Boolean,
    onRecordTapped: () -> Unit,
    onStopTapped: () -> Unit,
    onFlipTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .then(modifier)
    ) {
        if (recording) {
            CameraStopIcon(modifier = Modifier.align(Alignment.Center), onTapped = onStopTapped)
        } else {
            CameraRecordIcon(modifier = Modifier.align(Alignment.Center), onTapped = onRecordTapped)
        }
        if (showFlipIcon) {
            CameraFlipIcon(modifier = Modifier.align(Alignment.CenterEnd), onTapped = onFlipTapped)
        }
    }
}