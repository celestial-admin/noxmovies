# NOX Movies

Premium, minimal, cinematic Android media discovery and playback application.

## Features
- **Cinematic UI**: AMOLED-friendly `#080808` dark theme, minimalist editorial layout.
- **Media Player**: Built-in Exoplayer via AndroidX Media3.
- **Search & Discovery**: Debounced local searching and simulated fetching.
- **Save & Download**: (Stubbed) Download tracking UI and Favorites.
- **Android First**: Built natively using Kotlin, Jetpack Compose, and Material 3 (replaces the initial Flutter spec for native performance and platform fit).

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Media Player**: AndroidX Media3 (Exoplayer)
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: MVVM / Compose State Management

## Installation

This project is a native Android application built with Gradle.

To run locally from the command line:

```bash
./gradlew assembleDebug
```

## Running Locally
Open the project in Android Studio (Jellyfish or newer recommended). Let Gradle sync, then run it on an emulator or physical device.

## Building APK

```bash
./gradlew assembleRelease
```
APK output will be in `app/build/outputs/apk/release/`.

## Architecture
- `com.example.model` - Data models (Movie, MediaSource)
- `com.example.data` - Repositories (MovieRepository)
- `com.example.ui.screens` - Feature-based Compose UI screens
- `com.example.ui.theme` - Theme configuration for NOX (Colors, Typography)
- `com.example.ui.components` - Reusable UI widgets

## Configuration
No API keys required for the demo build. Add your `.env` following `.env.example` if you add backend integrations.

## Content Rights
Demo content provided via placeholder API responses and publicly available demo media (`BigBuckBunny`).
Do not distribute unauthorized copyrighted media with this source code.

## License
MIT License
