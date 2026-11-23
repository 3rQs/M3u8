package com.rqs.m3u8.core.download

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

/**
 * Persistent offline-download storage. This cache intentionally has no eviction policy:
 * completed downloads remain until the user removes them.
 */
object HlsDownloadManagerProvider {
    private const val DOWNLOAD_DIRECTORY = "hls_offline_downloads"

    @Volatile
    private var downloadCache: SimpleCache? = null

    @Volatile
    private var downloadManager: DownloadManager? = null

    private val downloadExecutor = Executors.newFixedThreadPool(2)

    @Synchronized
    fun getCache(context: Context): Cache {
        return downloadCache ?: run {
            val appContext = context.applicationContext
            val databaseProvider = StandaloneDatabaseProvider(appContext)
            SimpleCache(
                File(appContext.filesDir, DOWNLOAD_DIRECTORY),
                NoOpCacheEvictor(),
                databaseProvider
            ).also { downloadCache = it }
        }
    }

    @Synchronized
    fun getManager(context: Context): DownloadManager {
        return downloadManager ?: run {
            val appContext = context.applicationContext
            val databaseProvider = StandaloneDatabaseProvider(appContext)
            val cache = getCache(appContext)
            DownloadManager(
                appContext,
                databaseProvider,
                cache,
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(false),
                downloadExecutor
            ).apply {
                setMaxParallelDownloads(2)
                setMinRetryCount(3)
            }.also { downloadManager = it }
        }
    }
}
