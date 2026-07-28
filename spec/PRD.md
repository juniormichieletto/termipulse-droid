# Product Requirements Document (PRD)
## Project Name: TermiPulse SSH Terminal

### 1. Executive Summary
**TermiPulse** is a feature-packed, high-performance Android SSH Terminal app built for developers, system administrators, and DevOps engineers. It enables seamless remote access to PCs, servers, and cloud instances over SSH. TermiPulse features multi-tab session switching, customizable touch-optimized keybars, persistent background sessions for long-running jobs via Android Foreground Services, profile storage with key/password authentication, and a quick-command snippet launcher.

---

### 2. Objectives & User Use Cases

#### Core Objectives
* **Remote Management On-The-Go**: Connect to any remote SSH host with custom ports, credentials, or private SSH keys.
* **Multi-Tab Workspace**: Maintain concurrent active SSH channels across multiple servers or multiple sessions on a single machine.
* **Background Job Continuity**: Prevent disconnected sessions or dropped tasks when the app is backgrounded or when switching apps while running long-running jobs (`npm build`, `docker build`, `rsync`, `python scripts`).
* **Mobile-Optimized Touch Keyboard**: Quick-access touch bar providing `Ctrl`, `Alt`, `Esc`, `Tab`, directional arrows, pipe `|`, backtick `` ` ``, and function keys without struggling with standard soft keyboards.
* **Snippets & Quick Commands**: Store and execute frequently used bash scripts and system commands with a single tap.
* **Interactive Local Terminal Engine**: Built-in interactive local sandbox shell simulator allowing offline testing, mock remote host simulation, and immediate user demonstration.

---

### 3. Target Audience
* **DevOps & System Administrators**: Monitoring server metrics, logs (`tail -f`), restarting services, and executing deployment scripts.
* **Software Developers**: Accessing remote dev environments, building code on powerful remote PCs, checking git branch statuses.
* **Homelab Enthusiasts**: Managing Raspberry Pi nodes, Proxmox hypervisors, Docker containers, and NAS systems.

---

### 4. Functional Requirements

#### 4.1 Host Profile Management
* **Profile Attributes**: Label, Hostname / IP address, Port (Default 22), Username, Auth Mode (Password vs Private Key), Visual Badge Color.
* **Storage**: Encrypted/Local Room Database persistence with quick connect, duplicate, edit, and delete functionality.
* **Security**: Password & private key credentials stored securely in local database tables.

#### 4.2 Multi-Tab SSH Terminal Engine
* **Tab Navigation Bar**: Horizontally scrollable top bar listing all active sessions with host title, connection state badge (Connected, Connecting, Reconnecting, Idle, Long Job Active), and quick close button `✕`.
* **New Tab Creator**: Ability to launch a new tab from saved host profiles or open a local sandbox terminal.
* **Terminal Display**:
  * Rich Monospace output rendering.
  * ANSI color code interpretation (Green, Cyan, Yellow, Red, Magenta, Bold, Reset).
  * Smooth auto-scroll to bottom with manual scroll freeze on user swipe up.
  * Clear screen (`clear` / Ctrl+L) and copy terminal buffer to clipboard.
  * Font scaling (Zoom In / Zoom Out touch buttons or settings).

#### 4.3 Mobile Control Keybar
* Integrated dynamic toolbar above the soft keyboard:
  * **Primary Keys**: `Esc`, `Tab`, `Ctrl`, `Alt`, `|`, `/`, `\`, `~`, `-`, `_`, `$`, `;`
  * **Navigation Keys**: Up `▲`, Down `▼`, Left `◄`, Right `►`, `Home`, `End`
  * **Special Shortcuts**: `Ctrl+C` (Interrupt), `Ctrl+Z` (Suspend), `Ctrl+D` (EOF), `Ctrl+L` (Clear Screen)
  * **Key Toggle Modes**: Sticky Ctrl and Alt states for combination keystrokes.

#### 4.4 Background Job Continuity & Foreground Service
* **Android Foreground Service (`TerminalSessionService`)**:
  * Keeps SSH connections open and polling actively when app is minimized or device screen locks.
  * Displays a persistent notification with status of active tabs and quick disconnect/return actions.
  * Job status indicator: Visual badge indicating background task execution.

#### 4.5 Quick Command Snippets
* Library of user-saved bash snippets (e.g., `htop`, `docker ps -a`, `tail -f /var/log/syslog`, `git status`, `systemctl status nginx`).
* One-tap execution directly injected into the active terminal session.

#### 4.6 Local Sandbox Simulator
* In-app local shell sandbox supporting commands (`help`, `top`, `htop`, `ping`, `ls`, `uname`, `uptime`, `cat`, `python`, `docker`, `clear`, long-running background timers) to ensure full functionality without requiring an external server network setup.

---

### 5. Technical Architecture & Component Design

#### Tech Stack
* **Language**: 100% Kotlin
* **UI Framework**: Jetpack Compose (Material Design 3 with custom Dark Terminal aesthetics)
* **Architecture**: MVVM with Repository Pattern
* **Database**: Room DB with KSP annotation processing
* **Asynchronous Engine**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
* **SSH Client Engine**: JSch (`com.github.mwiede:jsch`) + Custom Socket Coroutine Wrapper & Local Shell Engine
* **Background Service**: Android Foreground Service (`TerminalSessionService`)

---

### 6. UI/UX Design System
* **Theme**: Deep Carbon Black (`#0C0E12`) canvas, Obsidian surface (`#161A22`), Matrix Emerald (`#00E676`) primary accent, Electric Cyan (`#00E5FF`) secondary accent, High-contrast Off-White (`#E0E6ED`) text.
* **Typography**: Monospace font family (`FontFamily.Monospace`) for terminal output, high readability crisp sans-serif for UI labels.
* **Layout Structure**:
  * Top App Bar: App identity, active host indicator, foreground service status, snippet drawer trigger, host settings.
  * Multi-Tab Bar: Tab pills with active indicator and status color dot.
  * Terminal Output Window: Full width scrollable terminal canvas.
  * Mobile Control Keybar: Touch buttons for terminal navigation.
  * Bottom Bar / Drawer: Profile selector, command history, and custom command input.

---

### 7. Non-Functional Requirements
* **Performance**: Sub-100ms keypress response, zero lag during rapid streaming terminal output.
* **Stability**: Graceful handling of network drops, auto-reconnect option on socket disconnect.
* **Privacy**: All keys, passwords, and profiles stored strictly on-device. No telemetry or external server tracking.
