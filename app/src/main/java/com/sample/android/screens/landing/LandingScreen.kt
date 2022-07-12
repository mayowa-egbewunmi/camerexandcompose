package com.sample.android.screens.landing

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sample.android.R
import com.sample.android.ScreenDestinations

@Composable
internal fun EntryScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .defaultMinSize(minHeight = 60.dp),
            shape = MaterialTheme.shapes.small,
            onClick = { navController.navigate(ScreenDestinations.Photo.route) },
        ) {
            Text(text = stringResource(id = R.string.take_picture))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .defaultMinSize(minHeight = 60.dp),
            shape = MaterialTheme.shapes.small,
            onClick = { navController.navigate(ScreenDestinations.Video.route) },
        ) {
            Text(text = stringResource(id = R.string.record_video))
        }
    }
}