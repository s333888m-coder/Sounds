package com.oneui7.gallery

import android.app.Application
import android.content.Intent
import java.io.PrintWriter
import java.io.StringWriter
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

class GalleryApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val intent = Intent(this, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("trace", sw.toString().take(20000))
                }
                startActivity(intent)
                Thread.sleep(600)
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("fhd_gallery_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(true)
            .crossfade(150)
            .build()
    }
}
