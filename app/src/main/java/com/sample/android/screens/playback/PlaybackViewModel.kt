package com.sample.android.screens.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect: SharedFlow<Effect> = _effect

    fun onEvent(event: Event) {
        when (event) {
            Event.CloseTapped -> onCloseTapped()
            Event.PauseTapped -> onPauseTapped()
            Event.PlayTapped -> onPlayTapped()
        }
    }

    private fun onCloseTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.NavigateUp)
        }
    }

    private fun onPauseTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.Pause)
        }
    }

    private fun onPlayTapped() {
        viewModelScope.launch {
            _effect.emit(Effect.Play)
        }
    }

    data class State(
        val filePath: String? = null,
        val playbackStatus: PlaybackStatus = PlaybackStatus.Idle,
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

        object CloseTapped : Event()
    }

    sealed class PlaybackStatus {
        object Idle : PlaybackStatus()
        object InProgress : PlaybackStatus()
        object Paused : PlaybackStatus()
    }
}