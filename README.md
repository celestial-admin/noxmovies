# 🎬 NOX Movies

<div align="center">

<img src="https://raw.githubusercontent.com/simple-icons/simple-icons/develop/icons/android.svg" width="80"/>

# NOX Movies

### Premium • Minimal • Cinematic Android Streaming Experience

*A modern Android application built with Kotlin and Jetpack Compose for discovering, streaming, and organizing movies with a premium cinematic interface.*

<p align="center">
<img src="https://img.shields.io/badge/Version-v1.0.0_Stable-FF4FA3?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Android-8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Kotlin-Jetpack_Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Material_3-AMOLED_UI-000000?style=for-the-badge"/>
<img src="https://img.shields.io/badge/License-MIT-2563EB?style=for-the-badge"/>
</p>

### Building NOX. From Zero.

Minimal aesthetics, cinematic motion, and powerful media playback — designed for Android.

</div>

---

## ✨ About NOX Movies

**NOX Movies** is a premium Android application focused on delivering a fast, elegant, and cinematic movie browsing experience.

Instead of copying traditional streaming apps, NOX follows a **minimal editorial design language** inspired by modern entertainment platforms and AMOLED-first Android interfaces.

### Why NOX?

* 🎬 Cinematic interface inspired by premium OTT apps.
* ⚡ Extremely fast Jetpack Compose UI.
* 🌑 AMOLED optimized dark theme.
* ❤️ Personal watch library and favorites.
* 📥 Offline download management.
* 🎥 Smooth AndroidX Media3 playback.

---

# 📱 Features

## 🎬 Cinematic User Interface

* Premium editorial layout.
* AMOLED optimized (`#080808`) dark theme.
* Beautiful movie banners and posters.
* Smooth animations powered by Jetpack Compose.
* Material 3 design system.

## 🔎 Smart Search & Discovery

* Debounced instant search.
* Trending movies.
* Popular categories.
* Recent searches.
* Local recommendation repository.

## ▶️ Built-in Video Player

Powered by **AndroidX Media3 (ExoPlayer)**

* Fullscreen playback.
* Subtitle-ready architecture.
* Landscape & portrait modes.
* Playback progress memory.
* Continue Watching support.

## ❤️ Personal Library

* Favorites.
* Watch History.
* Continue Watching.
* Recently Viewed movies.

## 📥 Download Manager

* Offline download UI.
* Progress indicators.
* Pause / Resume architecture.
* Download history.

## ⚙️ Performance

* Native Kotlin application.
* Compose rendering.
* Optimized image loading.
* Smooth scrolling.
* MVVM architecture.

---

# 📸 Screenshots

> Replace these with your own screenshots after release.

| Home                   | Details                   |
| ---------------------- | ------------------------- |
| `screenshots/home.png` | `screenshots/details.png` |

| Search                   | Player                   |
| ------------------------ | ------------------------ |
| `screenshots/search.png` | `screenshots/player.png` |

---

# 🎨 Design Language

NOX follows a simple philosophy.

> **Less Interface. More Cinema.**

## Theme

| Property   | Value                 |
| ---------- | --------------------- |
| Background | `#080808`             |
| Surface    | `#111111`             |
| Accent     | White + Soft Pink     |
| Typography | Material 3            |
| Style      | Cinematic / Editorial |

## Design Principles

* Minimal UI.
* Rich movie artwork.
* Smooth motion.
* Comfortable spacing.
* Zero visual clutter.

---

# 🛠 Tech Stack

| Layer            | Technology                  |
| ---------------- | --------------------------- |
| Language         | Kotlin                      |
| UI Framework     | Jetpack Compose             |
| Design           | Material 3                  |
| Player           | AndroidX Media3 (ExoPlayer) |
| Images           | Coil                        |
| Navigation       | Navigation Compose          |
| Architecture     | MVVM                        |
| Async            | Kotlin Coroutines           |
| State Management | ViewModel + Compose State   |

---

# 📂 Project Structure

```text
NoxMovies/
├── app/
│   ├── data/
│   ├── model/
│   ├── repository/
│   ├── player/
│   ├── ui/
│   │   ├── screens/
│   │   ├── components/
│   │   ├── navigation/
│   │   └── theme/
│   └── utils/
├── README.md
├── CHANGELOG.md
├── LICENSE
└── ROADMAP.md
```

---

# 🚀 Getting Started

## Requirements

* Android Studio Jellyfish or newer.
* Android SDK 34+
* JDK 21
* Gradle 8+

## Clone Repository

```bash
git clone https://github.com/HarshitRaj/NoxMovies.git

cd NoxMovies
```

## Build Debug APK

```bash
./gradlew assembleDebug
```

## Build Release APK

```bash
./gradlew assembleRelease
```

Release APK will be generated inside:

```text
app/build/outputs/apk/release/
```

---

# 📦 Installation

### Android Studio

1. Clone the repository.
2. Open in Android Studio.
3. Wait for Gradle Sync.
4. Run on Emulator or Android Device.

### APK Installation

1. Download the latest stable APK.
2. Allow **Install Unknown Apps**.
3. Install the APK.
4. Launch **NOX Movies**.

---

# 🎥 Current Screens

| Screen            | Status     |
| ----------------- | ---------- |
| Home              | ✅ Complete |
| Search            | ✅ Complete |
| Movie Details     | ✅ Complete |
| Favorites         | ✅ Complete |
| Downloads         | ✅ Complete |
| Player            | ✅ Complete |
| Continue Watching | ✅ Complete |

---

# 🗺 Roadmap

## v1.0.0 — Stable Release ✅

* Cinematic UI.
* Search.
* Movie Details.
* Media3 Player.
* Favorites.
* Downloads UI.

## v1.1 — Streaming Update 🚧

* User Authentication.
* Cloud Watch History.
* Trailer Preview.
* Better Downloads.
* Improved Search.

## v1.2 — Premium Update

* Chromecast.
* Continue Watching Sync.
* Dynamic Accent Colors.
* Subtitle Downloader.
* Notifications.

## v2.0 — NOX Ecosystem

* Firebase Backend.
* User Profiles.
* AI Recommendations.
* Watchlists.
* Cross-device Sync.

---

# ⚡ Performance Goals

* Fast startup time.
* Smooth 60 FPS scrolling.
* Low memory usage.
* Native Android performance.
* Compose-first architecture.

---

# 🤝 Contributing

We welcome contributions from developers.

## Steps

```bash
git checkout -b feature/amazing-feature

git commit -m "feat: add amazing feature"

git push origin feature/amazing-feature
```

Then create a Pull Request.

---

# 🐞 Reporting Issues

Please include:

* Device name.
* Android version.
* App version.
* Screenshots.
* Steps to reproduce.

GitHub Issues are available for bug reports and feature requests.

---

# 🔒 Security

Security vulnerabilities should be reported privately through GitHub Security Advisories.

Please do not disclose vulnerabilities publicly before they are fixed.

---

# ⚠️ Disclaimer

NOX Movies is developed for educational and development purposes.

* The repository does **not** contain copyrighted movie content.
* Users are responsible for using legal media sources.
* Demo media is used only for testing and UI development.

---

# 📊 Project Information

| Item            | Value                |
| --------------- | -------------------- |
| Project Name    | NOX Movies           |
| Codename        | NOX                  |
| Version         | **v1.0.0 Stable**    |
| Platform        | Android              |
| Language        | Kotlin               |
| UI              | Jetpack Compose      |
| Minimum Android | Android 8.0 (API 26) |
| Architecture    | MVVM                 |
| License         | MIT License          |

---

# 📄 Documentation

* `CHANGELOG.md` — Version history.
* `ROADMAP.md` — Upcoming releases.
* `CONTRIBUTING.md` — Contribution guide.
* `LICENSE` — MIT License.
* `SECURITY.md` — Security policy.

---

# 💖 Credits

Designed and developed by **Harshit Raj**.

**NOX** is a personal Android project focused on building a premium cinematic media experience using modern Android technologies.

---

<div align="center">

## NOX

### Building NOX. From Zero.

**Made with Kotlin, Jetpack Compose, and a love for cinematic design.**

⭐ If you like this project, consider starring the repository.

</div>
