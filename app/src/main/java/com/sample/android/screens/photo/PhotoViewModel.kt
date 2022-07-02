@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.screens.photo

import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sample.android.shared.PermissionAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PhotoViewModel : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    data class State(
        val lens: Int = 0,
        val permissionState: PermissionState? = null,
        val permissionAction: PermissionAction = PermissionAction.NO_ACTION
    )

    sealed class Event {

    }

    sealed class Effect {

    }
}