package com.asgharas.kkvideoeditor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.asgharas.kkvideoeditor.core.player.VideoPlayer
import com.asgharas.kkvideoeditor.core.processor.VideoProcessor

class MainActivity : ComponentActivity() {

    private val videoPlayer = VideoPlayer()
    private val videoProcessor = VideoProcessor()


    private var selectedVideoPath by mutableStateOf("")
    private var selectedVideoName by mutableStateOf("")

    // Video file picker launcher
    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedVideo(it) }
    }

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeComponents()
        } else {
            // Handle permission denial
            // You might want to show a dialog explaining why permissions are needed
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestPermissions()

        setContent {
            App(videoPlayer, videoProcessor, selectedVideoPath, selectedVideoName, ::launchVideoPicker)
        }
    }

    private fun launchVideoPicker() {
        videoPickerLauncher.launch("video/*")
    }

    private fun handleSelectedVideo(uri: Uri) {
        // Get the actual file path from URI
        val path = getPathFromUri(uri)

        // Get the display name
        val name = getFileNameFromUri(uri)

        if (path != null) {
            selectedVideoPath = path
            selectedVideoName = name ?: "Unknown video"
        } else {
            // If we can't get a file path, use the URI string
            // This works with content:// URIs
            selectedVideoPath = uri.toString()
            selectedVideoName = name ?: "Selected video"
        }
    }

    /**
     * Get the display name of a file from its URI
     */
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }

        return fileName
    }

    /**
     * Try to get the actual file path from a URI
     * Works for file:// URIs and some content:// URIs
     */
    private fun getPathFromUri(uri: Uri): String? {
        return when (uri.scheme) {
            "file" -> uri.path
            "content" -> {
                // For content URIs, try to get the real path
                // This works for some cases but not all
                try {
                    val projection = arrayOf(android.provider.MediaStore.Video.Media.DATA)
                    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        val columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA)
                        if (cursor.moveToFirst()) {
                            cursor.getString(columnIndex)
                        } else {
                            null
                        }
                    } ?: uri.toString() // Fallback to URI string
                } catch (e: Exception) {
                    // If we can't get the path, use the URI string
                    uri.toString()
                }
            }
            else -> uri.toString()
        }
    }



    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // Storage permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                // Write permission only needed for Android 9 and below
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            // Permissions already granted
            initializeComponents()
        }
    }

    private fun initializeComponents() {
        // Initialize VideoPlayer with context
        videoPlayer.initialize(this)

        // Initialize VideoProcessor with context
        videoProcessor.initialize(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        videoPlayer.release()
        videoProcessor.cancelProcessing()
    }

    override fun onPause() {
        super.onPause()
        // Pause video when app goes to background
        videoPlayer.pause()
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}