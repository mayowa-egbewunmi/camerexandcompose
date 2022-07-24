package com.sample.android.shared.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoPlaybackManager private constructor(private val builder: Builder) {

    private val context = builder.context

    private val lifecycleOwner = builder.lifecycleOwner

    private lateinit var coroutineJob: Job

    val videoView: VideoView by lazy {
        VideoView(context).apply {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutParams = params
            setVideoURI(builder.uri)
            setOnPreparedListener(builder.preparedListener)
            setOnCompletionListener(builder.completionListener)
            requestFocus()
        }
    }

    fun start(currentPosition: Int) {
        videoView.seekTo(currentPosition)
        videoView.start()
        coroutineJob = lifecycleOwner!!.lifecycleScope.launch {
            while (videoView.isPlaying) {
                builder.progressListener(videoView.currentPosition)
                delay(1000)
            }
        }
    }

    fun pausePlayback() {
        videoView.pause()
        coroutineJob.cancel()
    }

    class Builder(val context: Context) {

        var preparedListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener {}

        var completionListener: MediaPlayer.OnCompletionListener =
            MediaPlayer.OnCompletionListener {}

        var progressListener: (Int) -> Unit = {}

        var lifecycleOwner: LifecycleOwner? = null

        var uri: Uri? = null

        fun build(): VideoPlaybackManager {
            requireNotNull(uri)
            requireNotNull(lifecycleOwner)
            return VideoPlaybackManager(this)
        }
    }
}

val LocalPlaybackManager = compositionLocalOf<VideoPlaybackManager> { error("No playback manager found!") }
