# Keywe Project Memory & Rules

- **Project Name**: Keywe (`com.shejan.keywe`)
- **Template**: Empty Activity with Jetpack Compose
- **Build Configuration**: Kotlin DSL (`build.gradle.kts`)
- **Minimum SDK**: API 24 (Android 7.0 "Nougat") with ~99.2% device compatibility
- **Save Location**: `C:\Users\ShejanAhmmed\Documents\All My Projects\Apps\Keywe`
- **NDK Warning**: The project path contains whitespace (`All My Projects`), which may cause issues with NDK build tools.

## Core Technical Requirements & Architecture
- **Bluetooth HID (Human Interface Device)**:
  - The Android app must emulate a standard Bluetooth HID Keyboard and Mouse/Touchpad (using Android `BluetoothHidDevice` / BLE HID profile).
  - **PC Requirement**: Zero host-side software installation, scripts, or apps required on the PC. The PC detects the phone as a native Bluetooth peripheral device out of the box.
- **Features**:
  - Phone acts as a multi-touch touchpad (cursor movement, click, right-click, scrolling).
  - Phone acts as a full hardware/software keyboard with standard key modifiers (Ctrl, Alt, Shift, Win, Fn) and hotkey shortcuts.

## UI / Design Language & Aesthetic Guidelines
- **Design Inspiration**: Dot-matrix typography, monochrome / dark mode minimalist aesthetic, sleek tactile UI components, red status accent indicators, crisp line borders, and retro-futuristic design.
- **CRITICAL NAMING RULE**: NEVER use the name/string `"Nothing"` or `"nothing"` anywhere in the codebase, package names, layout files, assets, variable names, documentation, or attributes. Use descriptive terms like `Monochrome`, `DotMatrix`, `Glyph`, `Tactile`, or `Minimal`.
