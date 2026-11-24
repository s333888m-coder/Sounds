package com.oneui7.gallery.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val albumName: String,
    val isVideo: Boolean,
    val durationMs: Long = 0L,
    val dateAdded: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun formattedResolution(): String {
        return if (width > 0 && height > 0) "${width} x ${height} FHD" else "Full HD"
    }

    fun formattedSize(): String {
        val mb = sizeBytes / (1024.0 * 1024.0)
        return if (mb >= 1.0) String.format("%.1f MB", mb) else "${sizeBytes / 1024} KB"
    }
}

@Immutable
data class Album(
    val name: String,
    val items: List<MediaItem>,
    val isEssential: Boolean = true
) {
    val coverUri: Uri? get() = items.firstOrNull()?.uri
    val photoCount: Int get() = items.count { !it.isVideo }
    val videoCount: Int get() = items.count { it.isVideo }
    val totalCount: Int get() = items.size
}
