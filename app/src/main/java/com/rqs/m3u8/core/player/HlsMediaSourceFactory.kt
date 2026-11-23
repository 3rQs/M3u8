package com.rqs.m3u8.core.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.hls.HlsMediaSource
import com.rqs.m3u8.core.cache.HlsCache

object HlsMediaSourceFactory {
    fun create(context: Context, url: String, useCache: Boolean): MediaSource {
        val dataSourceFactory: DataSource.Factory = if (useCache) {
            HlsCache.dataSourceFactory(context)
        } else {
            DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(false)
        }

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        return HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }
}
