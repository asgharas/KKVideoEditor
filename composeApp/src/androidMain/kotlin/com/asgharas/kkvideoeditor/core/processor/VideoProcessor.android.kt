package com.asgharas.kkvideoeditor.core.processor

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.asgharas.kkvideoeditor.data.model.VideoFilter
import com.asgharas.kkvideoeditor.data.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class VideoProcessor {

    private var context: Context? = null
    private var currentTransformer: Transformer? = null

    companion object {
        private const val TAG = "VideoProcessor"
    }

    /**
     * Initialize with Android Context
     */
    actual fun initialize(platformContext: Any?) {
        context = platformContext as? Context
            ?: throw IllegalArgumentException("Android VideoProcessor requires Context")
        Log.d(TAG, "VideoProcessor initialized")
    }

    /**
     * Trim a video to a specific time range
     * Uses Media3 Transformer for efficient video processing
     */
    actual suspend fun trimVideo(
        sourceUri: String,
        startMs: Long,
        endMs: Long,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.Main) {
        val ctx = context ?: return@withContext Result.failure(
            VideoProcessingException("VideoProcessor not initialized. Call initialize() first")
        )

        try {
            Log.d(TAG, "=== Starting Trim Operation ===")
            Log.d(TAG, "Source URI: $sourceUri")
            Log.d(TAG, "Start: ${startMs}ms, End: ${endMs}ms")
            Log.d(TAG, "Duration: ${endMs - startMs}ms")

            // Generate output path if not provided or empty
            val finalOutputPath = if (outputPath.isEmpty() || outputPath.isBlank()) {
                val outputDir = ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
                    ?: ctx.filesDir
                val outputFile = File(outputDir, "trimmed_${System.currentTimeMillis()}.mp4")
                Log.d(TAG, "Generated output path: ${outputFile.absolutePath}")
                outputFile.absolutePath
            } else {
                Log.d(TAG, "Using provided output path: $outputPath")
                outputPath
            }

            // Ensure output directory exists
            val outputFile = File(finalOutputPath)
            outputFile.parentFile?.let { parentDir ->
                if (!parentDir.exists()) {
                    val created = parentDir.mkdirs()
                    Log.d(TAG, "Created output directory: $created")
                }
            }

            // Delete existing file if it exists
            if (outputFile.exists()) {
                val deleted = outputFile.delete()
                Log.d(TAG, "Deleted existing output file: $deleted")
            }

            suspendCoroutine { continuation ->
                try {
                    // Create clipping configuration
                    val clippingConfiguration = MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs)
                        .build()

                    Log.d(TAG, "Created clipping configuration")

                    // Parse URI - handle both file paths and content URIs
                    val uri = when {
                        sourceUri.startsWith("content://") -> {
                            Log.d(TAG, "Using content URI")
                            Uri.parse(sourceUri)
                        }

                        sourceUri.startsWith("file://") -> {
                            Log.d(TAG, "Using file URI")
                            Uri.parse(sourceUri)
                        }

                        else -> {
                            Log.d(TAG, "Converting file path to URI")
                            Uri.fromFile(File(sourceUri))
                        }
                    }

                    Log.d(TAG, "Parsed URI: $uri")

                    // Create media item with clipping
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setClippingConfiguration(clippingConfiguration)
                        .build()

                    val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()
                    Log.d(TAG, "Created EditedMediaItem")

                    // Build transformer
                    val transformer = Transformer.Builder(ctx)
                        .build()

                    Log.d(TAG, "Created Transformer")

                    currentTransformer = transformer

                    // Set up listener
                    transformer.addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, result: ExportResult) {
                            Log.d(TAG, "=== Trim Completed ===")
                            Log.d(TAG, "Export result: $result")
                            currentTransformer = null

                            // Verify file was created
                            if (outputFile.exists()) {
                                val fileSize = outputFile.length()
                                Log.d(TAG, "Output file exists, size: ${fileSize / 1024}KB")

                                if (fileSize > 0) {
                                    continuation.resume(Result.success(finalOutputPath))
                                } else {
                                    Log.e(TAG, "Output file is empty!")
                                    continuation.resume(
                                        Result.failure(
                                            VideoProcessingException("Output file is empty (0 bytes)")
                                        )
                                    )
                                }
                            } else {
                                Log.e(TAG, "Output file was not created!")
                                continuation.resume(
                                    Result.failure(
                                        VideoProcessingException("Output file not created at: $finalOutputPath")
                                    )
                                )
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            result: ExportResult,
                            exception: ExportException
                        ) {
                            Log.e(TAG, "=== Trim Failed ===")
                            Log.e(TAG, "Error code: ${exception.errorCode}")
                            Log.e(TAG, "Error message: ${exception.message}")
                            Log.e(TAG, "Stack trace:", exception)

                            currentTransformer = null

                            val errorMessage = when (exception.errorCode) {
                                ExportException.ERROR_CODE_FAILED_RUNTIME_CHECK ->
                                    "Runtime check failed. This may be a codec issue."

                                ExportException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                                    "Input file not found at: $sourceUri"

                                ExportException.ERROR_CODE_IO_UNSPECIFIED ->
                                    "I/O error. Check file permissions and storage space."

                                ExportException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED ->
                                    "Video format not supported for decoding."

                                ExportException.ERROR_CODE_ENCODER_INIT_FAILED ->
                                    "Failed to initialize encoder."

                                else -> exception.message ?: "Unknown error"
                            }

                            continuation.resume(
                                Result.failure(
                                    VideoProcessingException(
                                        "Trim failed: $errorMessage (code: ${exception.errorCode})",
                                        exception
                                    )
                                )
                            )
                        }
                    })

                    Log.d(TAG, "Added listener, starting transformation...")

                    // Start transformation
                    transformer.start(editedMediaItem, finalOutputPath)
                    Log.d(TAG, "Transformer.start() called successfully")

                } catch (e: Exception) {
                    Log.e(TAG, "Exception during trim setup", e)
                    currentTransformer = null
                    continuation.resume(
                        Result.failure(
                            VideoProcessingException("Failed to set up trim: ${e.message}", e)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Trim operation exception", e)
            currentTransformer = null
            Result.failure(VideoProcessingException("Trim operation failed: ${e.message}", e))
        }
    }

    /**
     * Extract metadata from a video file
     * Uses MediaMetadataRetriever for efficient metadata access
     */
    actual suspend fun getVideoMetadata(uri: String): Result<VideoMetadata> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting metadata for: $uri")

                val retriever = MediaMetadataRetriever()

                // Handle both file paths and content URIs
                if (uri.startsWith("content://") || uri.startsWith("file://")) {
                    retriever.setDataSource(context, Uri.parse(uri))
                } else {
                    retriever.setDataSource(uri)
                }

                // Extract metadata
                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val widthStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val bitrateStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                val mimeType =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                val hasAudioStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)

                // Get file size
                val fileSize = try {
                    if (uri.startsWith("content://")) {
                        context?.contentResolver?.openFileDescriptor(Uri.parse(uri), "r")?.use {
                            it.statSize
                        } ?: 0L
                    } else {
                        File(uri).length()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not get file size", e)
                    0L
                }

                retriever.release()

                // Parse values with defaults
                val duration = durationStr?.toLongOrNull() ?: 0L
                val width = widthStr?.toIntOrNull() ?: 0
                val height = heightStr?.toIntOrNull() ?: 0
                val bitrate = bitrateStr?.toLongOrNull() ?: 0L
                val hasAudio = hasAudioStr == "yes"

                // Note: Android doesn't easily expose FPS through MediaMetadataRetriever
                // We'll default to 30fps. For accurate FPS, would need MediaExtractor
                val fps = 30f

                val metadata = VideoMetadata(
                    durationMs = duration,
                    width = width,
                    height = height,
                    fps = fps,
                    bitrate = bitrate,
                    hasAudio = hasAudio,
                    fileSize = fileSize,
                    mimeType = mimeType ?: "video/mp4"
                )

                Log.d(TAG, "Metadata extracted: $metadata")
                Result.success(metadata)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract metadata", e)
                Result.failure(
                    VideoProcessingException(
                        "Failed to extract metadata: ${e.message}",
                        e
                    )
                )
            }
        }

    /**
     * Apply filters to a video
     * This will be implemented in Phase 4 when we add filter support
     */
    actual suspend fun applyFilters(
        sourceUri: String,
        filters: List<VideoFilter>,
        outputPath: String,
        onProgress: ((Float) -> Unit)?
    ): Result<String> {
        Log.d(TAG, "applyFilters called but not yet implemented")
        return Result.failure(
            VideoProcessingException("Filter application not yet implemented - coming in Phase 4")
        )
    }

    /**
     * Generate a thumbnail from video at specific time
     */
    actual suspend fun generateThumbnail(
        uri: String,
        timeMs: Long,
        width: Int,
        height: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext Result.failure(
            VideoProcessingException("VideoProcessor not initialized")
        )

        try {
            Log.d(TAG, "Generating thumbnail at ${timeMs}ms")

            val retriever = MediaMetadataRetriever()

            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                retriever.setDataSource(ctx, Uri.parse(uri))
            } else {
                retriever.setDataSource(uri)
            }

            // Get frame at specified time (in microseconds)
            val timeUs = timeMs * 1000
            val bitmap = retriever.getFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )

            retriever.release()

            if (bitmap == null) {
                Log.e(TAG, "Failed to extract frame")
                return@withContext Result.failure(
                    VideoProcessingException("Failed to extract frame at ${timeMs}ms")
                )
            }

            // Scale bitmap if needed
            val scaledBitmap = if (bitmap.width != width || bitmap.height != height) {
                android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }

            // Save to file
            val thumbnailFile =
                File(ctx.cacheDir, "thumbnail_${timeMs}_${System.currentTimeMillis()}.jpg")
            thumbnailFile.outputStream().use { out ->
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
            }

            // Clean up
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            Log.d(TAG, "Thumbnail saved: ${thumbnailFile.absolutePath}")
            Result.success(thumbnailFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate thumbnail", e)
            Result.failure(VideoProcessingException("Failed to generate thumbnail", e))
        }
    }

    /**
     * Check if a video file is valid
     */
    actual suspend fun isValidVideo(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()

            if (uri.startsWith("content://") || uri.startsWith("file://")) {
                context?.let { retriever.setDataSource(it, Uri.parse(uri)) }
            } else {
                retriever.setDataSource(uri)
            }

            val hasVideo = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO
            ) == "yes"

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            retriever.release()

            // Valid if has video track and duration > 0
            val isValid = hasVideo && duration > 0
            Log.d(TAG, "Video validation: $isValid (hasVideo: $hasVideo, duration: ${duration}ms)")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Video validation failed", e)
            false
        }
    }

    /**
     * Cancel any ongoing processing operation
     */
    actual fun cancelProcessing() {
        Log.d(TAG, "Cancelling processing")
        currentTransformer?.cancel()
        currentTransformer = null
    }
}
