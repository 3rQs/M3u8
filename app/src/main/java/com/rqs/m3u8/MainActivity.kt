package com.rqs.m3u8

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rqs.m3u8.core.player.HlsMediaSourceFactory

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var urlInput: EditText
    private lateinit var cacheCheckBox: CheckBox
    private lateinit var statusView: TextView
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        urlInput = EditText(this).apply {
            hint = "HTTPS HLS URL (.m3u8)"
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        root.addView(urlInput, matchWidth())

        cacheCheckBox = CheckBox(this).apply {
            text = "อนุญาตให้แคชวิดีโอนี้ (สูงสุด 500 MB)"
            isChecked = false
        }
        root.addView(cacheCheckBox, matchWidth())

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        controls.addView(Button(this).apply {
            text = "Play"
            setOnClickListener { playStream() }
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        controls.addView(Button(this).apply {
            text = "Stop"
            setOnClickListener {
                player?.stop()
                statusView.text = "Stopped"
            }
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        root.addView(controls, matchWidth())

        statusView = TextView(this).apply {
            text = "Ready. Paste an authorized HTTPS HLS URL."
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(statusView, matchWidth())

        playerView = PlayerView(this).apply {
            useController = true
        }
        root.addView(
            playerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(240)
            )
        )

        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    statusView.text = when (playbackState) {
                        Player.STATE_BUFFERING -> "Buffering…"
                        Player.STATE_READY -> "Playing / Ready"
                        Player.STATE_ENDED -> "Playback ended"
                        Player.STATE_IDLE -> "Idle"
                        else -> statusView.text
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // Do not log the source URL: it may contain signed credentials.
                    statusView.text = "Playback error: ${error.errorCodeName}"
                }
            })
        }
    }

    override fun onStop() {
        playerView.player = null
        player?.release()
        player = null
        super.onStop()
    }

    private fun playStream() {
        val url = urlInput.text.toString().trim()
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        if (uri == null || !uri.scheme.equals("https", ignoreCase = true) ||
            uri.host.isNullOrBlank()
        ) {
            Toast.makeText(this, "กรุณาใส่ URL HTTPS ที่ถูกต้อง", Toast.LENGTH_SHORT).show()
            return
        }

        val activePlayer = player
        if (activePlayer == null) {
            statusView.text = "Player is not ready. Try again."
            return
        }

        val useCache = cacheCheckBox.isChecked
        statusView.text = if (useCache) {
            "Preparing HLS (disk cache enabled, max 500 MB)…"
        } else {
            "Preparing HLS (cache disabled)…"
        }

        runCatching {
            HlsMediaSourceFactory.create(this, url, useCache)
        }.onSuccess { mediaSource ->
            activePlayer.setMediaSource(mediaSource)
            activePlayer.prepare()
            activePlayer.playWhenReady = true
        }.onFailure {
            statusView.text = "Could not prepare this HLS source."
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
