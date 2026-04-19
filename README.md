# CodeLab — Mobile Coding Education App

Android app (Java) implementing the design from the spec, integrated with the
Live Code Execution backend at `https://live-code-execution-api.onrender.com`.

## Screens

- **Home** — greeting, "Continue Learning" card wired to your active pack and next
  lesson, Quick Access tiles (Playground / Study / Marketplace), real Recent Sessions
  list.
- **Code Playground** — full-screen editor with line numbers, file tab, language tag.
  Run Code → real execution against the backend with status polling and console output.
  Sessions auto-saved locally so you can reopen them from Home.
- **Study Mode** — your active lesson pack at the top with real progress, filter chips
  (All / In Progress / Completed) that work against actual lesson state, marketplace
  button to install more packs.
- **Active Lesson** — task + hint callouts (per lesson), code editor seeded with
  the lesson's starter code, Run / Submit buttons. Submit checks output against
  the lesson's expected output and marks the lesson complete + awards XP on success.
- **Profile** — avatar with editable color variant, real name/handle/bio, real XP /
  Streak / Tasks counters, dynamic achievements (locked vs unlocked), real recent
  activity feed. Edit and Settings buttons in the top right.
- **Marketplace** — browse all available lesson packs from the catalog and install /
  uninstall them. Built-in packs always installed.
- **Edit Profile** — name, handle, bio, avatar color picker.
- **Settings** — theme (dark/light/system), manage packs (links to marketplace),
  app language preference, default coding language, backend URL override (great for
  local backends), reset all data, and an About section with version + backend.

Bottom navigation: Home · Study · Code · Profile.

## What's stored where (no hardcoded user data)

| Data                     | Where                  | Persistence                |
|--------------------------|------------------------|----------------------------|
| User profile             | `ProfileStore`         | SharedPreferences (JSON)   |
| Lesson progress          | `ProgressStore`        | SharedPreferences (sets)   |
| Recent sessions (local)  | `RecentSessionStore`   | SharedPreferences (JSON)   |
| Installed packs          | `PackRepository`       | SharedPreferences (set)    |
| Settings (theme/lang/URL)| `SettingsStore`        | SharedPreferences          |
| Lesson pack content      | `assets/packs/*.json`  | App assets                 |
| Marketplace catalog      | `assets/packs/catalog.json` | App assets            |
| Achievements             | `AchievementEngine`    | Computed from above        |
| Backend session id       | `RecentSession.backendSessionId` | Server + local mirror |

When you add a backend for packs/profiles later, swap the implementation of
`PackRepository`, `ProfileStore`, etc. The rest of the app calls them through
the same interface and won't change.

## Backend integration

Per your live-code-execution backend's spec, the app uses these endpoints:

- `POST /api/v1/code-sessions` — create a session
- `PATCH /api/v1/code-sessions/{id}` — autosave source (with optimistic versioning)
- `POST /api/v1/code-sessions/{id}/run` — submit for execution
- `GET /api/v1/executions/{id}` — poll for `QUEUED → RUNNING → COMPLETED/FAILED/TIMEOUT`

Per-device UUID `user_id` and `simulation_id` are generated on first launch and
stored in SharedPreferences. The base URL is overridable in Settings; the Retrofit
client rebuilds itself when changed.

## Architecture

- **Java 17**, `minSdk 24`, `targetSdk 34`
- **Retrofit 2 + OkHttp + Gson** for the backend
- **Material Components** for theming + bottom nav
- **AndroidX Fragments** for the 3 tab screens; separate Activities for
  Playground, Lesson, Marketplace, Profile Edit, Settings
- All long-lived state in singletons backed by SharedPreferences

## Building

1. Open the project in **Android Studio** (File → Open → select this folder).
2. Sync Gradle (downloads Gradle 8.4 + AGP 8.2 + dependencies on first run).
3. **Run** (Shift+F10) on a device or emulator (API 24+).

If Android Studio complains about a missing wrapper jar, **File → Sync Project
with Gradle Files** will fix it automatically.

## Backend cold start

The Render free-tier backend cold-starts after inactivity. The first Run tap may
sit at "Starting…" for 20–30 seconds while the dyno wakes up. After that, simple
Java programs finish in 1–5 seconds.

## Project structure

```
app/src/main/
├── AndroidManifest.xml
├── assets/packs/                         JSON lesson packs + catalog
├── java/com/codelab/app/
│   ├── CodeLabApp.java                   Application class (applies theme)
│   ├── api/                              Retrofit interface, client (dynamic URL)
│   │   └── dto/                          Request/response models
│   ├── data/
│   │   ├── Lesson.java                   Lesson model + Status enum
│   │   ├── LessonPack.java               Pack model
│   │   ├── CatalogEntry.java             Marketplace entry
│   │   ├── Achievement.java              Achievement model
│   │   ├── UserProfile.java              Profile (name, xp, streak, …)
│   │   ├── RecentSession.java            Real session record
│   │   ├── PackRepository.java           Loads & manages packs
│   │   ├── ProgressStore.java            Lesson completion state
│   │   ├── ProfileStore.java             User profile + XP/streak/level
│   │   ├── RecentSessionStore.java       Persists recent sessions
│   │   ├── SettingsStore.java            Theme / language / backend URL
│   │   ├── AchievementEngine.java        Computes achievements
│   │   └── LessonRepository.java         Facade over PackRepository + ProgressStore
│   ├── ui/
│   │   ├── MainActivity.java
│   │   ├── PlaygroundActivity.java
│   │   ├── home/                         HomeFragment + adapter
│   │   ├── study/                        StudyFragment + adapter
│   │   ├── lesson/                       LessonActivity
│   │   ├── market/                       MarketplaceActivity + adapter
│   │   ├── profile/                      ProfileFragment + ProfileEditActivity
│   │   └── settings/                     SettingsActivity
│   └── util/                             CodeRunner (execution orchestrator) + Prefs
└── res/                                  layouts, drawables, colors, strings, etc.
```

## Adding a new lesson pack

1. Drop a JSON file in `app/src/main/assets/packs/<id>.json`. Use the same
   schema as `java-fundamentals.json` (id, title, description, language,
   difficulty, sizeKb, builtIn, lessons[]).
2. Add an entry to `assets/packs/catalog.json` so it appears in the Marketplace.
3. Done — no code changes needed.

When you add a real backend for packs, replace `PackRepository` with a
Retrofit-backed implementation that downloads pack JSONs and caches them. The
rest of the app keeps working unchanged.

## Adding a new achievement

Add a string + condition to `AchievementEngine.computeAll(...)`. The Profile
screen reflects it automatically the next time it resumes.
