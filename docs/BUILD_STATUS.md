# Bloom TV OS Lite build status

Last verified: 2026-11-21

- GitHub Actions run #103 succeeded on code commit `8da069ce`: unit tests, `:app:assembleDebug`, debug APK artifact upload, and preview-release publishing all passed.
- Run #102 also succeeded on commit `55107663`, including preview release v0.1.102.
- Run #103 published preview release v0.1.103 with `app-debug.apk` (about 16.6 MB). These are debug-signed test builds, not production releases.
- Runs #100 and #101 failed during Kotlin compilation after a focus/icon-loading refactor. The `FocusRequester.requestFocus()` Unit return and delegated bitmap smart-cast errors were fixed in `55107663`; runs #102 and #103 passed afterward.
- Local pure-Kotlin smoke checks: **5 profile-catalog assertions passed**. Previous local checks also covered version comparison, trusted release URLs, app sorting and update status.
- Local Android build was **NOT RUN**: this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater downloads a release APK and hands it to Android's package installer for user approval. It is not an OTA or firmware updater, and the installation flow has not been verified on hardware.

PR #1 remains open. It is **77 commits ahead and 23 commits behind** current `main`; GitHub reports merge conflicts. No merge or rebase was performed.
