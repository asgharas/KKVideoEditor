package com.asgharas.kkvideoeditor.core.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer


actual class VideoPlayer actual constructor() {

    private var context: Context? = null
    private var exoPlayer: ExoPlayer? = null
    private var completionCallback: (() -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    actual fun initialize(platformContext: Any?) {
        context = platformContext as? Context
            ?: throw IllegalArgumentException("Android VideoPlayer requires Context")
    }

    actual fun loadVideo(uri: String) {
        val ctx = context ?: throw IllegalStateException("Call initialize() first")

        release() // Release any existing player instance

        exoPlayer = ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
//                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        completionCallback?.invoke()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
//                    super.onPlayerError(error)
                    errorCallback?.invoke(error.message ?: "Unknown error")
                }
            })
        }
    }

    actual fun play() {
        exoPlayer?.play()
    }

    actual fun pause() {
        exoPlayer?.pause()
    }

    actual fun seekTo(timeMs: Long) {
        exoPlayer?.seekTo(timeMs)
    }

    actual fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    actual fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    actual fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }

    actual fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    actual fun setVolume(volume: Float) {
        exoPlayer?.volume = volume
    }

    actual fun setOnCompletionListener(callback: (() -> Unit)?) {
        completionCallback = callback
    }

    actual fun setOnErrorListener(callback: ((String) -> Unit)?) {
        errorCallback = callback
    }
}