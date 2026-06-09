# Firebase + Google Sign-In Setup

Before building, you need a Firebase project. This takes about 5 minutes.

## 1. Create a Firebase project

Go to [console.firebase.google.com](https://console.firebase.google.com) and create a project.

## 2. Add an Android app

- Package name: `com.taskflow.android`
- Nickname: TaskFlow
- **Add your debug SHA-1** (required for Google Sign-In):
  ```bash
  cd taskflow-android
  ./gradlew signingReport
  # Copy the SHA1 from the debug variant
  ```
- Download `google-services.json` → place it in `taskflow-android/app/`

## 3. Enable Google Sign-In

Firebase Console → **Authentication** → Sign-in method → **Google** → Enable

## 4. Enable Firestore

Firebase Console → **Firestore Database** → Create database → choose a region close to your users.

## 5. Set Firestore security rules

Paste these in the **Rules** tab — ensures users can only read their own tasks:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/tasks/{taskId} {
      allow read, write: if request.auth != null
                         && request.auth.uid == userId;
    }
  }
}
```

## 6. Build & run

```bash
cd taskflow-android
./gradlew assembleDebug
```

## How sync works

```
Device A (Room) ──push──► Firestore /users/{uid}/tasks/
                                    │
                          real-time listener
                                    │
                                    ▼
Device B (Room) ◄──merge─────────────
```

| Scenario | Behaviour |
|---|---|
| Online, same device | Write goes to Room + Firestore immediately |
| Offline | Write goes to Room; Firestore queues the write locally (offline persistence on) |
| New device sign-in | `initialSync()` pulls all tasks from Firestore first, then starts real-time listener |
| Conflict | `updatedAt` timestamp wins — most-recent write kept |
| Deleted on one device | Firestore delete propagates via snapshot listener; removed from Room |

## Data location

Firestore path: `/users/{firebase_uid}/tasks/{syncId}`

Each task has a `syncId` (UUID set on creation) that stays stable across devices, preventing duplicate rows.
