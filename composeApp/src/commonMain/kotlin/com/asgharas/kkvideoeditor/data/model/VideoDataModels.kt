@file:OptIn(ExperimentalTime::class)

package com.asgharas.kkvideoeditor.data.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

/**
 * Represents a complete video editing project
 * Contains multiple tracks that can hold video clips
 */
data class VideoProject(
    val id: String = generateId(),
    val name: String,
    val tracks: List<Track> = listOf(Track()), // Start with one empty track
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val modifiedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val isLocked: Boolean = false, // Prevent editing when locked
) {
    /**
     * Calculate total project duration based on the longest track
     */
    val duration: Long
        get() = tracks.maxOfOrNull { track ->
            track.clips.maxOfOrNull { it.endTimeMs } ?: 0L
        } ?: 0L
}

/**
 * Represents a single track in the timeline
 * Can contain multiple video clips arranged sequentially or overlapping
 */
data class Track(
    val id: String = generateId(),
    val name: String = "Video Track",
    val clips: List<VideoClip> = emptyList(),
    val isLocked: Boolean = false, // Prevent editing when locked
    val isVisible: Boolean = true, // Hide/show track in preview
    val opacity: Float = 1.0f // Track-level opacity (0.0 to 1.0)
) {
    /**
     * Get the duration of this track (longest clip end time)
     */
    val duration: Long
        get() = clips.maxOfOrNull { it.endTimeMs } ?: 0L
}

/**
 * Represents a single video clip on the timeline
 * Contains source reference, position, trimming info, and applied effects
 */
data class VideoClip(
    val id: String = generateId(),
    val sourceUri: String, // Path to the source video file
    val startTimeMs: Long, // Position on timeline where clip starts
    val endTimeMs: Long, // Position on timeline where clip ends
    val trimStartMs: Long = 0L, // Trim from beginning of source video
    val trimEndMs: Long = 0L, // Trim from end of source video
    val filters: List<VideoFilter> = emptyList(),
    val volume: Float = 1.0f, // Clip volume (0.0 to 1.0)
    val speed: Float = 1.0f // Playback speed (0.25x to 4.0x, 1.0 = normal)
) {
    /**
     * Calculate the actual duration this clip plays on the timeline
     */
    val playbackDuration: Long
        get() = endTimeMs - startTimeMs

    /**
     * Get the source video duration being used (after trimming)
     */
    val sourceDuration: Long
        get() = (endTimeMs - startTimeMs) / speed.toLong()
}

/**
 * Base sealed class for all video filters
 * Each filter type extends this with specific parameters
 */
sealed class VideoFilter {
    /**
     * Unique identifier for this filter instance
     */
    abstract val id: String

    /**
     * Brightness adjustment filter
     * @param value Range: -1.0 (darkest) to 1.0 (brightest), 0.0 = no change
     */
    data class Brightness(
        override val id: String = generateId(),
        val value: Float // -1.0 to 1.0
    ) : VideoFilter() {
        init {
            require(value in -1.0f..1.0f) { "Brightness must be between -1.0 and 1.0" }
        }
    }

    /**
     * Contrast adjustment filter
     * @param value Range: -1.0 (lowest contrast) to 1.0 (highest contrast), 0.0 = no change
     */
    data class Contrast(
        override val id: String = generateId(),
        val value: Float // -1.0 to 1.0
    ) : VideoFilter() {
        init {
            require(value in -1.0f..1.0f) { "Contrast must be between -1.0 and 1.0" }
        }
    }

    /**
     * Saturation adjustment filter
     * @param value Range: -1.0 (grayscale) to 1.0 (highly saturated), 0.0 = no change
     */
    data class Saturation(
        override val id: String = generateId(),
        val value: Float // -1.0 to 1.0
    ) : VideoFilter() {
        init {
            require(value in -1.0f..1.0f) { "Saturation must be between -1.0 and 1.0" }
        }
    }

    // Placeholder for future color filters (Phase 4 later)
    // Uncomment and implement when ready:

    // data class Grayscale(override val id: String = generateId()) : VideoFilter()

    // data class Sepia(
    //     override val id: String = generateId(),
    //     val intensity: Float = 1.0f // 0.0 to 1.0
    // ) : VideoFilter()

    // data class Vintage(
    //     override val id: String = generateId(),
    //     val intensity: Float = 1.0f // 0.0 to 1.0
    // ) : VideoFilter()
}

/**
 * Metadata extracted from a video file
 * Used for validation and UI display
 */
data class VideoMetadata(
    val durationMs: Long, // Total duration in milliseconds
    val width: Int, // Video width in pixels
    val height: Int, // Video height in pixels
    val fps: Float, // Frames per second
    val bitrate: Long = 0L, // Video bitrate in bits/second
    val hasAudio: Boolean = true, // Whether video contains audio track
    val fileSize: Long = 0L, // File size in bytes
    val mimeType: String = "video/mp4" // MIME type of the video
) {
    /**
     * Get aspect ratio as a simplified string (e.g., "16:9", "4:3", "9:16")
     */
    val aspectRatio: String
        get() = calculateAspectRatio(width, height)

    /**
     * Check if video is portrait orientation
     */
    val isPortrait: Boolean
        get() = height > width

    /**
     * Check if video is landscape orientation
     */
    val isLandscape: Boolean
        get() = width > height

    private fun calculateAspectRatio(width: Int, height: Int): String {
        val gcd = gcd(width, height)
        val ratioW = width / gcd
        val ratioH = height / gcd
        return "$ratioW:$ratioH"
    }

    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }
}

/**
 * Enum for supported video export formats
 */
enum class VideoFormat {
    MP4,
    MOV,
    // Future formats:
    // WEBM,
    // AVI
}

/**
 * Enum for export resolution presets
 */
enum class Resolution(val width: Int, val height: Int, val label: String) {
    SD_480P(640, 480, "480p SD"),
    HD_720P(1280, 720, "720p HD"),
    FULL_HD_1080P(1920, 1080, "1080p Full HD"),
    UHD_4K(3840, 2160, "4K UHD");

    /**
     * Get total pixel count
     */
    val pixels: Int
        get() = width * height
}

/**
 * Export settings for final video rendering
 */
data class ExportSettings(
    val resolution: Resolution = Resolution.FULL_HD_1080P,
    val frameRate: Int = 30, // fps
    val bitrate: Int = 8_000_000, // bits per second (8 Mbps default)
    val format: VideoFormat = VideoFormat.MP4,
    val quality: ExportQuality = ExportQuality.HIGH
)

/**
 * Export quality presets
 */
enum class ExportQuality(val bitrateMultiplier: Float) {
    LOW(0.5f),      // 50% of base bitrate
    MEDIUM(0.75f),  // 75% of base bitrate
    HIGH(1.0f),     // 100% of base bitrate
    ULTRA(1.5f)     // 150% of base bitrate
}

/**
 * Generate a unique ID for entities
 * Using timestamp + random for uniqueness
 */
@OptIn(ExperimentalUuidApi::class)
private fun generateId(): String {
    return Uuid.random().toString()
}

// Extension functions for common operations

/**
 * Add a clip to a track at a specific position
 */
fun Track.addClip(clip: VideoClip): Track {
    return copy(clips = clips + clip)
}

/**
 * Remove a clip from a track by ID
 */
fun Track.removeClip(clipId: String): Track {
    return copy(clips = clips.filter { it.id != clipId })
}

/**
 * Update a clip in a track
 */
fun Track.updateClip(clipId: String, updater: (VideoClip) -> VideoClip): Track {
    return copy(clips = clips.map { clip ->
        if (clip.id == clipId) updater(clip) else clip
    })
}

/**
 * Add a filter to a clip
 */
fun VideoClip.addFilter(filter: VideoFilter): VideoClip {
    return copy(filters = filters + filter)
}

/**
 * Remove a filter from a clip by ID
 */
fun VideoClip.removeFilter(filterId: String): VideoClip {
    return copy(filters = filters.filter { it.id != filterId })
}

/**
 * Update a filter in a clip
 */
fun VideoClip.updateFilter(filterId: String, updater: (VideoFilter) -> VideoFilter): VideoClip {
    return copy(filters = filters.map { filter ->
        if (filter.id == filterId) updater(filter) else filter
    })
}

/**
 * Get all clips across all tracks in a project
 */
fun VideoProject.allClips(): List<VideoClip> {
    return tracks.flatMap { it.clips }
}

/**
 * Add a track to a project
 */
fun VideoProject.addTrack(track: Track): VideoProject {
    return copy(tracks = tracks + track)
}

/**
 * Remove a track from a project by ID
 */
fun VideoProject.removeTrack(trackId: String): VideoProject {
    return copy(tracks = tracks.filter { it.id != trackId })
}

/**
 * Update a track in a project
 */
fun VideoProject.updateTrack(trackId: String, updater: (Track) -> Track): VideoProject {
    return copy(tracks = tracks.map { track ->
        if (track.id == trackId) updater(track) else track
    })
}
