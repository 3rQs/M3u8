# M3u8

Android HLS player and optional disk cache built with Kotlin and AndroidX Media3.

## Current implementation

- HTTPS HLS playback with AndroidX Media3.
- Optional app-private disk cache, capped at 500 MiB with LRU eviction.
- Cache opt-in per playback session.
- Player lifecycle tied to the Activity.

## Requirements

- Android Studio with JDK 17
- Android SDK Platform 35
- Gradle 8.9 (via Android Studio or a compatible wrapper)

## Build and run

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator. See [docs/PLAYER_CACHE.md](docs/PLAYER_CACHE.md) for manual verification.

## Security and scope

Use only streams and request credentials you own or are authorized to access. The current UI accepts HTTPS URLs only and does not yet expose custom headers, cookies, or bearer-token entry. Never commit signed URLs, cookies, bearer tokens, or private keys.

This app does not extract credentials from third-party apps, defeat DRM, bypass access controls, or circumvent subscription restrictions.
