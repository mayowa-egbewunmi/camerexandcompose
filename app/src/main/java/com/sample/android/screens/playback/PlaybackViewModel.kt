package com.sample.android.screens.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaybackViewModel : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect: SharedFlow<Effect> = _effect

    fun onEvent(event: Event) {
        when (event) {
            Event.PauseTapped -> onPauseTapped()
            Event.PlayTapped -> onPlayTapped()

            is Event.OnProgress -> onProgress(event.progress)
            Event.Prepared -> onPrepared()
            Event.Completed -> onCompleted()
        }
    }

    private fun onPauseTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.Pause)
        }
        _state.update { it.copy(playbackStatus = PlaybackStatus.Idle) }
    }

    private fun onPlayTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.Play)
        }
    }

    private fun onProgress(progress: Int) {
        _state.update { it.copy(playbackPosition = progress, playbackStatus = PlaybackStatus.InProgress) }
    }

    private fun onPrepared() {
        _state.update { it.copy(playbackStatus = PlaybackStatus.Idle) }
    }

    private fun onCompleted() {
        _state.update { it.copy(playbackStatus = PlaybackStatus.Idle, playbackPosition = 0) }
    }

    data class State(
        val filePath: String? = null,
        val playbackStatus: PlaybackStatus? = PlaybackStatus.Idle,
        val playbackPosition: Int = 0
    )

    sealed class Effect {
        object NavigateUp : Effect()
        object Pause : Effect()
        object Play : Effect()
    }

    sealed class Event {
        object PlayTapped : Event()
        object PauseTapped : Event()

        object Prepared : Event()
        object Completed : Event()
        data class OnProgress(val progress: Int) : Event()
    }

    sealed class PlaybackStatus {
        object Idle : PlaybackStatus()
        object InProgress : PlaybackStatus()
    }
}