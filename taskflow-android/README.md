# TaskFlow Android

Native Android task app — Kotlin + Jetpack Compose + Material 3.
Based on the `TaskFlow_Dev_Roadmap.pdf` (MKBHD Waveform + review-site synthesis).

## Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow + Hilt |
| Database | Room (SQLite) |
| Background | WorkManager (notifications) |
| Widget | Glance API |
| Crash reporting | Firebase Crashlytics |

## Open in Android Studio

1. Clone the repo and check out `claude/task-app-android-google-owvwy3`
2. Open the `taskflow-android/` folder in Android Studio Hedgehog or newer
3. Let Gradle sync
4. Run on a Pixel emulator or device (API 26+)

## Key design decisions

- **NLP add** (`ParseTaskTextUseCase`): “Call dentist tomorrow at 3pm” auto-sets date, time, and priority — no form filling.
- **Full-context notifications**: `BigTextStyle` ensures the full task title is never truncated (Notion kill-zone fix).
- **Daily Focus section**: 5 highest-priority / most-urgent tasks surface at the top of the list each morning.
- **Swipe gestures**: right to cycle status, left to delete with undo snackbar.
- **Glance widget**: top-5 tasks visible and tappable from the home screen without opening the app.
- **Dynamic color**: automatically matches Pixel wallpaper palette (Material You).

## Roadmap progress

See [ROADMAP.md](./ROADMAP.md).
