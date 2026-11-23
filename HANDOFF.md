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
- `.github/workflows/android-debug.yml`: GitHub Actions build for debug APK using JDK 17, Android SDK 35, and Gradle 8.9. No Gradle wrapper is currently required by this workflow.

## Verified

- GitHub Actions workflow file was committed and fetched back from the feature branch.
- Workflow is configured to run on pushes to `feature/hls-player-cache` and pull requests targeting `main`.
- The workflow build itself has not yet been confirmed successful.
- No Android device/emulator playback or cache test has been performed.

## Important limitations / risks

- Current UI accepts HTTPS URL only; no custom headers, cookies, or bearer-token input yet.
- Cache is opt-in. Only enable it for content the provider permits to be cached.
- DRM handling is not implemented.
- HLS live playlists rotate and not every resource is guaranteed to remain cached; the 500 MiB LRU policy may evict old content.
- No cache hit/miss display or clear-cache control yet.
- Build errors, if any, must be fixed surgically based on the actual Actions log before merging.

## Build APK from Android phone

1. Open https://github.com/3rQs/M3u8/actions
2. Select **Android Debug APK**.
3. Open the latest run for `feature/hls-player-cache`.
4. When successful, download artifact **M3u8-debug-apk** (ZIP), then extract the APK on the phone.

## Next steps

1. Check the GitHub Actions run and inspect the first failing step/log if it fails.
2. Download and install the debug APK only after a successful build.
3. Test an authorized HTTPS VOD HLS stream on a real device.
4. Repeat with cache unchecked, then checked; verify the second request and app-private cache behavior.
5. Test invalid HTTP URL and malformed URL rejection.
6. Review PR #1 and merge only after build and playback tests pass.

## Local build command

```bash
gradle :app:assembleDebug
```