package com.sample.android.shared.composables

import android.text.format.DateUtils
import androidx.camera.core.ImageCapture
import androidx.camera.core.TorchState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sample.android.R
import com.sample.android.screens.recording.RecordingViewModel

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
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
                modifier = Modifier.size(60.dp),
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
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = ""
            )
        }
    )
}

@Composable
fun CameraPauseIconSmall(modifier: Modifier = Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
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
fun CameraPlayIconSmall(modifier: Modifier = Modifier, onTapped: () -> Unit) {
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
                modifier = Modifier.size(60.dp),
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
                modifier = Modifier.size(60.dp),
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
fun CameraTorchIcon(modifier: Modifier = Modifier, @TorchState.State torchState: Int, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            val drawable = if (torchState == TorchState.ON) {
                R.drawable.ic_flash_off
            } else {
                R.drawable.ic_flash_on
            }
            Image(
                painter = painterResource(id = drawable),
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
internal fun RequestPermission(message: Int, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onClick) {
            Text(text = stringResource(id = message))
        }
    }
}

@Composable
fun Timer(modifier: Modifier = Modifier, seconds: Int) {
    if (seconds > 0) {
        Box(modifier = Modifier.padding(vertical = 24.dp).then(modifier)) {
            Text(
                text = DateUtils.formatElapsedTime(seconds.toLong()),
                color = Color.White,
                modifier = Modifier
                    .background(color = Color.Red)
                    .padding(horizontal = 10.dp)
                    .then(modifier)
            )
        }

    }
}