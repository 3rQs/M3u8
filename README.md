# M3u8

Android HLS player and optional disk cache built with Kotlin and AndroidX Media3.

## Current implementation

- HTTPS HLS playback with AndroidX Media3.
- Optional app-private disk cache, capped at 500 MiB with LRU eviction.
- Cache opt-in per playback session.
- Player lifecycle tied to the Activity.
- GitHub Actions workflow to build a debug APK without a local Android Studio installation.

## Build an APK from a phone

1. Open the [Actions tab](https://github.com/3rQs/M3u8/actions).
2. Select **Android Debug APK** and open the latest run for `feature/hls-player-cache`.
3. Wait for the run to finish successfully.
4. Under **Artifacts**, download **M3u8-debug-apk**. GitHub provides it as a ZIP containing the debug APK.

The workflow uses JDK 17, Android SDK 35, and Gradle 8.9. It runs automatically when this feature branch is pushed and for pull requests targeting `main`. Manual **Run workflow** is available when the workflow exists on the default branch.

## Build locally

Open this repository in Android Studio with JDK 17 and Android SDK Platform 35, then build the `app` configuration. See [docs/PLAYER_CACHE.md](docs/PLAYER_CACHE.md) for manual playback/cache verification.

## Security and scope

Use only streams and request credentials you own or are authorized to access. The current UI accepts HTTPS URLs only and does not yet expose custom headers, cookies, or bearer-token entry. Never commit signed URLs, cookies, bearer tokens, or private keys.

This app does not extract credentials from third-party apps, defeat DRM, bypass access controls, or circumvent subscription restrictions.