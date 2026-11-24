package com.oneui7.gallery

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra("trace") ?: "No error details."

        val title = TextView(this).apply {
            text = "Gallery crashed - send a screenshot of this screen"
            setTextColor(Color.WHITE)
            textSize = 16f
        }
        val copy = Button(this).apply {
            text = "Copy error"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("crash", trace))
            }
        }
        val body = TextView(this).apply {
            text = trace
            setTextColor(Color.WHITE)
            textSize = 11f
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply { addView(body) }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            fitsSystemWindows = true
            setPadding(24, 24, 24, 24)
            addView(title)
            addView(copy)
            addView(
                scroll,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            )
        }
        setContentView(root)
    }
}
