@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.photo

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sample.android.shared.PreviewState
import com.sample.android.R
import com.sample.android.shared.composables.*

@Composable
internal fun PhotoScreen(
    navHostController: NavHostController,
    factory: ViewModelProvider.Factory,
    photoViewModel: PhotoViewModel = viewModel(factory = factory)
) {
    val state by photoViewModel.state.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember {
        object : PhotoCaptureManager.PhotoListener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
//                viewModel.onEvent(FaceIDCameraViewModel.Event.CameraInitialized(cameraLensInfo))
            }
            override fun onSuccess(imageResult: ImageCapture.OutputFileResults) {
//                viewModel.onEvent(FaceIDCameraViewModel.Event.ImageCaptured(imageResult))
            }
            override fun onError(exception: Exception) {
//                viewModel.onEvent(FaceIDCameraViewModel.Event.Error(exception))
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
        onPermissionDenied = { /*viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionDenied)*/ },
        onPermissionGranted = { /*viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionsGranted)*/ },
        onPermissionNeverAskAgain = { /*viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionNeverAskAgain)*/ }
    )

    LaunchedEffect(permissionState) {
//        viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionStateInitialized(permissionState))
    }

    state.permissionState?.let {
        HandlePermissionAction(
            action = state.permissionAction,
            permissionState = it,
            rationaleText = R.string.permission_rationale,
            neverAskAgainText = R.string.permission_rationale,
            onOkTapped = { /*viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionsGranted)*/ },
            onSettingsTapped = { /*viewModel.onEvent(FaceIDCameraViewModel.Event.PermissionSettingsTapped)*/ },
        )
    }

    BackHandler {
        navHostController.popBackStack()
    }

}

@Composable
private fun PhotoScreenContent(
    flashSupported: Boolean,
    flashMode: Int,
    flipSupported: Boolean,
    cameraLens: Int?,
    captureManager: PhotoCaptureManager,
    permissionState: PermissionState?,
    onEvent: (PhotoViewModel.Event) -> Unit
) {
    if (permissionState?.hasPermission != true) {
        PermissionRequestScreen(onClick = { /*onEvent(PhotoViewModel.Event.PermissionRequired)*/ })
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            CaptureHeader(
                flashSupported = flashSupported,
                flashMode = flashMode,
                onFlashTapped = { /*onEvent(FaceIDCameraViewModel.Event.FlashTapped)*/ },
                onCloseTapped = { /*onEvent(FaceIDCameraViewModel.Event.CloseTapped)*/ }
            )
            Spacer(modifier = Modifier
                .height(16.dp)
                .fillMaxWidth()
                .background(color = Color.Black))
            Column(modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 300.dp)) {
                cameraLens?.let { FaceCapturePreview(captureManager = captureManager, lens = it, flashMode = flashMode) }
            }
            cameraLens?.let {
                CaptureFooter(
                    flipSupported = flipSupported,
                    onCaptureTapped = { /*onEvent(FaceIDCameraViewModel.Event.CaptureTapped)*/ },
                    onFlipTapped = { /*onEvent(FaceIDCameraViewModel.Event.FlipCameraLensTapped)*/ }
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { /*TODO*/ }) {
            Text(text = "<Request Permission>")
        }
    }
}

@Composable
private fun CaptureHeader(modifier: Modifier = Modifier, flashSupported: Boolean, flashMode: Int, onFlashTapped: () -> Unit, onCloseTapped: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(Color.Black)
        .padding(8.dp)
        .then(modifier)
    ) {
        if (flashSupported) {
            CameraFlashIcon(flashEnabled =  flashMode == ImageCapture.FLASH_MODE_ON, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun CaptureFooter(flipSupported: Boolean, onCaptureTapped: () -> Unit, onFlipTapped: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black)
        .padding(horizontal = 16.dp, vertical = 24.dp)) {
        CameraCaptureIcon(modifier = Modifier.align(Alignment.Center), onTapped = onCaptureTapped)
        if (flipSupported) {
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