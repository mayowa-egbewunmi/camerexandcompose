package com.sample.android.shared.composables

import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sample.android.R

@Composable
fun CameraCaptureIcon(modifier: Modifier, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            Image(
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
fun CameraFlashIcon(modifier: Modifier = Modifier, flashEnabled: Boolean, onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier
            .then(modifier),
        onClick = { onTapped() },
        content = {
            val flashDrawable = if (flashEnabled) {
                R.drawable.ic_flash_on
            } else {
                R.drawable.ic_flash_off
            }
            Image(
                painter = painterResource(id = flashDrawable),
                contentDescription = ""
            )
        }
    )
}