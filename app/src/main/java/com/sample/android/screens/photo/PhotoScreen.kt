@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.photo

import android.Manifest
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sample.android.shared.PreviewState
import com.sample.android.R
import com.sample.android.navigateTo
import com.sample.android.shared.composables.*
import kotlinx.coroutines.flow.collect

@Composable
internal fun PhotoScreen(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    photoViewModel: PhotoViewModel = viewModel(factory = factory),
    onShowMessage: (message: Int) -> Unit
) {
    val state by photoViewModel.state.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember {
        object : PhotoCaptureManager.PhotoListener {
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
        PhotoCaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { photoListener = listener }
    }

    val permissionState = AccompanistPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionDenied = { photoViewModel.onEvent(PhotoViewModel.Event.PermissionDenied) },
        onPermissionGranted = { photoViewModel.onEvent(PhotoViewModel.Event.PermissionsGranted) },
        onPermissionNeverAskAgain = { photoViewModel.onEvent(PhotoViewModel.Event.PermissionNeverAskAgain) }
    )

    LaunchedEffect(permissionState) {
        photoViewModel.onEvent(PhotoViewModel.Event.PermissionStateInitialized(permissionState))
    }

    state.permissionState?.let {
        HandlePermissionAction(
            action = state.permissionAction,
            permissionState = it,
            rationaleText = R.string.permission_rationale,
            neverAskAgainText = R.string.permission_rationale,
            onOkTapped = { photoViewModel.onEvent(PhotoViewModel.Event.PermissionsGranted) },
            onSettingsTapped = { photoViewModel.onEvent(PhotoViewModel.Event.PermissionSettingsTapped) },
        )
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

    PhotoScreenContent(
        cameraLens = state.lens,
        flashMode = state.flashMode,
        hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
        hasDualCamera = state.lensInfo.size > 1,
        captureManager = captureManager,
        permissionState = state.permissionState,
        onEvent = photoViewModel::onEvent
    )
}

@Composable
private fun PhotoScreenContent(
    cameraLens: Int?,
    @ImageCapture.FlashMode flashMode: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    captureManager: PhotoCaptureManager,
    permissionState: PermissionState?,
    onEvent: (PhotoViewModel.Event) -> Unit
) {
    if (permissionState?.hasPermission != true) {
        PermissionRequestScreen(onClick = { onEvent(PhotoViewModel.Event.PermissionRequired) })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            cameraLens?.let {
                FaceCapturePreview(captureManager = captureManager, lens = it, flashMode = flashMode)
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
private fun PermissionRequestScreen(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onClick) {
            Text(text = stringResource(id = R.string.request_permission))
        }
    }
}

@Composable
private fun CaptureHeader(modifier: Modifier = Modifier, showFlashIcon: Boolean, flashMode: Int, onFlashTapped: () -> Unit, onCloseTapped: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(8.dp)
        .then(modifier)
    ) {
        if (showFlashIcon) {
            CameraFlashIcon(flashMode =  flashMode, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun CaptureFooter(modifier: Modifier = Modifier, showFlipIcon: Boolean, onCaptureTapped: () -> Unit, onFlipTapped: () -> Unit) {
    Box(modifier = Modifier
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
private fun FaceCapturePreview(captureManager: PhotoCaptureManager, lens: Int, @ImageCapture.FlashMode flashMode: Int) {
    Box {
        AndroidView(
            factory = { captureManager.showPreview(PreviewState(cameraLens = lens, flashMode = flashMode)) },
            modifier = Modifier.fillMaxSize(),
            update = { captureManager.updatePreview(PreviewState(cameraLens = lens, flashMode = flashMode), it) }
        )
    }
}