@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.photo

import android.net.Uri
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.sample.android.R
import com.sample.android.ScreenDestinations
import com.sample.android.shared.composables.PermissionsHandler
import com.sample.android.shared.utils.FileManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalArgumentException

class PhotoViewModel constructor(
    private val fileManager: FileManager,
    val permissionsHandler: PermissionsHandler
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    init {
        permissionsHandler
            .state
            .onEach { handlerState ->
                _state.update { it.copy(multiplePermissionsState = handlerState.multiplePermissionsState) }
            }
            .catch { Timber.e(it) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: Event) {
        when (event) {
            Event.FlashTapped -> onFlashTapped()
            Event.CloseTapped -> onCloseTapped()
            Event.FlipTapped -> onFlipTapped()
            Event.CaptureTapped -> onCaptureTapped()

            is Event.CameraInitialized -> onCameraInitialized(event.cameraLensInfo)
            is Event.Error -> onError()
            is Event.ImageCaptured -> onImageCaptured(event.imageResult.savedUri)

            Event.PermissionRequired -> onPermissionRequired()
        }
    }

    private fun onFlashTapped() {
        _state.update {
            when (_state.value.flashMode) {
                ImageCapture.FLASH_MODE_OFF -> it.copy(flashMode = ImageCapture.FLASH_MODE_ON)
                //ImageCapture.FLASH_MODE_AUTO -> it.copy(flashMode = ImageCapture.FLASH_MODE_ON)
                ImageCapture.FLASH_MODE_ON -> it.copy(flashMode = ImageCapture.FLASH_MODE_OFF)
                else -> it.copy(flashMode = ImageCapture.FLASH_MODE_OFF)
            }
        }
    }

    private fun onCloseTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.NavigateTo(ScreenDestinations.Landing.route))
        }
    }

    private fun onFlipTapped() {
        val lens = if (_state.value.lens == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        //Check if the lens has flash unit
        val flashMode = if (_state.value.lensInfo[lens]?.hasFlashUnit() == true) {
            _state.value.flashMode
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
        if (_state.value.lensInfo[lens] != null) {
            _state.getAndUpdate { it.copy(lens = lens, flashMode = flashMode) }
        }
    }

    private fun onPermissionRequired() {
        permissionsHandler.onEvent(PermissionsHandler.Event.PermissionRequired)
    }

    private fun onCaptureTapped() {
        viewModelScope.launch {
            try {
                val filePath = fileManager.createFile("photos", "jpeg")
                _effect.emit(Effect.CaptureImage(filePath))
            } catch (exception: IllegalArgumentException) {
                Timber.e(exception)
                _effect.emit(Effect.ShowMessage())
            }
        }
    }

    private fun onImageCaptured(uri: Uri?) {
        if (uri != null && uri.path != null) {
            viewModelScope.launch {
                _effect.emit(Effect.NavigateTo(ScreenDestinations.Preview.createRoute(uri.encodedPath!!)))
            }
        } else {
            viewModelScope.launch {
                _effect.emit(Effect.ShowMessage(R.string.path_is_null))
            }
        }
    }

    private fun onError() {
        viewModelScope.launch {
            _effect.emit(Effect.ShowMessage())
        }
    }

    private fun onCameraInitialized(cameraLensInfo: HashMap<Int, CameraInfo>) {
        if (cameraLensInfo.isNotEmpty()) {
            val defaultLens = if (cameraLensInfo[CameraSelector.LENS_FACING_BACK] != null) {
                CameraSelector.LENS_FACING_BACK
            } else if (cameraLensInfo[CameraSelector.LENS_FACING_BACK] != null) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                null
            }
            _state.update {
                it.copy(
                    lens = it.lens ?: defaultLens,
                    lensInfo = cameraLensInfo
                )
            }
        }
    }

    data class State(
        val lens: Int? = null,
        @ImageCapture.FlashMode val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
        val lensInfo: MutableMap<Int, CameraInfo> = mutableMapOf(),
        val permissionRequestInFlight: Boolean = false,
        val hasCameraPermission: Boolean = false,
        val multiplePermissionsState: MultiplePermissionsState? = null,
        val permissionAction: PermissionsHandler.Action = PermissionsHandler.Action.NO_ACTION
    )

    sealed class Event {
        data class CameraInitialized(val cameraLensInfo: HashMap<Int, CameraInfo>) :
            PhotoViewModel.Event()

        data class ImageCaptured(val imageResult: ImageCapture.OutputFileResults) : Event()
        data class Error(val exception: Exception) : Event()

        object FlashTapped : Event()
        object CloseTapped : Event()
        object FlipTapped : Event()
        object CaptureTapped : Event()
        object PermissionRequired : Event()
    }

    sealed class Effect {

        data class ShowMessage(val message: Int = R.string.something_went_wrong) : Effect()
        data class CaptureImage(val filePath: String) : Effect()
        data class NavigateTo(val route: String) : Effect()
    }
}