# TableNew

## State
Currently: Initial setup — AndroidX migration, GitHub Actions added, OnePlus 7 targeting

Last session: 2026-06-07

## Done
- Migrated dependencies from support library to AndroidX
- Added GitHub Actions workflow for APK builds
- Git repo initialized
- Compatible with OnePlus 7 (Android 10, API 29)

## Next
- Push to GitHub remote
- Test build on OnePlus 7 via wireless ADB or sideload from Actions artifact

## Key decisions
- minSdk 24, targetSdk 34, compileSdk 34
- Debug APK built on every push, uploaded as GitHub Actions artifact
- No signing config — debug builds only for now
