@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.recording

import android.Manifest
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraInfo
import androidx.camera.core.TorchState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sample.android.R
import com.sample.android.navigateTo
import com.sample.android.shared.PreviewState
import com.sample.android.shared.composables.*
import com.sample.android.shared.composables.RequestPermission
import com.sample.android.shared.utils.LocalVideoCaptureManager
import com.sample.android.shared.utils.VideoCaptureManager

@Composable
internal fun RecordingScreen(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    recordingViewModel: RecordingViewModel = viewModel(factory = factory),
    onShowMessage: (message: Int) -> Unit
) {
    val state by recordingViewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember(recordingViewModel) {
        object : VideoCaptureManager.Listener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                recordingViewModel.onEvent(RecordingViewModel.Event.CameraInitialized(cameraLensInfo))
            }
            override fun recordingPaused() {
                recordingViewModel.onEvent(RecordingViewModel.Event.RecordingPaused)
            }
            override fun onProgress(progress: Int) {
                recordingViewModel.onEvent(RecordingViewModel.Event.OnProgress(progress))
            }
            override fun recordingCompleted(outputUri: Uri) {
                recordingViewModel.onEvent(RecordingViewModel.Event.RecordingEnded(outputUri))
            }
            override fun onError(throwable: Throwable?) {
                recordingViewModel.onEvent(RecordingViewModel.Event.Error(throwable))
            }
        }
    }

    val captureManager = remember(recordingViewModel) {
        VideoCaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { this.listener = listener }
    }

    val permissions = remember { listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) }
    HandlePermissionsRequest(permissions = permissions, permissionsHandler = recordingViewModel.permissionsHandler)

    CompositionLocalProvider(LocalVideoCaptureManager provides captureManager) {
        VideoScreenContent(
            allPermissionsGranted = state.multiplePermissionsState?.allPermissionsGranted ?: false,
            cameraLens = state.lens,
            torchState = state.torchState,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            recordedLength = state.recordedLength,
            recordingStatus = state.recordingStatus,
            onEvent = recordingViewModel::onEvent
        )
    }

    LaunchedEffect(recordingViewModel) {
        recordingViewModel.effect.collect {
            when (it) {
                is RecordingViewModel.Effect.NavigateTo -> navController.navigateTo(it.route)
                is RecordingViewModel.Effect.ShowMessage -> onShowMessage(it.message)
                is RecordingViewModel.Effect.RecordVideo -> captureManager.startRecording(it.filePath)
                RecordingViewModel.Effect.PauseRecording -> captureManager.pauseRecording()
                RecordingViewModel.Effect.ResumeRecording -> captureManager.resumeRecording()
                RecordingViewModel.Effect.StopRecording -> captureManager.stopRecording()
            }
        }
    }
}

@Composable
private fun VideoScreenContent(
    allPermissionsGranted: Boolean,
    cameraLens: Int?,
    @TorchState.State torchState: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    recordedLength: Int,
    recordingStatus: RecordingViewModel.RecordingStatus,
    onEvent: (RecordingViewModel.Event) -> Unit
) {
    if (!allPermissionsGranted) {
        RequestPermission(message = R.string.request_permissions) {
            onEvent(RecordingViewModel.Event.PermissionRequired)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            cameraLens?.let {
                CameraPreview(lens = it, torchState = torchState)
                if (recordingStatus == RecordingViewModel.RecordingStatus.Idle) {
                    CaptureHeader(
                        modifier = Modifier.align(Alignment.TopStart),
                        showFlashIcon = hasFlashUnit,
                        torchState = torchState,
                        onFlashTapped = { onEvent(RecordingViewModel.Event.FlashTapped) },
                        onCloseTapped = { onEvent(RecordingViewModel.Event.CloseTapped) }
                    )
                }
                if (recordedLength > 0) {
                    Timer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        seconds = recordedLength
                    )
                }
                RecordFooter(
                    modifier = Modifier.align(Alignment.BottomStart),
                    recordingStatus = recordingStatus,
                    showFlipIcon = hasDualCamera,
                    onRecordTapped = { onEvent(RecordingViewModel.Event.RecordTapped) },
                    onPauseTapped = { onEvent(RecordingViewModel.Event.PauseTapped) },
                    onResumeTapped = { onEvent(RecordingViewModel.Event.ResumeTapped) },
                    onStopTapped = { onEvent(RecordingViewModel.Event.StopTapped) },
                    onFlipTapped = { onEvent(RecordingViewModel.Event.FlipTapped) }
                )
            }
        }
    }
}

@Composable
internal fun CaptureHeader(
    modifier: Modifier = Modifier,
    showFlashIcon: Boolean,
    torchState: Int,
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
            CameraTorchIcon(torchState = torchState, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}


@Composable
internal fun RecordFooter(
    modifier: Modifier = Modifier,
    recordingStatus: RecordingViewModel.RecordingStatus,
    showFlipIcon: Boolean,
    onRecordTapped: () -> Unit,
    onStopTapped: () -> Unit,
    onPauseTapped: () -> Unit,
    onResumeTapped: () -> Unit,
    onFlipTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .then(modifier)
    ) {
        when (recordingStatus) {
            RecordingViewModel.RecordingStatus.Idle -> {
                CameraRecordIcon(
                    modifier = Modifier.align(Alignment.Center),
                    onTapped = onRecordTapped
                )
            }
            RecordingViewModel.RecordingStatus.Paused -> {
                CameraStopIcon(modifier = Modifier.align(Alignment.Center), onTapped = onStopTapped)
                CameraPlayIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 150.dp), onTapped = onResumeTapped
                )
            }
            RecordingViewModel.RecordingStatus.InProgress -> {
                CameraStopIcon(modifier = Modifier.align(Alignment.Center), onTapped = onStopTapped)
                CameraPauseIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 140.dp), onTapped = onPauseTapped
                )
            }
        }

        if (showFlipIcon && recordingStatus == RecordingViewModel.RecordingStatus.Idle) {
            CameraFlipIcon(modifier = Modifier.align(Alignment.CenterEnd), onTapped = onFlipTapped)
        }
    }
}

@Composable
private fun CameraPreview(lens: Int, @TorchState.State torchState: Int) {
    val captureManager = LocalVideoCaptureManager.current
    BoxWithConstraints {
        AndroidView(
            factory = {
                captureManager.showPreview(
                    PreviewState(
                        cameraLens = lens,
                        torchState = torchState,
                        size = Size(this.minWidth.value.toInt(), this.maxHeight.value.toInt())
                    )
                )
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                captureManager.updatePreview(
                    PreviewState(cameraLens = lens, torchState = torchState),
                    it
                )
            }
        )
    }
}