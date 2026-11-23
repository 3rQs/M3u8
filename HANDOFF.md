# Handoff — HLS Player + Cache

Date: 2026-11-23

## Repository state

- Repository: https://github.com/3rQs/M3u8
- Working branch: `feature/hls-player-cache`
- Draft pull request: https://github.com/3rQs/M3u8/pull/1
- Target branch: `main`
- PR is open as draft and has not been merged.

## Implemented

- `MainActivity.kt`: HTTPS URL input, Play/Stop controls, opt-in cache checkbox, Media3 PlayerView, playback status, Activity player lifecycle.
- `core/player/HlsMediaSourceFactory.kt`: HLS media source creation with explicit `application/x-mpegURL` MIME type.
- `core/cache/HlsCache.kt`: app-private Media3 SimpleCache, 500 MiB LRU eviction, cache data source factory.
- `docs/PLAYER_CACHE.md`: behavior, limitations, and manual verification.
- `README.md`: updated project status and scope.

## Verified

- Changed files were fetched back from GitHub after writes and expected implementation markers were present.
- Commit exists on the feature branch and PR #1 was created.
- No Android build, emulator run, or physical-device playback test has been performed.

## Important limitations / risks

- Current UI accepts HTTPS URL only; no custom headers, cookies, or bearer-token input yet.
- Cache is opt-in. Only enable it for content the provider permits to be cached.
- DRM handling is not implemented.
- HLS live playlists rotate and not every resource is guaranteed to remain cached; the 500 MiB LRU policy may evict old content.
- No cache hit/miss display or clear-cache control yet.
- Confirm dependency resolution and Kotlin/API compatibility with an actual Gradle build before merging.
- Repository scaffold currently has no Gradle wrapper files; use Android Studio's compatible Gradle setup or add a wrapper in a separately reviewed change.

## Next steps

1. Open the feature branch in Android Studio with JDK 17 and Android SDK 35.
2. Run Gradle sync and build the debug APK.
3. Fix any compiler/dependency errors surgically; do not claim success until build output confirms it.
4. Test an authorized HTTPS VOD HLS stream on a real device or emulator.
5. Repeat with cache unchecked, then checked; inspect app-private cache storage and verify playback after a second request.
6. Test invalid HTTP URL and malformed URL rejection.
7. Review PR #1 and merge only after build and playback tests pass.

## Suggested build commands

If a Gradle wrapper is added or available:
```bash
./gradlew :app:assembleDebug
```

Otherwise use Android Studio: **Build > Make Project**, then run the `app` configuration.
