package com.asgharas.kkvideoeditor.core.player

/**
 * Platform-independent video player interface
 * Implementations handle native video playback on each platform
 *
 * Android implementation uses ExoPlayer
 * iOS implementation uses AVPlayer
 */
expect class VideoPlayer() {

    /**
     * Initialize the player with platform-specific context
     * Must be called before any other operations
     *
     * @param platformContext Android: Context, iOS: null (not needed)
     */
    fun initialize(platformContext: Any?)

    /**
     * Load a video from the given URI/path
     * This prepares the video for playback but doesn't start playing
     *
     * @param uri Platform-specific video URI or file path
     * @throws VideoPlayerException if video cannot be loaded
     */
    fun loadVideo(uri: String)

    /**
     * Start or resume video playback
     * If video is at the end, it will restart from the beginning
     */
    fun play()

    /**
     * Pause video playback at current position
     * Can be resumed with play()
     */
    fun pause()

    /**
     * Seek to a specific time position in the video
     *
     * @param timeMs Time position in milliseconds
     */
    fun seekTo(timeMs: Long)

    /**
     * Stop playback and release all resources
     * Player cannot be reused after this call - create a new instance
     * Always call this when done with the player to prevent memory leaks
     */
    fun release()

    /**
     * Get the current playback position in milliseconds
     * Returns 0 if no video is loaded
     */
    fun getCurrentPosition(): Long

    /**
     * Get the total duration of the loaded video in milliseconds
     * Returns 0 if no video is loaded
     */
    fun getDuration(): Long

    /**
     * Check if the player is currently playing
     * Returns false if paused, stopped, or no video loaded
     */
    fun isPlaying(): Boolean

    /**
     * Set the playback volume
     *
     * @param volume Volume level from 0.0 (mute) to 1.0 (full volume)
     */
    fun setVolume(volume: Float)

    /**
     * Set a callback to be invoked when playback reaches the end
     *
     * @param callback Function to call when video ends, or null to remove callback
     */
    fun setOnCompletionListener(callback: (() -> Unit)?)

    /**
     * Set a callback to be invoked when an error occurs during playback
     *
     * @param callback Function to call with error message, or null to remove callback
     */
    fun setOnErrorListener(callback: ((String) -> Unit)?)
}

/**
 * Exception thrown by VideoPlayer operations
 */
class VideoPlayerException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Player state enum for tracking playback state
 * Useful for UI state management
 */
enum class PlayerState {
    IDLE,       // No video loaded
    LOADING,    // Video is being prepared
    READY,      // Video loaded and ready to play
    PLAYING,    // Currently playing
    PAUSED,     // Paused at a position
    ENDED,      // Playback completed
    ERROR       // Error occurred
}
