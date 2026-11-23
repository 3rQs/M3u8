package com.rqs.m3u8.core.download

import android.content.Context
import android.net.Uri
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import java.security.MessageDigest

@UnstableApi
object M3u8Downloads {
    fun enqueue(context: Context, url: String, title: String = ""): String {
        val uri = Uri.parse(url)
        require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) {
            "Only valid HTTPS HLS URLs are supported"
        }

        val id = sha256(url)
        val request = DownloadRequest.Builder(id, uri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setData(title.toByteArray(Charsets.UTF_8))
            .build()

        DownloadService.sendAddDownload(
            context,
            M3u8DownloadService::class.java,
            request,
            false
        )
        return id
    }

    fun pauseAll(context: Context) {
        DownloadService.sendPauseDownloads(context, M3u8DownloadService::class.java, false)
    }

    fun resumeAll(context: Context) {
        DownloadService.sendResumeDownloads(context, M3u8DownloadService::class.java, false)
    }

    fun pause(context: Context, id: String) {
        DownloadService.sendSetStopReason(
            context,
            M3u8DownloadService::class.java,
            id,
            USER_PAUSE_REASON,
            false
        )
    }

    fun resume(context: Context, id: String) {
        DownloadService.sendSetStopReason(
            context,
            M3u8DownloadService::class.java,
            id,
            Download.STOP_REASON_NONE,
            false
        )
    }

    fun remove(context: Context, id: String) {
        DownloadService.sendRemoveDownload(
            context,
            M3u8DownloadService::class.java,
            id,
            false
        )
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private const val USER_PAUSE_REASON = 1
}
