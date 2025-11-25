package com.asgharas.kkvideoeditor.core.processor

import com.asgharas.kkvideoeditor.data.model.VideoFilter
import com.asgharas.kkvideoeditor.data.model.VideoMetadata

actual class VideoProcessor actual constructor() {
    actual suspend fun trimVideo(
        sourceUri: String,
        startMs: Long,
        endMs: Long,
        outputPath: String
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun getVideoMetadata(uri: String): Result<VideoMetadata> {
        TODO("Not yet implemented")
    }

    actual suspend fun applyFilters(
        sourceUri: String,
        filters: List<VideoFilter>,
        outputPath: String,
        onProgress: ((Float) -> Unit)?
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun generateThumbnail(
        uri: String,
        timeMs: Long,
        width: Int,
        height: Int
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun isValidVideo(uri: String): Boolean {
        TODO("Not yet implemented")
    }

    actual fun cancelProcessing() {
    }
}