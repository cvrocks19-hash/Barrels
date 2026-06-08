# Task App

A mobile-first task management application built with React + Flask, designed for easy migration to Android/Google Play.

## Architecture

```
task-app/
├── backend/          # Flask REST API (Python)
│   ├── app.py        # CRUD endpoints + stats
│   └── requirements.txt
└── frontend/         # React + TypeScript (Vite)
    ├── src/
    │   ├── App.tsx
    │   ├── components/
    │   ├── api/
    │   └── types/
    └── package.json
```

## Running Locally

### Backend
```bash
cd task-app/backend
pip install -r requirements.txt
python app.py
# API at http://localhost:5001
```

### Frontend
```bash
cd task-app/frontend
npm install
npm run dev
# App at http://localhost:5173
```

## Android Migration Paths

### 1. Capacitor (easiest — wrap the React build as a native app)
```bash
npm install @capacitor/core @capacitor/cli @capacitor/android
npx cap init
npm run build
npx cap add android
npx cap open android   # opens Android Studio
```

### 2. React Native (same component model, full native performance)
- `div` → `View`, `p/span` → `Text`, `button` → `TouchableOpacity`
- Tailwind → StyleSheet (or use NativeWind for identical class names)
- Shared business logic and API layer stay unchanged

### 3. Backend for Android
- Deploy Flask API to **Google Cloud Run** or **Firebase Functions**
- Or replace with Android **Room database + ViewModel** for fully offline-first

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/tasks | List tasks (supports ?status, ?priority, ?category, ?q) |
| POST | /api/tasks | Create task |
| GET | /api/tasks/:id | Get single task |
| PUT | /api/tasks/:id | Update task |
| DELETE | /api/tasks/:id | Delete task |
| GET | /api/stats | Task counts by status |
