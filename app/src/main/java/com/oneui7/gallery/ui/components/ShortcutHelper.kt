package com.oneui7.gallery.ui.components

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.oneui7.gallery.MainActivity

object ShortcutHelper {

    fun pinAlbumShortcut(context: Context, albumName: String) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcutIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("OPEN_ALBUM", albumName)
            }

            val iconBitmap = createOneUIAlbumBadgeBitmap(albumName)
            val shortcut = ShortcutInfoCompat.Builder(context, "album_$albumName")
                .setShortLabel(albumName)
                .setLongLabel("$albumName Album")
                .setIcon(IconCompat.createWithBitmap(iconBitmap))
                .setIntent(shortcutIntent)
                .build()

            val pinnedShortcutCallbackIntent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val successCallback = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, flags)

            ShortcutManagerCompat.requestPinShortcut(context, shortcut, successCallback.intentSender)
            Toast.makeText(context, "$albumName added to Home screen.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Enable 'Home screen shortcuts' in App Info > Other permissions.", Toast.LENGTH_LONG).show()
        }
    }

    private fun createOneUIAlbumBadgeBitmap(title: String): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val gradient = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            intArrayOf(0xFFFF3B5C.toInt(), 0xFFFF6B3D.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = gradient
        }
        canvas.drawRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), 64f, 64f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 96f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val letter = title.firstOrNull()?.uppercase() ?: "G"
        canvas.drawText(letter, size / 2f, size / 2f + 32f, textPaint)
        return bitmap
    }
}
