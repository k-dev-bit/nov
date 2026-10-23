# NOVA AI — Hybrid Android AI Assistant

NOVA is a personal AI assistant built for Android using Kotlin and Jetpack Compose. It integrates cloud-based AI providers (Google Gemini, Groq Cloud, Pollinations AI, custom OpenAI-compatible endpoints), local offline models (Ollama, LM Studio), real-time multilingual voice conversation (English, Hindi, Hinglish), text-to-image synthesis, and local Android phone control.

---

## Architecture & Technology Stack

- **Architecture:** Clean MVVM (Model-View-ViewModel) + Repository & Coordination Engine
- **Language:** 100% Kotlin with Coroutines and Flow
- **UI Framework:** Jetpack Compose with Material Design 3 (Dark/Light cyber aesthetic)
- **Local Database:** Room Database with full offline conversation history, caching, and pinning
- **Preferences:** Thread-safe state-backed PreferencesManager for reactive settings
- **Voice Engine:** Android `SpeechRecognizer` for low-latency STT and `TextToSpeech` with speed/pitch controls
- **Image Generation:** Neural image synthesis studio with 10+ artistic styles, multiple aspect ratios, and direct saving to Android `MediaStore` gallery
- **Phone Action Engine:** Local Android Intent System for deep links, app querying, and system hardware manipulation (Camera flashlight, BatteryManager, Settings, Alarm/Timer, dialer, SMS)

---

## 5 Main Sections

1. **Chat Screen**
   - Real-time conversation stream with provider & model badges
   - Markdown rendering with code blocks and one-tap copy
   - Read-aloud, regenerate, copy, and share actions
   - History drawer with search, rename, delete, and pin features
   - Fast provider switcher chip (Auto Coordinator, Gemini, Groq, Pollinations, Custom, Local)

2. **Voice Assistant Screen**
   - Interactive, dynamic glowing `VoiceOrb` reacting to speech volume and state (Listening, Thinking, Speaking, Idle)
   - Multilingual support: English (`en-US`), हिन्दी (`hi-IN`), and Hinglish (`en-IN`)
   - Adjustable speaking rate and instant interruption button

3. **Image Studio**
   - Text-to-image synthesis powered by Flux
   - 10 artistic styles (Photorealistic, Anime, 3D Render, Cinematic, Digital Art, Watercolor, Pixel Art, etc.)
   - Aspect ratio selection (1:1 Square, 16:9 Landscape, 9:16 Portrait, 4:3 Standard)
   - Save directly to Gallery via `MediaStore.Images`

4. **Phone Control Hub**
   - Direct shortcuts to launch YouTube, Chrome, Maps, Camera, Gmail, Dialer, SMS
   - Hardware toggles: Flashlight on/off, Battery level & charging status, Wi-Fi & Bluetooth settings
   - Natural Language Command Parser supporting Hindi, English, and Hinglish queries (e.g. *"YouTube kholo"*, *"Flashlight on karo"*, *"Battery kitni hai"*)

5. **AI Provider Manager & Settings**
   - **Google Gemini:** `gemini-3.5-flash` / `gemini-3.1-pro-preview`
   - **Groq Cloud:** Ultra-fast `llama-3.3-70b-versatile`
   - **Pollinations AI:** Free zero-configuration community models (Works out-of-the-box!)
   - **Custom OpenAI API:** Any `/v1/chat/completions` endpoint
   - **Local Inference:** Ollama / LM Studio on local Wi-Fi
   - Auto Fallback toggle and Offline-Only mode

---

## Build and Installation Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat (or compatible AGP 8.8+ / 9.0+)
- JDK 17 or higher
- Android SDK 36 (minSdk 24)

### Building the Debug APK
To assemble the debug APK from the command line:
```bash
gradle assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Installing on Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Running Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### Release Signing
Release signing configuration is pre-configured in `app/build.gradle.kts`. For custom releases:
```bash
gradle assembleRelease
```
Ensure `STORE_PASSWORD`, `KEY_PASSWORD`, and `KEYSTORE_PATH` environment variables are supplied.
