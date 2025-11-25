package com.asgharas.kkvideoeditor.core.exporter

import com.asgharas.kkvideoeditor.data.model.ExportSettings
import com.asgharas.kkvideoeditor.data.model.VideoProject

/**
 * Platform-independent video exporter interface
 * Handles final composition and export of complete video projects
 *
 * Android implementation uses Media3 Transformer for complex compositions
 * iOS implementation uses AVAssetExportSession / AVAssetWriter
 */
expect class VideoExporter() {

    /**
     * Export a complete video project to a file
     * Combines all tracks, applies all filters, transitions, and effects
     *
     * @param project The complete video project to export
     * @param outputPath Path where the exported video should be saved
     * @param settings Export configuration (resolution, quality, format, etc.)
     * @param onProgress Callback invoked with progress updates (0.0 to 1.0)
     * @return Result containing output path on success, or error on failure
     */
    suspend fun exportProject(
        project: VideoProject,
        outputPath: String,
        settings: ExportSettings,
        onProgress: ((Float) -> Unit)? = null
    ): Result<ExportResult>

    /**
     * Estimate the file size of an export before starting
     * Useful for showing user estimated output size
     *
     * @param project The video project to estimate
     * @param settings Export settings to use for estimation
     * @return Estimated file size in bytes
     */
    suspend fun estimateFileSize(
        project: VideoProject,
        settings: ExportSettings
    ): Long

    /**
     * Estimate the time required to export a project
     * Based on project duration and device capabilities
     *
     * @param project The video project to estimate
     * @param settings Export settings to use for estimation
     * @return Estimated export time in milliseconds
     */
    suspend fun estimateExportTime(
        project: VideoProject,
        settings: ExportSettings
    ): Long

    /**
     * Cancel an ongoing export operation
     * Safe to call even if no export is in progress
     * Cleans up temporary files
     */
    fun cancelExport()

    /**
     * Check if an export operation is currently in progress
     *
     * @return true if export is running, false otherwise
     */
    fun isExporting(): Boolean
}

/**
 * Result of a video export operation
 * Contains metadata about the exported file
 */
data class ExportResult(
    val outputPath: String,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val resolution: String, // e.g., "1920x1080"
    val exportTimeMs: Long  // How long the export took
)

/**
 * Exception thrown during export operations
 */
class ExportException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Export status for tracking export progress
 */
enum class ExportStatus {
    IDLE,           // No export in progress
    PREPARING,      // Setting up export pipeline
    EXPORTING,      // Actively exporting
    FINALIZING,     // Finishing up (writing metadata, etc.)
    COMPLETED,      // Export completed successfully
    CANCELLED,      // Export was cancelled by user
    FAILED          // Export failed with error
}

/**
 * Export progress information
 * Provides detailed status during export
 */
data class ExportProgress(
    val status: ExportStatus,
    val progress: Float, // 0.0 to 1.0
    val currentFrame: Long,
    val totalFrames: Long,
    val elapsedTimeMs: Long,
    val estimatedRemainingMs: Long
)