package com.asgharas.kkvideoeditor.core.player

actual class VideoPlayer actual constructor() {

    actual fun initialize(platformContext: Any?) {
    }

    actual fun loadVideo(uri: String) {
    }

    actual fun play() {
    }

    actual fun pause() {
    }

    actual fun seekTo(timeMs: Long) {
    }

    actual fun release() {
    }

    actual fun getCurrentPosition(): Long {
        TODO("Not yet implemented")
    }

    actual fun getDuration(): Long {
        TODO("Not yet implemented")
    }

    actual fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun setVolume(volume: Float) {
    }

    actual fun setOnCompletionListener(callback: (() -> Unit)?) {
    }

    actual fun setOnErrorListener(callback: ((String) -> Unit)?) {
    }


}