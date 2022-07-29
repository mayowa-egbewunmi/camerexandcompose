@file:OptIn(ExperimentalPermissionsApi::class)

package com.sample.android.shared.composables

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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
            is Event.PermissionStateUpdated -> onPermissionStateUpdated(event.permissionState)
        }
    }

    private fun onPermissionStateUpdated(permissionState: MultiplePermissionsState) {
        _state.update { it.copy(multiplePermissionsState = permissionState) }
    }

    private fun onPermissionGranted() {
        _state.update {
            it.copy(
                permissionRequestInFlight = false,
                permissionAction = Action.NO_ACTION
            )
        }
    }

    private fun onPermissionDenied() {
        _state.update {
            it.copy(
                permissionRequestInFlight = false,
                permissionAction = Action.NO_ACTION
            )
        }
    }

    private fun onPermissionNeverShowAgain() {
        _state.update {
            it.copy(
                permissionAction = Action.SHOW_NEVER_ASK_AGAIN,
                permissionRequestInFlight = false
            )
        }
    }

    private fun onPermissionRequired() {
        _state.value.multiplePermissionsState?.let {
            val permissionAction =
                if (!it.allPermissionsGranted && !it.shouldShowRationale && !it.permissionRequested) {
                    Action.REQUEST_PERMISSION
                } else if (!it.allPermissionsGranted && it.shouldShowRationale) {
                    Action.SHOW_RATIONALE
                } else {
                    Action.SHOW_NEVER_ASK_AGAIN
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
                permissionAction = Action.REQUEST_PERMISSION,
                permissionRequestInFlight = true
            )
        }
    }

    private fun onPermissionDismissTapped() {
        _state.update {
            it.copy(
                permissionAction = Action.NO_ACTION,
                permissionRequestInFlight = false
            )
        }
    }

    private fun onPermissionSettingsTapped() {
        _state.update {
            it.copy(
                permissionAction = Action.NO_ACTION,
                permissionRequestInFlight = false
            )
        }
    }

    data class State(
        val multiplePermissionsState: MultiplePermissionsState? = null,
        val permissionAction: Action = Action.NO_ACTION,
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

        data class PermissionStateUpdated(val permissionState: MultiplePermissionsState) :
            Event()
    }

    enum class Action {
        REQUEST_PERMISSION, SHOW_RATIONALE, SHOW_NEVER_ASK_AGAIN, NO_ACTION
    }
}

@Composable
fun AccompanistPermissionsState(
    permissions: List<String>,
    permissionHandler: PermissionHandler
) {

    val state by permissionHandler.state.collectAsState()
    val permissionStates = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(permissionStates) {
        permissionHandler.onEvent(PermissionHandler.Event.PermissionStateUpdated(permissionStates))
        when {
            permissionStates.allPermissionsGranted -> {
                permissionHandler.onEvent(PermissionHandler.Event.PermissionsGranted)
            }
            permissionStates.permissionRequested && !permissionStates.shouldShowRationale -> {
                permissionHandler.onEvent(PermissionHandler.Event.PermissionNeverAskAgain)
            }
            else -> {
                permissionHandler.onEvent(PermissionHandler.Event.PermissionDenied)
            }
        }
    }

    HandlePermissionAction(
        action = state.permissionAction,
        permissionStates = state.multiplePermissionsState,
        rationaleText = R.string.permission_rationale,
        neverAskAgainText = R.string.permission_rationale,
        onOkTapped = { permissionHandler.onEvent(PermissionHandler.Event.PermissionRationaleOkTapped) },
        onSettingsTapped = { permissionHandler.onEvent(PermissionHandler.Event.PermissionSettingsTapped) },
    )
}

@Composable
fun HandlePermissionAction(
    action: PermissionHandler.Action,
    permissionStates: MultiplePermissionsState?,
    @StringRes rationaleText: Int,
    @StringRes neverAskAgainText: Int,
    onOkTapped: () -> Unit,
    onSettingsTapped: () -> Unit,
) {
    val context = LocalContext.current
    when (action) {
        PermissionHandler.Action.REQUEST_PERMISSION -> {
            LaunchedEffect(true) {
                permissionStates?.launchMultiplePermissionRequest()
            }
        }
        PermissionHandler.Action.SHOW_RATIONALE -> {
            PermissionRationaleDialog(
                message = stringResource(rationaleText),
                onOkTapped = onOkTapped
            )
        }
        PermissionHandler.Action.SHOW_NEVER_ASK_AGAIN -> {
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
        PermissionHandler.Action.NO_ACTION -> Unit
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
                color = Color.Black
            )
        },
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = primaryButtonText.uppercase(),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onOkTapped() },
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = stringResource(id = R.string.settings),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onSettingsTapped() },
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
