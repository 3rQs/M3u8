package com.rqs.m3u8.core.cache

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Process-wide disk cache for streams that the user is explicitly allowed to cache.
 * Keep this instance alive across Activity/player recreation.
 */
object HlsCache {
    private const val MAX_CACHE_BYTES = 500L * 1024L * 1024L
    private const val CACHE_DIRECTORY = "media3_hls_cache"

    @Volatile
    private var instance: SimpleCache? = null

    @Synchronized
    fun get(context: Context): Cache {
        return instance ?: SimpleCache(
            File(context.applicationContext.filesDir, CACHE_DIRECTORY),
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
            StandaloneDatabaseProvider(context.applicationContext)
        ).also { instance = it }
    }

    fun dataSourceFactory(context: Context): CacheDataSource.Factory {
        val upstream = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(false)

        return CacheDataSource.Factory()
            .setCache(get(context))
            .setUpstreamDataSourceFactory(upstream)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    const val maxCacheBytes: Long = MAX_CACHE_BYTES
}
