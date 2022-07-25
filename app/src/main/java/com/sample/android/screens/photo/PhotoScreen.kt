@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.photo

import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.*
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
import com.sample.android.shared.utils.LocalCaptureManager
import com.sample.android.shared.utils.CaptureManager
import kotlinx.coroutines.flow.collect

@Composable
internal fun PhotoScreen(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    photoViewModel: PhotoViewModel = viewModel(factory = factory),
    onShowMessage: (message: Int) -> Unit
) {
    val state by photoViewModel.state.collectAsState()
    val permissionHandler =
        AccompanistPermissionHandler(permission = android.Manifest.permission.CAMERA)
    val permissionHandlerState by permissionHandler.state.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember {
        object : CaptureManager.PhotoListener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                photoViewModel.onEvent(PhotoViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun onSuccess(imageResult: ImageCapture.OutputFileResults) {
                photoViewModel.onEvent(PhotoViewModel.Event.ImageCaptured(imageResult))
            }

            override fun onError(exception: Exception) {
                photoViewModel.onEvent(PhotoViewModel.Event.Error(exception))
            }
        }
    }

    val captureManager = remember {
        CaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { photoListener = listener }
    }

    LaunchedEffect(photoViewModel) {
        photoViewModel.effect.collect {
            when (it) {
                is PhotoViewModel.Effect.NavigateTo -> navController.navigateTo(it.destination.route)
                is PhotoViewModel.Effect.CaptureImage -> captureManager.takePhoto(it.filePath)
                is PhotoViewModel.Effect.ShowMessage -> onShowMessage(it.message)
            }
        }
    }

    CompositionLocalProvider(LocalCaptureManager provides captureManager) {
        PhotoScreenContent(
            hasPermission = permissionHandlerState.permissionState?.hasPermission ?: false,
            cameraLens = state.lens,
            flashMode = state.flashMode,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            onEvent = photoViewModel::onEvent,
            onPermissionEvent = permissionHandler::onEvent
        )
    }
}

@Composable
private fun PhotoScreenContent(
    hasPermission: Boolean,
    cameraLens: Int?,
    @ImageCapture.FlashMode flashMode: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    onEvent: (PhotoViewModel.Event) -> Unit,
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
                    onFlashTapped = { onEvent(PhotoViewModel.Event.FlashTapped) },
                    onCloseTapped = { onEvent(PhotoViewModel.Event.CloseTapped) }
                )
                CaptureFooter(
                    modifier = Modifier.align(Alignment.BottomStart),
                    showFlipIcon = hasDualCamera,
                    onCaptureTapped = { onEvent(PhotoViewModel.Event.CaptureTapped) },
                    onFlipTapped = { onEvent(PhotoViewModel.Event.FlipTapped) }
                )
            }
        }
    }
}


@Composable
private fun CameraPreview(
    lens: Int,
    @ImageCapture.FlashMode flashMode: Int
) {
    val captureManager = LocalCaptureManager.current

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