package com.sample.android.screens.playback

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sample.android.ScreenDestinations
import com.sample.android.navigateTo
import com.sample.android.shared.composables.*
import com.sample.android.shared.utils.LocalPlaybackManager
import com.sample.android.shared.utils.VideoPlaybackManager
import kotlinx.coroutines.flow.collect

@Composable
internal fun PlaybackScreen(navHostController: NavHostController, playbackViewModel: PlaybackViewModel = viewModel()) {
    val state by playbackViewModel.state.collectAsState()
    if (state.filePath == null) return

    val context = LocalContext.current

    //TODO: Create a prepared listener that determines if the icons should be displayed
    //TODO: A completed listener to reset the playback position and update playback status
    //TODO: A playback in progress listener to check playback position
    val playbackManager = remember {
        VideoPlaybackManager.Builder(context)
            .apply { uri = Uri.parse(state.filePath) }
            .build()
    }

    CompositionLocalProvider(LocalPlaybackManager provides playbackManager) {
        PlaybackScreenContent(state, playbackViewModel::onEvent)
    }

    LaunchedEffect(playbackViewModel) {
        playbackViewModel.effect.collect {
            when (it) {
                PlaybackViewModel.Effect.NavigateUp -> navHostController.navigateTo(ScreenDestinations.Landing.route)
                PlaybackViewModel.Effect.Pause -> playbackManager.pausePlayback()
                PlaybackViewModel.Effect.Play -> playbackManager.start(state.playbackPosition)
            }
        }
    }
}

@Composable
private fun PlaybackScreenContent(state: PlaybackViewModel.State, onEvent: (PlaybackViewModel.Event) -> Unit) {
    val playbackManager = LocalPlaybackManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        CameraCloseIcon(modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)) {
            onEvent(PlaybackViewModel.Event.CloseTapped)
        }
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { playbackManager.videoView })
        if (state.playbackStatus == PlaybackViewModel.PlaybackStatus.InProgress) {
            CameraPauseIcon(
                Modifier
                    .size(60.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)) {
                onEvent(PlaybackViewModel.Event.PauseTapped)
            }
        } else {
            CameraPlayIcon(
                Modifier
                    .size(60.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)) {
                onEvent(PlaybackViewModel.Event.PlayTapped)
            }
        }

    }
}