# HLS player and disk cache

## Implemented in this iteration

- AndroidX Media3 HLS playback using an explicit HLS MIME type.
- HTTPS URL validation in the Activity.
- Optional Media3 `SimpleCache` backed by app-private files.
- 500 MiB `LeastRecentlyUsedCacheEvictor` limit.
- Cache is opt-in per playback session; unchecked means direct upstream playback.
- Player is released with the Activity lifecycle. The process-wide cache is not released when the Activity stops.

## Cache behavior and limitations

Media3's cache data source stores fetched HLS resources that pass through it, including playlist/segment requests where supported by the stream. This is not a guarantee that every HLS resource is retained: live playlists rotate, byte-range requests and encryption/key resources have stream-specific behavior, and cache eviction can remove old data. The UI does not yet expose cache hit/miss metrics or a clear-cache control.

Only enable caching when the content provider permits local copies. Do not use this cache for DRM-protected content unless the provider explicitly authorizes that workflow. This implementation does not implement DRM.

## Source requirements

- HTTPS URL with a valid host.
- Publicly accessible or otherwise authorized source.
- No custom request-header editor in this iteration. Sources requiring cookies, bearer tokens, or custom headers are not supported by the current UI.

## Manual verification

1. Open the project in Android Studio and sync Gradle.
2. Run on Android 7.0 (API 24) or newer.
3. Play a short HTTPS HLS test stream that you own or are authorized to use.
4. Repeat playback with cache enabled and inspect app-private storage/cache behavior using Android Studio Device Explorer or app instrumentation.
5. Verify the app still plays with cache disabled.
6. Verify invalid HTTP and malformed URLs are rejected.

A successful repository write is not a successful Android build. Build and device playback still need to be run in Android Studio or CI.
