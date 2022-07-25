package com.sample.android.screens.recording

import android.Manifest
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraInfo
import androidx.camera.core.TorchState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sample.android.navigateTo
import com.sample.android.shared.PreviewState
import com.sample.android.shared.composables.*
import com.sample.android.shared.composables.CaptureHeader
import com.sample.android.shared.composables.RequestPermission
import com.sample.android.shared.utils.LocalVideoCaptureManager
import com.sample.android.shared.utils.RecordingManager
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RecordingScreen(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    recordingViewModel: RecordingViewModel = viewModel(factory = factory),
    onShowMessage: (message: Int) -> Unit
) {
    val state by recordingViewModel.state.collectAsState()
    val permissionHandler =
        AccompanistPermissionHandler(permission = Manifest.permission.CAMERA)
    val permissionHandlerState by permissionHandler.state.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember {
        object : RecordingManager.RecordingListener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                recordingViewModel.onEvent(RecordingViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun recordingStarted() {
                recordingViewModel.onEvent(RecordingViewModel.Event.RecordingStarted)
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

    val captureManager = remember {
        RecordingManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { recordingListener = listener }
    }

    LaunchedEffect(recordingViewModel) {
        recordingViewModel.effect.collect {
            when (it) {
                is RecordingViewModel.Effect.NavigateTo -> navController.navigateTo(it.route)
                is RecordingViewModel.Effect.RecordVideo -> captureManager.startRecording(it.filePath)
                is RecordingViewModel.Effect.ShowMessage -> onShowMessage(it.message)
                RecordingViewModel.Effect.PauseRecording -> captureManager.pauseRecording()
                RecordingViewModel.Effect.ResumeRecording -> captureManager.resumeRecording()
                RecordingViewModel.Effect.StopRecording -> captureManager.stopRecording()
            }
        }
    }

    CompositionLocalProvider(LocalVideoCaptureManager provides captureManager) {
        VideoScreenContent(
            hasPermission = permissionHandlerState.permissionState?.hasPermission ?: false,
            cameraLens = state.lens,
            torchState = state.torchState,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            recordedLength = state.recordedLength,
            recordingStatus = state.recordingStatus,
            onEvent = recordingViewModel::onEvent,
            onPermissionEvent = permissionHandler::onEvent
        )
    }
}

@Composable
private fun VideoScreenContent(
    hasPermission: Boolean,
    cameraLens: Int?,
    @TorchState.State torchState: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    recordedLength: Int,
    recordingStatus: RecordingViewModel.RecordingStatus,
    onEvent: (RecordingViewModel.Event) -> Unit,
    onPermissionEvent: (PermissionHandler.Event) -> Unit
) {
    if (!hasPermission) {
        RequestPermission(onClick = { onPermissionEvent(PermissionHandler.Event.PermissionRequired) })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            cameraLens?.let {
                CameraPreview(
                    lens = it,
                    torchState = torchState
                )
                if (recordingStatus == RecordingViewModel.RecordingStatus.Idle) {
                    CaptureHeader(
                        modifier = Modifier.align(Alignment.TopStart),
                        showFlashIcon = hasFlashUnit,
                        flashMode = torchState,
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