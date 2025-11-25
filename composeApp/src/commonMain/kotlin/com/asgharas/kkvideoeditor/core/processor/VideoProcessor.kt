package com.asgharas.kkvideoeditor.core.processor

import com.asgharas.kkvideoeditor.data.model.VideoFilter
import com.asgharas.kkvideoeditor.data.model.VideoMetadata


/**
 * Platform-independent video processing interface
 * Handles video manipulation operations like trimming, filtering, etc.
 *
 * Android implementation uses Media3 Transformer / MediaCodec
 * iOS implementation uses AVFoundation (AVAssetExportSession, AVComposition)
 */
expect class VideoProcessor() {

    /**
     * Trim a video to a specific time range
     * Creates a new video file with only the specified portion
     *
     * @param sourceUri Path to the source video file
     * @param startMs Start time in milliseconds (inclusive)
     * @param endMs End time in milliseconds (exclusive)
     * @param outputPath Path where the trimmed video should be saved
     * @return Result containing output path on success, or error on failure
     */
    suspend fun trimVideo(
        sourceUri: String,
        startMs: Long,
        endMs: Long,
        outputPath: String
    ): Result<String>

    /**
     * Extract metadata from a video file
     * Reads video properties without loading the entire file into memory
     *
     * @param uri Path to the video file
     * @return Result containing VideoMetadata on success, or error on failure
     */
    suspend fun getVideoMetadata(uri: String): Result<VideoMetadata>

    /**
     * Apply filters to a video
     * Creates a new video file with filters applied
     *
     * @param sourceUri Path to the source video file
     * @param filters List of filters to apply in order
     * @param outputPath Path where the filtered video should be saved
     * @param onProgress Callback to report progress (0.0 to 1.0)
     * @return Result containing output path on success, or error on failure
     */
    suspend fun applyFilters(
        sourceUri: String,
        filters: List<VideoFilter>,
        outputPath: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String>

    /**
     * Generate a thumbnail image from a video at a specific time
     * Useful for timeline previews
     *
     * @param uri Path to the video file
     * @param timeMs Time position in milliseconds to capture
     * @param width Desired thumbnail width in pixels
     * @param height Desired thumbnail height in pixels
     * @return Result containing path to saved thumbnail on success, or error on failure
     */
    suspend fun generateThumbnail(
        uri: String,
        timeMs: Long,
        width: Int = 160,
        height: Int = 90
    ): Result<String>

    /**
     * Check if a video file is valid and can be processed
     * Performs quick validation without full metadata extraction
     *
     * @param uri Path to the video file
     * @return true if video is valid, false otherwise
     */
    suspend fun isValidVideo(uri: String): Boolean

    /**
     * Cancel any ongoing processing operation
     * Safe to call even if no operation is in progress
     */
    fun cancelProcessing()
}

/**
 * Exception thrown by VideoProcessor operations
 */
class VideoProcessingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Result wrapper for video processing operations
 * Provides structured success/error handling
 */
sealed class ProcessingResult<out T> {
    data class Success<T>(val data: T) : ProcessingResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : ProcessingResult<Nothing>()
    data class Progress(val progress: Float) : ProcessingResult<Nothing>() // 0.0 to 1.0
}

/**
 * Processing status for tracking long-running operations
 */
enum class ProcessingStatus {
    IDLE,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    FAILED
}
