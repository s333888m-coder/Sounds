package com.oneui7.gallery.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.oneui7.gallery.model.Album
import com.oneui7.gallery.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    suspend fun loadAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val imagesDeferred = async { queryMediaStore(isVideo = false) }
        val videosDeferred = async { queryMediaStore(isVideo = true) }

        val allMedia = (imagesDeferred.await() + videosDeferred.await())
            .sortedByDescending { it.dateAdded }

        if (allMedia.isEmpty()) return@withContext emptyList()

        val albums = mutableListOf<Album>()
        albums.add(Album(name = "Recent", items = allMedia, isEssential = true))

        val videosOnly = allMedia.filter { it.isVideo }
        if (videosOnly.isNotEmpty()) {
            albums.add(Album(name = "Videos", items = videosOnly, isEssential = true))
        }

        val grouped = allMedia.groupBy { it.albumName }
        grouped.forEach { (albumName, items) ->
            if (albumName.isNotBlank()) {
                albums.add(Album(name = albumName, items = items, isEssential = true))
            }
        }

        albums
    }

    private fun queryMediaStore(isVideo: Boolean): List<MediaItem> {
        val list = mutableListOf<MediaItem>()
        val collection: Uri = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE
        ).apply {
            if (isVideo) add(MediaStore.Video.VideoColumns.DURATION)
        }.toTypedArray()

        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val bucketCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val durationCol = if (isVideo) cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION) else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Media_$id"
                    val bucket = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "Camera" else "Camera"
                    val dateAdded = cursor.getLong(dateCol)
                    val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                    val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val contentUri = ContentUris.withAppendedId(collection, id)

                    list.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            albumName = bucket,
                            isVideo = isVideo,
                            durationMs = duration,
                            dateAdded = dateAdded,
                            width = width,
                            height = height,
                            sizeBytes = size
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return list
    }
}
