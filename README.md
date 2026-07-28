# TermiPulse SSH Terminal

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7B1FA2.svg)](https://m3.material.io)

**TermiPulse** is a feature-packed, modern Android SSH client and terminal emulator built with Kotlin, Jetpack Compose, and Material Design 3. Designed for developers, sysadmins, and DevOps engineers, TermiPulse provides powerful remote session management, quick terminal commands, and a local sandbox shell right from your mobile device.

---

## 🚀 Features

- 🖥️ **Multi-Tab Terminal Engine**: Seamlessly switch between multiple active SSH sessions or local sandbox environments with live status indicators.
- 🔑 **Custom Control Key Bar**: Quick access to essential terminal controls like `CTRL`, `ALT`, `TAB`, `ESC`, `▲`, `▼`, `Ctrl+C`, `Ctrl+L`, `Ctrl+D`, and `Ctrl+Z`.
- 📁 **Host Profile Manager**: Save, edit, and organize SSH host profiles with customized labels, port configurations, authentication preferences, and visual badge colors.
- ⚡ **Saved Snippets Drawer**: Store frequently used terminal commands and scripts categorized by workflow (e.g., System, DevOps, Monitoring) for one-tap execution.
- 🧪 **Built-in Local Sandbox**: Safely run simulated shell commands (`pwd`, `top`, `docker`, `git`, `df`, `whoami`, `date`) even when offline.
- 🔄 **Persistent Session Service**: Background service with status bar notifications to keep remote jobs active during app navigation.
- 🎨 **Terminal Theme & Canvas**: High-contrast, dark-themed terminal UI with ANSI color parsing, adjustable font scaling, and cursor animation.
- 💾 **Local Data Persistence**: Powered by Room Database for fast and reliable storage of profiles and snippets.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose with Material 3 components
- **Language**: Kotlin 100%
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture patterns
- **Database**: Room Database with KSP (Kotlin Symbol Processing)
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Testing**: Robolectric & Roborazzi for local JVM testing & screenshot regression tests
- **SSH Engine**: JSch (Java Secure Channel) integration

---

## 📦 Getting Started

### Prerequisites

- **Android Studio**: Jellyfish or newer
- **JDK**: Java 17 or higher
- **Minimum SDK**: Android 8.0 (API level 26)
- **Target SDK**: Android 14 / 15 (API level 34+)

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/juniormichieletto/termipulse-ssh-terminal.git
   cd termipulse-ssh-terminal
   ```

2. **Open in Android Studio**:
   Open Android Studio and select `Open an existing project`, then select the repository folder.

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit & Robolectric Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
