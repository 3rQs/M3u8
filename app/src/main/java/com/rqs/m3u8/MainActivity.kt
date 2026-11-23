package com.rqs.m3u8

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = TextView(this).apply {
            text = "M3u8\n\nProject scaffold is ready. HLS playback and cache are the next implementation steps."
            textSize = 18f
            setPadding(32, 48, 32, 32)
        }
        setContentView(message)
    }
}
