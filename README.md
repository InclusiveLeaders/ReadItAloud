# Read It Aloud

> Point your camera. Tap once. Hear it read aloud.

**Read It Aloud** is an Android accessibility app that reads printed and on-screen text aloud using your device's camera. Designed for adults with reduced vision — no account, no internet, no complexity.

---

## What it does

1. Open the app → see a full-screen camera preview, hear *"Ready."*
2. Point your phone at any text — newspaper, label, screen, sign
3. Tap the large **READ** button → hear the text read aloud instantly

The entire workflow takes under 8 seconds. Everything runs on-device. No data ever leaves your phone.

---

## Key Features

- **Live camera preview** — full-screen on launch, no navigation required
- **On-device OCR** — recognises printed and screen text fully offline
- **Text-to-speech playback** — auto-starts after capture; play, pause, stop, restart
- **Adjustable speech rate** — 0.8× to 1.4×, with voice confirmation
- **Torch toggle** — manage glare and low light without leaving the main screen
- **Volume button shortcut** — control playback without looking at the screen
- **TalkBack support** — all controls navigable via Android accessibility services; major state changes announced by voice
- **Large touch targets** — 72dp minimum, designed for low-vision use

---

## Requirements

- Android 8.0 (API 26) or higher
- Rear camera (mandatory)
- No internet connection required or used

---

## Building the App

1. Clone the repository:
   ```
   git clone https://github.com/InclusiveLeaders/ReadItAloud.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer recommended)
3. Connect an Android device or start an emulator (API 26+)
4. Run the app via **Run → Run 'app'**

> On first launch, the app will request camera permission. The app cannot function without it.

---

## Roadmap

### v1.0 — MVP (in progress)
- [x] Live camera preview
- [x] On-device text recognition
- [x] TTS auto-playback with controls and rate adjustment
- [x] Custom Compose component library
- [ ] WCAG AAA visual design system
- [ ] TalkBack navigation and voice state prompts
- [ ] Torch toggle
- [ ] Volume button playback shortcut
- [ ] Settings screen (speech rate default, voice selection, auto-flash)

### v1.1 — Smart Capture
- [ ] 3-frame burst capture with best-frame selection
- [ ] Tap-to-focus
- [ ] Blur detection with user guidance

---

## License

MIT License — see [LICENSE](LICENSE) for details.

Copyright (c) 2026 Simonetta Batteiger / Inclusive Leaders
