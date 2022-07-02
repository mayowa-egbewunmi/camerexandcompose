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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AccompanistPermissionState(
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
