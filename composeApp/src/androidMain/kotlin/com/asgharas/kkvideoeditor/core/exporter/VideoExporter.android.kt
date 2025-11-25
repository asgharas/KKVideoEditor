package com.asgharas.kkvideoeditor.core.exporter

import com.asgharas.kkvideoeditor.data.model.ExportSettings
import com.asgharas.kkvideoeditor.data.model.VideoProject

actual class VideoExporter actual constructor() {
    actual suspend fun exportProject(
        project: VideoProject,
        outputPath: String,
        settings: ExportSettings,
        onProgress: ((Float) -> Unit)?
    ): Result<ExportResult> {
        TODO("Not yet implemented")
    }

    actual suspend fun estimateFileSize(
        project: VideoProject,
        settings: ExportSettings
    ): Long {
        TODO("Not yet implemented")
    }

    actual suspend fun estimateExportTime(
        project: VideoProject,
        settings: ExportSettings
    ): Long {
        TODO("Not yet implemented")
    }

    actual fun cancelExport() {
    }

    actual fun isExporting(): Boolean {
        TODO("Not yet implemented")
    }
}