@file:OptIn(ExperimentalPermissionsApi::class)

import android.Manifest
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Box
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
import com.sample.android.screens.recording.RecordingViewModel
import com.sample.android.shared.PreviewState
import com.sample.android.shared.composables.*
import com.sample.android.shared.composables.CaptureHeader
import com.sample.android.shared.composables.RequestPermission
import com.sample.android.shared.utils.LocalVideoCaptureManager
import com.sample.android.shared.utils.VideoCaptureManager
import kotlinx.coroutines.flow.collect

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
        object : VideoCaptureManager.RecordingListener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                recordingViewModel.onEvent(RecordingViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun onSuccess(imageResult: ImageCapture.OutputFileResults) {
                recordingViewModel.onEvent(RecordingViewModel.Event.ImageCaptured(imageResult))
            }

            override fun onError(exception: Exception) {
                recordingViewModel.onEvent(RecordingViewModel.Event.Error(exception))
            }
        }
    }

    val captureManager = remember {
        VideoCaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { recordingListener = listener }
    }

    LaunchedEffect(recordingViewModel) {
        recordingViewModel.effect.collect {
            when (it) {
                is RecordingViewModel.Effect.NavigateTo -> navController.navigateTo(it.destination.route)
                is RecordingViewModel.Effect.CaptureImage -> captureManager.startRecording(it.filePath)
                is RecordingViewModel.Effect.ShowMessage -> onShowMessage(it.message)
            }
        }
    }

    CompositionLocalProvider(LocalVideoCaptureManager provides captureManager) {
        VideoScreenContent(
            hasPermission = permissionHandlerState.permissionState?.hasPermission ?: false,
            cameraLens = state.lens,
            flashMode = state.flashMode,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            onEvent = recordingViewModel::onEvent,
            onPermissionEvent = permissionHandler::onEvent
        )
    }
}

@Composable
private fun VideoScreenContent(
    hasPermission: Boolean,
    cameraLens: Int?,
    @ImageCapture.FlashMode flashMode: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
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
                    flashMode = flashMode
                )
                CaptureHeader(
                    modifier = Modifier.align(Alignment.TopStart),
                    showFlashIcon = hasFlashUnit,
                    flashMode = flashMode,
                    onFlashTapped = { onEvent(RecordingViewModel.Event.FlashTapped) },
                    onCloseTapped = { onEvent(RecordingViewModel.Event.CloseTapped) }
                )
                RecordFooter(
                    modifier = Modifier.align(Alignment.BottomStart),
                    recording = false,
                    showFlipIcon = hasDualCamera,
                    onRecordTapped = { onEvent(RecordingViewModel.Event.CaptureTapped) },
                    onStopTapped = {},
                    onFlipTapped = { onEvent(RecordingViewModel.Event.FlipTapped) }
                )
            }
        }
    }
}


@Composable
private fun CameraPreview(lens: Int, @ImageCapture.FlashMode flashMode: Int) {
    val captureManager = LocalVideoCaptureManager.current
    Box {
        AndroidView(
            factory = {
                captureManager.showPreview(
                    PreviewState(
                        cameraLens = lens,
                        flashMode = flashMode
                    )
                )
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                captureManager.updatePreview(
                    PreviewState(
                        cameraLens = lens,
                        flashMode = flashMode
                    ), it
                )
            }
        )
    }
}