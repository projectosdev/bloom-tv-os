# Bloom TV OS Lite build status

Last verified: 2026-11-22

- GitHub Actions run #104 succeeded on commit `37ded1fe`: unit tests, `:app:assembleDebug`, debug APK artifact upload, and preview release v0.1.104 all passed.
- Launcher discovery regression fixed: `queryIntentActivities` now uses flags `0` instead of `MATCH_DEFAULT_ONLY`, which can exclude launcher filters that omit `CATEGORY_DEFAULT`. Tests cover the query policy and enabled/exported/label eligibility.
- Run #102 also succeeded on commit `55107663`, including preview release v0.1.102.
- Run #103 published preview release v0.1.103 with `app-debug.apk` (about 16.6 MB). These are debug-signed test builds, not production releases.
- Runs #100 and #101 failed during Kotlin compilation after a focus/icon-loading refactor. The `FocusRequester.requestFocus()` Unit return and delegated bitmap smart-cast errors were fixed in `55107663`; runs #102 and #103 passed afterward.
- Local pure-Kotlin smoke checks: **5 profile-catalog assertions passed**. Previous local checks also covered version comparison, trusted release URLs, app sorting and update status.
- Local Android build was **NOT RUN**: this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater downloads a release APK and hands it to Android's package installer for user approval. It is not an OTA or firmware updater, and the installation flow has not been verified on hardware.

PR #1 remains open, 80 commits ahead and 23 behind current `main`; GitHub reports merge conflicts. No merge or rebase was performed.
