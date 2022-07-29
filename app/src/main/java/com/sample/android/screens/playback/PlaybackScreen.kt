package com.sample.android.screens.playback

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sample.android.ScreenDestinations
import com.sample.android.navigateTo
import com.sample.android.shared.composables.*
import com.sample.android.shared.utils.LocalPlaybackManager
import com.sample.android.shared.utils.PlaybackManager
import kotlinx.coroutines.flow.collect

@Composable
internal fun PlaybackScreen(
    filePath: String,
    navHostController: NavHostController,
    playbackViewModel: PlaybackViewModel = viewModel()
) {
    val state by playbackViewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = object : PlaybackManager.PlaybackListener {
        override fun onPrepared() {
            playbackViewModel.onEvent(PlaybackViewModel.Event.Prepared)
        }

        override fun onProgress(progress: Int) {
            playbackViewModel.onEvent(PlaybackViewModel.Event.OnProgress(progress))
        }

        override fun onCompleted() {
            playbackViewModel.onEvent(PlaybackViewModel.Event.Completed)
        }
    }

    val playbackManager = remember {
        PlaybackManager.Builder(context)
            .apply {
                this.uri = Uri.parse(filePath)
                this.listener = listener
                this.lifecycleOwner = lifecycleOwner
            }
            .build()
    }

    CompositionLocalProvider(LocalPlaybackManager provides playbackManager) {
        PlaybackScreenContent(state, playbackViewModel::onEvent)
    }

    LaunchedEffect(playbackViewModel) {
        playbackViewModel.effect.collect {
            when (it) {
                PlaybackViewModel.Effect.NavigateUp -> navHostController.navigateTo(
                    ScreenDestinations.Landing.route
                )
                PlaybackViewModel.Effect.Pause -> playbackManager.pausePlayback()
                PlaybackViewModel.Effect.Play -> playbackManager.start(state.playbackPosition)
            }
        }
    }
}

@Composable
private fun PlaybackScreenContent(
    state: PlaybackViewModel.State,
    onEvent: (PlaybackViewModel.Event) -> Unit
) {
    val playbackManager = LocalPlaybackManager.current

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.Black)) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { playbackManager.videoView })
        when (state.playbackStatus) {
            PlaybackViewModel.PlaybackStatus.Idle -> {
                CameraPlayIcon(Modifier.align(Alignment.Center)) {
                    onEvent(PlaybackViewModel.Event.PlayTapped)
                }
            }
            PlaybackViewModel.PlaybackStatus.InProgress -> {
                CameraPauseIcon(Modifier.align(Alignment.Center)) {
                    onEvent(PlaybackViewModel.Event.PlayTapped)
                }
            }
            else -> {
                CircularProgressIndicator()
            }
        }
    }
}