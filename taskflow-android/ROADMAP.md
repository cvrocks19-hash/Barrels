# TaskFlow — Dev Roadmap

> Source: `TaskFlow_Dev_Roadmap.pdf` (MKBHD Waveform + Capterra/G2/Trustpilot synthesis)

## Phase 1 — Foundation
- [x] **#1 Project architecture** — MVVM + Room + Hilt + ViewModel + coroutines
- [x] **#2 Task list screen** — Material 3, swipe-to-complete / swipe-to-delete + undo snackbar
- [x] **#3 Add task (< 10 seconds)** — bottom sheet + NLP parser (`ParseTaskTextUseCase`)
- [x] **#4 Notifications with full context** — WorkManager + BigTextStyle (Notion kill-zone fix)

## Phase 2 — Differentiators
- [x] **#5 Smart daily focus** — `observeDailyFocus()` surfaces top 5 by deadline + priority
- [ ] **#6 Recurring tasks** — `Recurrence` enum in schema; generation logic TBD
- [x] **#7 Home screen widget** — Glance API (`TaskFlowWidget`)
- [ ] **#8 Calendar + tasks unified view**
- [ ] **#9 Guilt-free deletion UX** — undo snackbar done; insight copy TBD

## Phase 3 — Polish
- [x] **#10 Onboarding** — Google Sign-In screen ("Your tasks. Every device.")
- [x] **#11 Beautiful UI** — Material 3 + Pixel dynamic color + `enableEdgeToEdge()`
- [x] **#12 Habits vs tasks** — `isHabit` field + separate `observeHabits()` query; UI tab TBD
- [ ] **#13 Swipe-to-reorder** (no drag-and-drop)
- [x] **#14 Priority levels** — LOW/MEDIUM/HIGH, color-coded bar in `TaskCard`

## Phase 4 — Launch
- [ ] **#15 Accessibility + large text**
- [x] **#16 Firebase Crashlytics** — wired in build
- [ ] **#17 Play Store submission**
- [ ] **#18 Monetization** — personal-only, careful free tier

## Cross-device sync (bonus)
- [x] **Google Sign-In** — Firebase Auth via `AuthRepository`
- [x] **Firestore sync** — `TaskSyncService` with offline persistence + real-time listener
- [x] **Conflict resolution** — last-write-wins via `updatedAt` timestamp
- [x] **UUID-stable IDs** — `syncId` prevents duplicate rows across devices

---
**13 / 18 roadmap tasks done or scaffolded. Next: #6 recurring generation, #9 deletion insight, #13 swipe-reorder, #15 accessibility.**
