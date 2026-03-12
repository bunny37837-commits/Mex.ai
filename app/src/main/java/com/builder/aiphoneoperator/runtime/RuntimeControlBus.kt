package com.builder.aiphoneoperator.runtime

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface RuntimeControlSignal {
    data object Stop : RuntimeControlSignal
    data object Pause : RuntimeControlSignal
    data object Cancel : RuntimeControlSignal
}

object RuntimeControlBus {
    private val _signals = MutableSharedFlow<RuntimeControlSignal>(extraBufferCapacity = 8)
    val signals = _signals.asSharedFlow()

    fun stop() {
        _signals.tryEmit(RuntimeControlSignal.Stop)
    }

    fun pause() {
        _signals.tryEmit(RuntimeControlSignal.Pause)
    }

    fun cancel() {
        _signals.tryEmit(RuntimeControlSignal.Cancel)
    }
}
