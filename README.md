# All-in-One Toolbox (Android · Kotlin · Jetpack Compose)

> Package: `com.mmushtaq.orm.allinone`  
> A modern, elegant utility app bundling everyday tools into one lightweight package:
>
> **Compass • Torch • Bubble Level • Ruler • Sound Meter • Unit Converter**

---

## Table of Contents
- [Features](#features)
- [Screens](#screens)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Permissions](#permissions)
- [How It Works (per module)](#how-it-works-per-module)
- [App Icon](#app-icon)
- [Screenshots](#screenshots)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Compass** — smooth heading; optional location/declination read-out.
- **Torch** — big glowing toggle, **SOS (… --- …)** mode; lifecycle-safe (auto-off on background).
- **Bubble Level** — horizontal/vertical leveling with calibration and sensitivity tweaks.
- **Ruler** — cm/inch ticks, draggable **A/B markers**, quick DPI calibration.
- **Sound Meter** — live **RMS/Peak** dBFS-ish readout + waveform; calibration offset.
- **Unit Converter** — offline categories: Length, Mass, Temperature, Area, Volume, Speed, Time, Data, Angle. Searchable pickers, swap, and quick table.
- **Home & Navigation** — Compose `NavHost` with clean routes and back-stack behavior.
- **Adaptive App Icon** — gradient background, compass glyph, monochrome for Android 13+.
- **Offline** — everything runs on-device; minimal dependencies.

---

## Screens

| Route       | Screen           | Notes |
|------------:|------------------|-------|
| `home`      | HomeScreen       | Tiles/cards for all tools |
| `compass`   | CompassScreen    | Sensors; optional location |
| `torch`     | TorchScreen      | Camera flash toggle + SOS |
| `bubble`    | BubbleLevel      | Accelerometer-based level |
| `ruler`     | RulerScreen      | Long scroll; A/B markers; calibration |
| `sound`     | SoundScreen      | AudioRecord; waveform; calibration |
| `converter` | ConverterScreen  | Segmented categories; results table |

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture/State:** MVVM (`ViewModel`), Compose state/`StateFlow`
- **Navigation:** `androidx.navigation.compose`
- **Sensors/Hardware:** `SensorManager`, `CameraManager#setTorchMode`, `AudioRecord`
- **Min SDK:** 23 (Android 6.0) — adjust as needed

---

## Architecture

- Each feature is **self-contained**: `Screen` + `ViewModel` + UI state.
- **Torch**
  - Picks a back camera with flash; uses `CameraManager.setTorchMode`.
  - Listens to `TorchCallback` for truth; turns off on `ON_STOP/ON_DESTROY`.
  - SOS coroutine plays Morse (… --- …) with cancel-only default (no double-toggle).
- **Sound**
  - Recording loop on `Dispatchers.Default`; UI updates **throttled (~25 fps)** to avoid ANRs.
  - Waveform is downsampled; calibration offset applied to displayed dB.
- **Ruler**
  - Uses real `densityDpi`; supports scale calibration; dual draggable markers show start/end/Δ.
- **Converter**
  - Pure offline `ConverterEngine` maps units to a base (meters, seconds, etc.); temperature is non-linear (C/F/K).

---
MIT License

Copyright (c) 2025 Khurram Mushtaq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

