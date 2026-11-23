# M3u8

Android HLS player and cache manager built with Kotlin and AndroidX Media3.

> Current status: project scaffold. Playback, authenticated source handling, and persistent caching are planned but are not implemented yet.

## Goals

- Accept an HLS master or media playlist URL supplied by an authorized source.
- Support explicitly supplied request headers for authorized playback.
- Play HLS streams with AndroidX Media3.
- Add bounded disk caching and cache lifecycle controls.
- Keep credentials out of logs, source control, and diagnostic reports.

## Requirements

- Android Studio with JDK 17
- Android SDK Platform 35
- Gradle 8.9 (via Android Studio or a compatible wrapper)

## Build

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

## Project layout

- `app/`: Android application and UI
- `core/network/`: authorized source and request configuration
- `core/player/`: Media3 playback integration
- `core/cache/`: disk cache policy and lifecycle
- `docs/`: architecture, API contract, and implementation notes

## Security and scope

This app does not extract credentials from third-party apps, defeat DRM, bypass access controls, or circumvent subscription restrictions. Use only streams and request credentials you own or are authorized to access. Never commit cookies, bearer tokens, signed URLs, or private keys.

## Roadmap

1. Scaffold and architecture documentation.
2. URL validation and source configuration.
3. Media3 HLS playback.
4. Persistent cache with size limits and eviction.
5. Tests for URL handling, cache policy, and playback errors.
