<div align="center">

<img src="app/src/main/res/drawable/keywe_icon.png" width="120" height="120" alt="Keywe Logo" />

# KEYWE

**Native Bluetooth HID Keyboard & Multi-Touch Trackpad Peripheral for Android**

*Transform your Android device into a zero-latency, hardware-emulated Bluetooth Keyboard and Multi-Touch Trackpad — with zero host software or background drivers required on your computer.*

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-00E676?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/Version-1.2.0--beta-FF2A2A?style=for-the-badge)](app/build.gradle.kts)
[![Privacy Policy](https://img.shields.io/badge/Privacy--Policy-Live-0A66C2?style=for-the-badge)](https://www.farjan.me/KeywePrivacyPolicy/)
[![License](https://img.shields.io/badge/License-GPL--3.0-FFFF2A2A?style=for-the-badge)](LICENSE)

---

</div>

## 📌 Overview

**Keywe** is a high-performance Android utility that emulates a physical Bluetooth Human Interface Device (HID) over standard Bluetooth L2CAP channels. Built on Android's native `BluetoothHidDevice` subsystem, Keywe operates directly at the hardware protocol layer.

Unlike traditional VNC, Wi-Fi, or proprietary remote desktop solutions, **Keywe requires ZERO host-side software, background agents, drivers, or companion server applications.** Connecting devices (Windows, macOS, Linux, ChromeOS, Smart TVs, and gaming consoles) recognize your phone out of the box as a standard physical Bluetooth keyboard and mouse.

---

## ✨ Core Features & Technical Highlights

### ⚡ True Driverless & Persistent Multitasking Architecture
- **Zero Host Installation**: Direct Bluetooth HID pairing. Zero companion apps, server daemons, or open local network ports required.
- **Background Multitasking Service (`KeyweHidService`)**: Powered by an Android Foreground Service (`connectedDevice` type). Maintains a 100% active Bluetooth HID link while you switch apps or multitask.
- **Smart Recents Teardown**: Hooks into `onTaskRemoved()` — when you swipe away Keywe from the Recent Apps screen, the service automatically disconnects the PC, stops the service, and dismisses the status notification.
- **Cross-Platform Compatibility**: Out-of-the-box support for **Windows 10/11**, **macOS**, **Linux**, **ChromeOS**, **Android TV**, **Apple TV**, and **Consoles**.
- **Privacy-Centric**: Communication stays strictly local over encrypted Bluetooth links; zero data leaves your local environment.

---

### 🖱️ Multi-Touch Gesture Trackpad
- **High-Precision Cursor Engine**: Delta-based pointer tracking with customizable sensitivity scaling (`0.5x` – `3.5x`).
- **Live Touch Aura Feedback**: Real-time canvas-rendered glowing indicator tracking multi-touch coordinates on the active surface.
- **Dual-Axis 2-Finger Gestures**:
  - **Vertical 2-Finger Drag**: Smooth vertical wheel scrolling.
  - **Horizontal 2-Finger Drag**: Native horizontal axis panning.
  - *Dominant-axis analysis cleanly disambiguates diagonal strokes.*
- **Tap Gesture Detection**:
  - **1-Finger Tap**: Left Click.
  - **2-Finger Tap**: Right Click.
  - *8px Euclidean displacement jitter filter ensures micro-movements do not cancel taps.*
- **Physical Click Bar**: Dedicated `[ L-CLICK ]` and `[ R-CLICK ]` buttons with tactile haptic vibration pulses.

---

### 📳 Universal Hardware Haptic Engine
- **Android 10+ (API 29+) Hardware Waveforms**: Utilizes `VibrationEffect.EFFECT_CLICK` to trigger native hardware tactile click motors directly.
- **Explicit Amplitude Fallback (`180`/255)**: Designed to overcome OEM vibration motor suppression (Samsung OneUI, Xiaomi MIUI, Motorola, Realme/Oppo) on Android 8/9 devices.

---

### ⌨️ 75% Tactile Mechanical Input Deck
- **Authentic Physical Layout**: Complete 75% deck including Function keys (`F1`–`F12`), `Esc` (Accent Color), `PrtSc` (Print Screen), `Delete`, `Enter`, and a dedicated directional arrow cluster (`▲`, `◄`, `▼`, `►`).
- **Dynamic Keycap Shortcut Hints**:
  - Toggling **`Ctrl`** dynamically renders sub-labels on keycaps (`C` ➔ `COPY`, `V` ➔ `PASTE`, `Z` ➔ `UNDO`, `A` ➔ `ALL`, `S` ➔ `SAVE`, `W` ➔ `CLOSE`, `F` ➔ `FIND`, etc.).
  - Toggling **`Win`** exposes operating system shortcuts (`E` ➔ `FILES`, `R` ➔ `RUN`, `D` ➔ `DESK`, `L` ➔ `LOCK`, `V` ➔ `CLIP`).
- **One-Shot Shift**: Shift acts as a natural one-shot modifier, automatically resetting after character entry to prevent unintentional caps lock states.
- **Simultaneous Release Protocol**: HID release reports clear keycodes and modifier masks simultaneously `(0, 0)` for clean keystroke termination.

---

### 📱 Dual Keyboard Engines & Remote Clipboard Sync
- **Tactile Deck Engine**: Low-latency monochrome mechanical keyboard with localized haptics.
- **System Soft Keyboard (Phone IME)**: Seamlessly switch to your phone's native keyboard (Gboard, SwiftKey, Voice Typing, Glide Input) by holding the mode selector.
- **Paced Async Clipboard Streaming**: `PASTE TO PC` transmits clipboard strings with live progress tracking (`SENDING 42/150`) and paced 8ms inter-key dispatching to prevent HID buffer overruns.
- **Host-Synced Remote Clear**: Tapping `CLEAR` synchronizes the focused host input field (`Ctrl + A` + `Delete`) while clearing the local text buffer.

---

### 🖥️ Adaptive Widescreen & Landscape UI
- **Compact Landscape Toolbar**: High-density horizontal layout designed for landscape trackpad and typing usage.
- **Header Bluetooth Control**: Direct access to the Bluetooth Device Manager from both portrait status indicators and the landscape `BT` button.
- **Scroll-Safe Modal Architecture**: Dialogs (`DeviceManagerDialog`, `SettingsDialog`, `UserGuideDialog`) dynamically adapt with vertical scroll boundaries across small screens.

---

### 🎨 Minimalist Monochrome & Dot-Matrix Design System
- **Curated Theme Presets**:
  - `MONOCHROME DARK` (Deep Charcoal & Signal Red Accent)
  - `MATRIX GREEN` (Terminal Phosphor Green Accent)
  - `CYBER AMBER` (Industrial Amber Accent)
  - `TACTILE WHITE` (High-Contrast Minimal Accent)
- **Persistent Preferences**: Saves theme choice, keyboard engine preference, touchpad sensitivity, and haptics to `SharedPreferences`.

---

## 🛠️ Architecture & Data Flow

```
+-------------------------------------------------------------------+
|                        Keywe Application                          |
|   (Jetpack Compose UI • Dot-Matrix Monospace Design System)       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|               KeyweHidService (Foreground Service)                |
|  - Foreground Type: "connectedDevice" • Live Status Notification  |
|  - Manages Persistent Connection Across Multitasking/App Switch   |
|  - Automatic Teardown on Task Removed (Swiped from Recents)       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                   BluetoothHidManager Engine                      |
|  - Dual Executors: Thread-Priority Scheduled & High-Priority HID  |
|  - Idempotent Lifecycle & ACL Link Disconnect State Machine       |
|  - Real-Time Bluetooth Profile Verification                       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                 USB HID Combo Report Descriptor                   |
|       Report ID 1: 4-Byte Mouse (Buttons, dX, dY, Wheel)          |
|       Report ID 2: 8-Byte Keyboard (Modifiers, Reserved, Keys[6]) |
+-------------------------------------------------------------------+
                                  |
                      Bluetooth L2CAP Connection
                                  |
                                  v
+-------------------------------------------------------------------+
|                 Host PC / Mac / Linux / Smart TV                  |
|          (Standard Native OS USB HID Peripheral Driver)           |
+-------------------------------------------------------------------+
```

---

## 📋 Technical Specifications

| Specification | Details |
|---|---|
| **App Name** | Keywe |
| **Package Name** | `com.shejan.keywe` |
| **Version** | `1.2.0-beta` (Version Code `3`) |
| **Language** | Kotlin 2.0.0 |
| **UI Toolkit** | Jetpack Compose (Material 3) |
| **Bluetooth Architecture** | Android `BluetoothHidDevice` API, L2CAP Sockets, SDP Records |
| **Service Architecture** | Foreground Service (`connectedDevice`), Notification Channel |
| **Minimum SDK** | API Level 24 (Android 7.0 "Nougat") ~99.2% Device Compatibility |
| **Target SDK** | API Level 36 (Android 15 / Minor API Level 1) |
| **Build Configuration** | Kotlin DSL (`build.gradle.kts`) |

---

## 🚀 Quick Setup Guide

### 1. Requirements
- An Android device running **Android 9.0 (API Level 28)** or newer with hardware HID profile support.
- A target computer or device with Bluetooth enabled.

### 2. Pairing Process
1. Open **Keywe** on your phone.
2. Tap the LED status indicator in the header to open the **Bluetooth Device Manager**.
3. Under the **NEARBY** tab, scan and select your computer, or make your phone discoverable and pair directly from your computer's Bluetooth settings.
4. Once paired, the header status turns **`CONNECTED`** — start typing and controlling your cursor instantly!

---

## ⚙️ Building From Source

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17 / 21
- Android SDK Platform 36

### Build Commands (PowerShell)

```powershell
# Clone the repository
git clone https://github.com/shejanahmmed/Keywe.git
cd Keywe

# Assemble debug build
.\gradlew.bat assembleDebug

# Install to connected device
.\gradlew.bat installDebug
```

---

## 🔒 Privacy & Security

Keywe does **not** collect, store, or transmit any user input, keystrokes, or personal data. All HID transmissions are strictly local to the established Bluetooth connection.

Read our live privacy policy:  
👉 **[https://www.farjan.me/KeywePrivacyPolicy/](https://www.farjan.me/KeywePrivacyPolicy/)**

---

## 👤 Author

**Farjan Ahmmed (Shejan)**  
*Software Engineer*

[![Email](https://img.shields.io/badge/Email-farjan.swe%40gmail.com-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:farjan.swe@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-shejanahmmed-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/shejanahmmed)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-farjan--ahmmed-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/farjan-ahmmed/)
[![Facebook](https://img.shields.io/badge/Facebook-beingshejan-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://www.facebook.com/beingshejan/)
[![Instagram](https://img.shields.io/badge/Instagram-iamshejan-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/iamshejan/)

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.
