package com.oneui7.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.oneui7.gallery.ui.GalleryViewModel
import com.oneui7.gallery.ui.screens.GalleryAppScreen
import com.oneui7.gallery.ui.theme.OneUI7GalleryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val modes = display?.supportedModes
            val highestMode = modes?.maxByOrNull { it.refreshRate }
            if (highestMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = highestMode.modeId
                window.attributes = params
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        val initialAlbum = intent?.getStringExtra("OPEN_ALBUM")

        setContent {
            GalleryRoot(initialAlbum = initialAlbum, viewModel = viewModel)
        }
    }
}

@Composable
private fun GalleryRoot(initialAlbum: String?, viewModel: GalleryViewModel) {
    OneUI7GalleryTheme {
        val context = androidx.compose.ui.platform.LocalContext.current
        val permissions = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        var hasPermission by remember {
            mutableStateOf(
                permissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
            )
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            hasPermission = result.values.all { it }
            if (hasPermission) {
                viewModel.refreshMedia(initialAlbum)
            }
        }

        LaunchedEffect(Unit) {
            if (hasPermission) {
                viewModel.refreshMedia(initialAlbum)
            } else {
                launcher.launch(permissions)
            }
        }

        GalleryAppScreen(
            viewModel = viewModel,
            hasPermission = hasPermission,
            onRequestPermission = { launcher.launch(permissions) }
        )
    }
}
