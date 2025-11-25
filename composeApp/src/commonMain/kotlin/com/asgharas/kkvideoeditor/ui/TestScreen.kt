@file:OptIn(ExperimentalTime::class)

package com.asgharas.kkvideoeditor.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.asgharas.kkvideoeditor.core.player.VideoPlayer
import com.asgharas.kkvideoeditor.core.processor.VideoProcessor
import com.asgharas.kkvideoeditor.data.model.Track
import com.asgharas.kkvideoeditor.data.model.VideoClip
import com.asgharas.kkvideoeditor.data.model.VideoFilter
import com.asgharas.kkvideoeditor.data.model.VideoMetadata
import com.asgharas.kkvideoeditor.data.model.VideoProject
import com.asgharas.kkvideoeditor.data.model.addClip
import com.asgharas.kkvideoeditor.data.model.allClips
import com.asgharas.kkvideoeditor.data.model.updateTrack
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Test screen for validating VideoPlayer and VideoProcessor functionality
 * This is a simple UI to verify the core components work correctly
 */

/**
 * Test screen for validating VideoPlayer and VideoProcessor functionality
 * This is a simple UI to verify the core components work correctly
 */
@Composable
fun VideoEditorTestScreen(
    videoPlayer: VideoPlayer,
    videoProcessor: VideoProcessor,
    selectedVideoPath: String,
    selectedVideoName: String,
    onPickVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var videoPath by remember(selectedVideoPath) { mutableStateOf(selectedVideoPath) }
    var videoName by remember(selectedVideoName) { mutableStateOf(selectedVideoName) }
    var outputLog by remember { mutableStateOf("Ready to test...\n") }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var metadata by remember { mutableStateOf<VideoMetadata?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Update position periodically when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            kotlinx.coroutines.delay(100)
            currentPosition = videoPlayer.getCurrentPosition()
            duration = videoPlayer.getDuration()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Header
        Text(
            text = "Video Editor Test UI",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Divider()

        // Video Picker Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selected Video",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = if (videoName.isEmpty()) "No video selected" else videoName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (videoPath.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (videoPath.isNotEmpty()) {
                    Text(
                        text = videoPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = onPickVideo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📁 Pick Video File")
                }
            }
        }

        // Test Data Models
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Test Data Models",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = {
                        outputLog += "\n--- Testing Data Models ---\n"

                        // Create a test project
                        val project = VideoProject(
                            name = "Test Project",
                            tracks = listOf(
                                Track(
                                    name = "Video Track 1",
                                    clips = listOf(
                                        VideoClip(
                                            sourceUri = videoPath,
                                            startTimeMs = 0L,
                                            endTimeMs = 5000L,
                                            filters = listOf(
                                                VideoFilter.Brightness(value = 0.2f),
                                                VideoFilter.Contrast(value = 0.1f),
                                                VideoFilter.Saturation(value = 0.3f)
                                            )
                                        ),
                                        VideoClip(
                                            sourceUri = videoPath,
                                            startTimeMs = 5000L,
                                            endTimeMs = 10000L
                                        )
                                    )
                                )
                            )
                        )

                        outputLog += "✓ Created project: ${project.name}\n"
                        outputLog += "✓ Tracks: ${project.tracks.size}\n"
                        outputLog += "✓ Total clips: ${project.allClips().size}\n"
                        outputLog += "✓ Project duration: ${project.duration}ms\n"
                        outputLog += "✓ Filters on first clip: ${project.tracks[0].clips[0].filters.size}\n"

                        // Test extension functions
                        val updatedProject = project.updateTrack(project.tracks[0].id) { track ->
                            track.addClip(
                                VideoClip(
                                    sourceUri = "/another/video.mp4",
                                    startTimeMs = 10000L,
                                    endTimeMs = 15000L
                                )
                            )
                        }

                        outputLog += "✓ Added clip via extension: ${updatedProject.allClips().size} total clips\n"
                        outputLog += "✓ Data models working correctly!\n"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Data Models")
                }
            }
        }

        // Test VideoProcessor
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Test VideoProcessor",
                    style = MaterialTheme.typography.titleMedium
                )

                // Get Metadata
                Button(
                    onClick = {
                        if (videoPath.isBlank()) {
                            outputLog += "\n❌ Please enter a video path first\n"
                            return@Button
                        }

                        scope.launch {
                            outputLog += "\n--- Getting Video Metadata ---\n"
                            val result = videoProcessor.getVideoMetadata(videoPath)

                            result.onSuccess { meta ->
                                metadata = meta
                                outputLog += "✓ Duration: ${meta.durationMs}ms (${meta.durationMs / 1000}s)\n"
                                outputLog += "✓ Resolution: ${meta.width}x${meta.height}\n"
                                outputLog += "✓ Aspect Ratio: ${meta.aspectRatio}\n"
                                outputLog += "✓ FPS: ${meta.fps}\n"
                                outputLog += "✓ Bitrate: ${meta.bitrate / 1000}kbps\n"
                                outputLog += "✓ File Size: ${meta.fileSize / 1024 / 1024}MB\n"
                                outputLog += "✓ Has Audio: ${meta.hasAudio}\n"
                                outputLog += "✓ MIME Type: ${meta.mimeType}\n"
                                outputLog += "✓ Portrait: ${meta.isPortrait}, Landscape: ${meta.isLandscape}\n"
                            }

                            result.onFailure { error ->
                                outputLog += "❌ Failed: ${error.message}\n"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get Video Metadata")
                }

                // Validate Video
                Button(
                    onClick = {
                        if (videoPath.isBlank()) {
                            outputLog += "\n❌ Please enter a video path first\n"
                            return@Button
                        }

                        scope.launch {
                            outputLog += "\n--- Validating Video ---\n"
                            val isValid = videoProcessor.isValidVideo(videoPath)

                            if (isValid) {
                                outputLog += "✓ Video is valid!\n"
                            } else {
                                outputLog += "❌ Video is invalid or corrupted\n"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Validate Video")
                }

                // Generate Thumbnail
                Button(
                    onClick = {
                        if (videoPath.isBlank()) {
                            outputLog += "\n❌ Please enter a video path first\n"
                            return@Button
                        }

                        scope.launch {
                            outputLog += "\n--- Generating Thumbnail ---\n"
                            val result = videoProcessor.generateThumbnail(
                                uri = videoPath,
                                timeMs = 1000L, // 1 second
                                width = 160,
                                height = 90
                            )

                            result.onSuccess { thumbPath ->
                                outputLog += "✓ Thumbnail saved: $thumbPath\n"
                            }

                            result.onFailure { error ->
                                outputLog += "❌ Failed: ${error.message}\n"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Thumbnail (1s)")
                }

                Button(
                    onClick = {
                        if (videoPath.isBlank()) {
                            outputLog += "\n❌ Please select a video first\n"
                            return@Button
                        }

                        scope.launch {
                            outputLog += "\n--- Trimming Video (2s to 7s) ---\n"
                            outputLog += "Source: $videoPath\n"
                            outputLog += "This may take 10-30 seconds...\n"

                            // Check video duration first
                            val metaResult = videoProcessor.getVideoMetadata(videoPath)
                            metaResult.onSuccess { meta ->
                                if (meta.durationMs < 7000L) {
                                    outputLog += "⚠️ Warning: Video is only ${meta.durationMs / 1000}s long\n"
                                    outputLog += "⚠️ Trimming to available duration...\n"
                                }
                            }

                            try {
                                val result = videoProcessor.trimVideo(
                                    sourceUri = videoPath,
                                    startMs = 2000L,
                                    endMs = 7000L,
                                    outputPath = "" // Let processor create path
                                )

                                result.onSuccess { output ->
                                    outputLog += "✓ Video trimmed successfully!\n"
                                    outputLog += "✓ Saved to: $output\n"
                                    outputLog += "✓ You can find it in your app's files directory\n"
                                }

                                result.onFailure { error ->
                                    outputLog += "❌ Trim failed: ${error.message}\n"
                                    outputLog += "❌ Stack trace: ${error.stackTraceToString()}\n"
                                }
                            } catch (e: Exception) {
                                outputLog += "❌ Exception during trim: ${e.message}\n"
                                outputLog += "❌ ${e.stackTraceToString()}\n"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trim Video (2s-7s)")
                }
            }
        }

        // Test VideoPlayer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Test VideoPlayer",
                    style = MaterialTheme.typography.titleMedium
                )

                // Player Info
                if (duration > 0) {
                    Text(
                        text = "Position: ${formatTime(currentPosition)} / ${formatTime(duration)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    LinearProgressIndicator(
                        progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Load Video
                Button(
                    onClick = {
                        if (videoPath.isBlank()) {
                            outputLog += "\n❌ Please enter a video path first\n"
                            return@Button
                        }

                        try {
                            outputLog += "\n--- Loading Video ---\n"
                            videoPlayer.loadVideo(videoPath)
                            duration = videoPlayer.getDuration()
                            outputLog += "✓ Video loaded: ${formatTime(duration)}\n"
                        } catch (e: Exception) {
                            outputLog += "❌ Load failed: ${e.message}\n"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load Video")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play/Pause
                    Button(
                        onClick = {
                            if (isPlaying) {
                                videoPlayer.pause()
                                isPlaying = false
                                outputLog += "⏸ Paused\n"
                            } else {
                                videoPlayer.play()
                                isPlaying = true
                                outputLog += "▶ Playing\n"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }

                    // Stop
                    Button(
                        onClick = {
                            videoPlayer.pause()
                            videoPlayer.seekTo(0)
                            isPlaying = false
                            currentPosition = 0
                            outputLog += "⏹ Stopped\n"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                }

                // Seek Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newPos = (currentPosition - 5000L).coerceAtLeast(0)
                            videoPlayer.seekTo(newPos)
                            currentPosition = newPos
                            outputLog += "⏪ Seek -5s\n"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-5s")
                    }

                    Button(
                        onClick = {
                            val newPos = (currentPosition + 5000L).coerceAtMost(duration)
                            videoPlayer.seekTo(newPos)
                            currentPosition = newPos
                            outputLog += "⏩ Seek +5s\n"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+5s")
                    }
                }

                // Volume Control
                var volume by remember { mutableStateOf(1.0f) }

                Text("Volume: ${(volume * 100).toInt()}%")
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        videoPlayer.setVolume(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Set Callbacks
                DisposableEffect(Unit) {
                    videoPlayer.setOnCompletionListener {
                        isPlaying = false
                        outputLog += "✓ Playback completed\n"
                    }

                    videoPlayer.setOnErrorListener { error ->
                        outputLog += "❌ Player error: $error\n"
                    }

                    onDispose {
                        videoPlayer.release()
                    }
                }
            }
        }

        // Metadata Display
        metadata?.let { meta ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Video Info",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Divider()
                    InfoRow("Duration", "${meta.durationMs / 1000}s")
                    InfoRow("Resolution", "${meta.width}x${meta.height}")
                    InfoRow("Aspect Ratio", meta.aspectRatio)
                    InfoRow("FPS", meta.fps.toString())
                    InfoRow("Bitrate", "${meta.bitrate / 1000}kbps")
                    InfoRow("File Size", "${meta.fileSize / 1024 / 1024}MB")
                    InfoRow("Has Audio", if (meta.hasAudio) "Yes" else "No")
                }
            }
        }

        // Output Log
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output Log",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Green
                    )

                    TextButton(onClick = { outputLog = "Log cleared.\n" }) {
                        Text("Clear", color = Color.Red)
                    }
                }

                Text(
                    text = outputLog,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}



private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
}