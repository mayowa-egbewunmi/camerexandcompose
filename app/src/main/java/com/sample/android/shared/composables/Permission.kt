@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.shared.composables

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.sample.android.shared.PermissionAction
import com.sample.android.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PermissionHandler {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun onEvent(event: Event) {
        when (event) {
            Event.PermissionDenied -> onPermissionDenied()
            Event.PermissionDismissTapped -> onPermissionDismissTapped()
            Event.PermissionNeverAskAgain -> onPermissionNeverShowAgain()
            Event.PermissionRationaleDisplayed -> onPermissionRationaleDisplayed()
            Event.PermissionRationaleOkTapped -> onPermissionRationaleOkTapped()
            Event.PermissionRequired -> onPermissionRequired()
            Event.PermissionSettingsTapped -> onPermissionSettingsTapped()
            Event.PermissionsGranted -> onPermissionGranted()
            is Event.PermissionStateInitialized -> onPermissionStateInitialized(event.permissionState)
        }
    }

    private fun onPermissionStateInitialized(permissionState: PermissionState) {
        _state.update { it.copy(permissionState = permissionState) }
    }

    private fun onPermissionGranted() {
        _state.update {
            it.copy(
                permissionRequestInFlight = false,
                permissionAction = PermissionAction.NO_ACTION
            )
        }
    }

    private fun onPermissionDenied() {
        _state.update {
            it.copy(
                permissionRequestInFlight = false,
                permissionAction = PermissionAction.NO_ACTION
            )
        }
    }

    private fun onPermissionNeverShowAgain() {
        _state.update {
            it.copy(
                permissionAction = PermissionAction.SHOW_NEVER_ASK_AGAIN,
                permissionRequestInFlight = false
            )
        }
    }

    private fun onPermissionRequired() {
        _state.value.permissionState?.let {
            val permissionAction =
                if (!it.hasPermission && !it.shouldShowRationale && !it.permissionRequested) {
                    PermissionAction.REQUEST_PERMISSION
                } else if (!it.hasPermission && it.shouldShowRationale) {
                    PermissionAction.SHOW_RATIONALE
                } else {
                    PermissionAction.SHOW_NEVER_ASK_AGAIN
                }
            _state.update { it.copy(permissionAction = permissionAction) }
        }
    }

    private fun onPermissionRationaleDisplayed() {
        _state.update { it.copy(permissionRequestInFlight = true) }
    }

    private fun onPermissionRationaleOkTapped() {
        _state.update {
            it.copy(
                permissionAction = PermissionAction.REQUEST_PERMISSION,
                permissionRequestInFlight = true
            )
        }
    }

    private fun onPermissionDismissTapped() {
        _state.update {
            it.copy(
                permissionAction = PermissionAction.NO_ACTION,
                permissionRequestInFlight = false
            )
        }
    }

    private fun onPermissionSettingsTapped() {
        _state.update {
            it.copy(
                permissionAction = PermissionAction.NO_ACTION,
                permissionRequestInFlight = false
            )
        }
    }

    data class State(
        val permissionState: PermissionState? = null,
        val permissionAction: PermissionAction = PermissionAction.NO_ACTION,
        val permissionRequestInFlight: Boolean = false
    )

    sealed class Event {
        object PermissionDenied : Event()
        object PermissionsGranted : Event()
        object PermissionSettingsTapped : Event()
        object PermissionNeverAskAgain : Event()
        object PermissionDismissTapped : Event()
        object PermissionRationaleDisplayed : Event()
        object PermissionRationaleOkTapped : Event()
        object PermissionRequired : Event()

        data class PermissionStateInitialized(val permissionState: PermissionState) : Event()
    }
}

@Composable
fun AccompanistPermissionHandler(permission: String): PermissionHandler {

    val permissionHandler = remember(permission) { PermissionHandler() }

    val state by permissionHandler.state.collectAsState()

    val permissionState = AccompanistPermissionHandler(
        permission = permission,
        onPermissionDenied = { permissionHandler.onEvent(PermissionHandler.Event.PermissionDenied) },
        onPermissionGranted = { permissionHandler.onEvent(PermissionHandler.Event.PermissionsGranted) },
        onPermissionNeverAskAgain = { permissionHandler.onEvent(PermissionHandler.Event.PermissionNeverAskAgain) }
    )

    LaunchedEffect(permissionState) {
        permissionHandler.onEvent(
            PermissionHandler.Event.PermissionStateInitialized(
                permissionState
            )
        )
    }

    state.permissionState?.let {
        HandlePermissionAction(
            action = state.permissionAction,
            permissionState = it,
            rationaleText = R.string.permission_rationale,
            neverAskAgainText = R.string.permission_rationale,
            onOkTapped = { permissionHandler.onEvent(PermissionHandler.Event.PermissionsGranted) },
            onSettingsTapped = { permissionHandler.onEvent(PermissionHandler.Event.PermissionSettingsTapped) },
        )
    }
    return permissionHandler
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AccompanistPermissionHandler(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionNeverAskAgain: () -> Unit
): PermissionState {
    val permissionState = rememberPermissionState(permission)

    var hasPermission by remember { mutableStateOf(permissionState.hasPermission) }
    var permissionRequested by remember { mutableStateOf(permissionState.permissionRequested) }
    var shouldShowRationale by remember { mutableStateOf(permissionState.shouldShowRationale) }

    LaunchedEffect(permissionState.hasPermission) {
        if (permissionState.hasPermission != hasPermission) {

            hasPermission = permissionState.hasPermission
            permissionRequested = permissionState.permissionRequested
            shouldShowRationale = permissionState.shouldShowRationale

            if (hasPermission) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    LaunchedEffect(permissionState.permissionRequested) {
        if (permissionState.permissionRequested != permissionRequested) {
            permissionRequested = permissionState.permissionRequested
            shouldShowRationale = permissionState.shouldShowRationale

            if (permissionRequested && !shouldShowRationale) {
                onPermissionNeverAskAgain()
            } else if (permissionRequested && shouldShowRationale) {
                onPermissionDenied()
            }
        }
    }

    LaunchedEffect(permissionState.shouldShowRationale) {
        if (permissionState.shouldShowRationale != shouldShowRationale) {
            permissionRequested = permissionState.permissionRequested
            shouldShowRationale = permissionState.shouldShowRationale

            if (permissionRequested && !shouldShowRationale) {
                onPermissionNeverAskAgain()
            } else if (permissionRequested && shouldShowRationale) {
                onPermissionDenied()
            }
        }
    }
    return permissionState
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionAction(
    action: PermissionAction,
    permissionState: PermissionState,
    @StringRes rationaleText: Int,
    @StringRes neverAskAgainText: Int,
    onOkTapped: () -> Unit,
    onSettingsTapped: () -> Unit,
) {
    val context = LocalContext.current
    when (action) {
        PermissionAction.REQUEST_PERMISSION -> {
            LaunchedEffect(true) {
                permissionState.launchPermissionRequest()
            }
        }
        PermissionAction.SHOW_RATIONALE -> {
            PermissionRationaleDialog(
                message = stringResource(rationaleText),
                onOkTapped = onOkTapped
            )
        }
        PermissionAction.SHOW_NEVER_ASK_AGAIN -> {
            ShowGotoSettingsDialog(
                title = stringResource(R.string.allow_permission),
                message = stringResource(neverAskAgainText),
                onSettingsTapped = {
                    onSettingsTapped()
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:" + context.packageName)
                        context.startActivity(this)
                    }
                },
            )
        }
        PermissionAction.NO_ACTION -> Unit
    }
}

@Composable
fun PermissionRationaleDialog(
    message: String,
    title: String = stringResource(R.string.allow_permission),
    primaryButtonText: String = stringResource(R.string.ok),
    onOkTapped: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        },
        buttons = {
            Text(
                text = primaryButtonText.uppercase(),
                modifier = Modifier
                    .clickable { onOkTapped() }
                    .padding(vertical = 12.dp),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowGotoSettingsDialog(
    title: String,
    message: String,
    onSettingsTapped: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        },
        buttons = {
            Text(
                text = stringResource(id = R.string.settings),
                modifier = Modifier
                    .clickable { onSettingsTapped() }
                    .padding(vertical = 12.dp),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
