# Architecture

## Module boundaries

The first iteration keeps a single Android application module and separates core responsibilities by package. The folders can become Gradle modules when the interfaces stabilize.

- `network`: validates source URLs and builds request configuration from user-authorized headers.
- `player`: owns Media3 player lifecycle, HLS media-source creation, playback state, and error mapping.
- `cache`: owns SimpleCache, cache directory, eviction policy, and cache statistics.
- `ui`: accepts a source URL and displays playback/cache state.

## Data flow

1. The user supplies an HLS URL and optional authorized request headers.
2. The network layer validates the URL scheme and creates a request configuration.
3. The player layer creates a Media3 HLS media source.
4. The cache layer may provide a CacheDataSource for content that is permitted to be cached.
5. The UI observes playback state and cache status.

## Cache design constraints

- Use a single process-wide Media3 SimpleCache instance per cache directory.
- Use an explicit maximum cache size and an LRU eviction policy.
- Release the cache only when the application is shutting down, not whenever a player screen closes.
- Do not cache DRM-protected content unless the content provider explicitly permits it.
- Do not treat an HLS playlist file as the complete video: segments, keys, subtitles, and manifests may be separate resources.
- Avoid logging full URLs or request headers because signed query parameters and credentials may be present.

## Security

Credentials must be provided through an authorized configuration path, kept in memory where practical, and redacted from logs and crash reports. Do not scrape another app's private storage or intercept protected traffic.
