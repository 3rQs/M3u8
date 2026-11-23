package com.rqs.m3u8

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.ui.PlayerView
import com.rqs.m3u8.core.download.HlsDownloadManagerProvider
import com.rqs.m3u8.core.download.M3u8Downloads
import com.rqs.m3u8.core.player.HlsMediaSourceFactory
import java.util.concurrent.Executors

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var urlInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var cacheCheckBox: CheckBox
    private lateinit var statusView: TextView
    private lateinit var downloadRows: LinearLayout
    private var player: ExoPlayer? = null
    private var downloadManager: DownloadManager? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val queryExecutor = Executors.newSingleThreadExecutor()
    private val refreshDownloadsRunnable = object : Runnable {
        override fun run() {
            refreshDownloads()
            mainHandler.postDelayed(this, 1500L)
        }
    }

    private val downloadListener = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            refreshDownloads()
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            refreshDownloads()
        }

        override fun onInitialized(downloadManager: DownloadManager) {
            refreshDownloads()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val scrollView = ScrollView(this).apply {
            addView(content)
        }

        urlInput = EditText(this).apply {
            hint = "HTTPS HLS URL (.m3u8)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }
        content.addView(urlInput, matchWidth())

        cacheCheckBox = CheckBox(this).apply {
            text = "อนุญาตให้แคชวิดีโอระหว่างเล่น (สูงสุด 500 MB)"
            isChecked = false
        }
        content.addView(cacheCheckBox, matchWidth())

        val playbackControls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        playbackControls.addView(Button(this).apply {
            text = "Play"
            setOnClickListener { playStream() }
        }, weightedButton())
        playbackControls.addView(Button(this).apply {
            text = "Stop"
            setOnClickListener {
                player?.stop()
                statusView.text = "Stopped"
            }
        }, weightedButton())
        content.addView(playbackControls, matchWidth())

        statusView = TextView(this).apply {
            text = "Ready. Paste an authorized HTTPS HLS URL."
            setPadding(0, dp(8), 0, dp(8))
        }
        content.addView(statusView, matchWidth())

        playerView = PlayerView(this).apply {
            useController = true
        }
        content.addView(
            playerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(220)
            )
        )

        content.addView(sectionTitle("Offline downloads"))
        titleInput = EditText(this).apply {
            hint = "ชื่อวิดีโอ (ไม่บังคับ)"
            setSingleLine(true)
        }
        content.addView(titleInput, matchWidth())

        content.addView(Button(this).apply {
            text = "Download HLS"
            setOnClickListener { enqueueDownload() }
        }, matchWidth())

        val downloadControls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        downloadControls.addView(Button(this).apply {
            text = "Pause all"
            setOnClickListener { M3u8Downloads.pauseAll(this@MainActivity) }
        }, weightedButton())
        downloadControls.addView(Button(this).apply {
            text = "Resume all"
            setOnClickListener { M3u8Downloads.resumeAll(this@MainActivity) }
        }, weightedButton())
        content.addView(downloadControls, matchWidth())

        downloadRows = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(downloadRows, matchWidth())

        setContentView(scrollView)
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

        downloadManager = HlsDownloadManagerProvider.getManager(this).also {
            it.addListener(downloadListener)
        }
        refreshDownloads()
        mainHandler.post(refreshDownloadsRunnable)
    }

    override fun onStop() {
        mainHandler.removeCallbacks(refreshDownloadsRunnable)
        downloadManager?.removeListener(downloadListener)
        downloadManager = null
        playerView.player = null
        player?.release()
        player = null
        super.onStop()
    }

    override fun onDestroy() {
        queryExecutor.shutdown()
        super.onDestroy()
    }

    private fun playStream() {
        val url = urlInput.text.toString().trim()
        if (!isValidHttpsUrl(url)) {
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
            "Preparing HLS (playback cache enabled, max 500 MB)…"
        } else {
            "Preparing HLS (playback cache disabled)…"
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

    private fun enqueueDownload() {
        val url = urlInput.text.toString().trim()
        if (!isValidHttpsUrl(url)) {
            Toast.makeText(this, "กรุณาใส่ URL HTTPS ที่ถูกต้อง", Toast.LENGTH_SHORT).show()
            return
        }

        val title = titleInput.text.toString().trim().ifBlank {
            Uri.parse(url).host ?: "HLS download"
        }
        runCatching {
            M3u8Downloads.enqueue(this, url, title)
        }.onSuccess {
            statusView.text = "Added to offline download queue."
            refreshDownloads()
        }.onFailure {
            statusView.text = "Could not add this download."
            Toast.makeText(this, "เพิ่มรายการดาวน์โหลดไม่สำเร็จ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDownloads() {
        val index = downloadManager?.downloadIndex ?: return
        queryExecutor.execute {
            val downloads = runCatching { readAllDownloads(index) }.getOrElse { emptyList() }
            runOnUiThread {
                if (!isFinishing && !isDestroyed) renderDownloads(downloads)
            }
        }
    }

    private fun readAllDownloads(index: DownloadIndex): List<Download> {
        val result = mutableListOf<Download>()
        var cursor: DownloadCursor? = null
        try {
            cursor = index.getDownloads()
            while (cursor.moveToNext()) {
                result.add(cursor.download)
            }
        } finally {
            cursor?.close()
        }
        return result.sortedByDescending { it.startTimeMs }
    }

    private fun renderDownloads(downloads: List<Download>) {
        downloadRows.removeAllViews()
        if (downloads.isEmpty()) {
            downloadRows.addView(TextView(this).apply {
                text = "ยังไม่มีรายการดาวน์โหลด"
                setPadding(0, dp(8), 0, dp(8))
            })
            return
        }

        downloads.forEach { download ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, dp(10))
            }
            val title = String(download.request.data, Charsets.UTF_8)
                .ifBlank { download.request.uri.lastPathSegment ?: "HLS download" }
            card.addView(TextView(this).apply {
                text = title
                textSize = 16f
            }, matchWidth())

            val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                val percent = download.percentDownloaded
                if (percent >= 0f) {
                    isIndeterminate = false
                    this.progress = percent.toInt().coerceIn(0, 100)
                } else {
                    isIndeterminate = download.state == Download.STATE_DOWNLOADING
                }
            }
            card.addView(progress, matchWidth())

            card.addView(TextView(this).apply {
                val percent = download.percentDownloaded
                val progressText = if (percent >= 0f) "${percent.toInt()}%" else "—"
                text = "${downloadStateText(download)} • $progressText • ${formatBytes(download.bytesDownloaded)}"
                setPadding(0, dp(4), 0, dp(4))
            }, matchWidth())

            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            if (download.state != Download.STATE_COMPLETED &&
                download.state != Download.STATE_FAILED &&
                download.state != Download.STATE_REMOVING
            ) {
                val paused = download.stopReason != Download.STOP_REASON_NONE
                actions.addView(Button(this).apply {
                    text = if (paused) "Resume" else "Pause"
                    setOnClickListener {
                        if (paused) {
                            M3u8Downloads.resume(this@MainActivity, download.request.id)
                        } else {
                            M3u8Downloads.pause(this@MainActivity, download.request.id)
                        }
                        refreshDownloads()
                    }
                }, weightedButton())
            }
            actions.addView(Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    M3u8Downloads.remove(this@MainActivity, download.request.id)
                    refreshDownloads()
                }
            }, weightedButton())
            card.addView(actions, matchWidth())
            downloadRows.addView(card, matchWidth())
            downloadRows.addView(View(this).apply {
                setBackgroundColor(0xFFDDDDDD.toInt())
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ))
        }
    }

    private fun downloadStateText(download: Download): String = when {
        download.state == Download.STATE_COMPLETED -> "Completed (offline file saved)"
        download.state == Download.STATE_FAILED -> "Failed"
        download.state == Download.STATE_DOWNLOADING -> "Downloading"
        download.state == Download.STATE_QUEUED && download.stopReason != Download.STOP_REASON_NONE -> "Paused"
        download.state == Download.STATE_QUEUED -> "Queued"
        download.state == Download.STATE_REMOVING -> "Removing"
        download.state == Download.STATE_RESTARTING -> "Restarting"
        download.state == Download.STATE_STOPPED -> "Stopped"
        else -> "Waiting"
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "${bytes} B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        return String.format("%.1f MB", kb / 1024.0)
    }

    private fun isValidHttpsUrl(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }

    private fun sectionTitle(value: String) = TextView(this).apply {
        text = value
        textSize = 18f
        setPadding(0, dp(18), 0, dp(8))
    }

    private fun weightedButton() = LinearLayout.LayoutParams(0, dp(48), 1f)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
