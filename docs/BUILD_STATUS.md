# Bloom TV OS Lite build status

Last verified: 2026-11-23

- GitHub Actions run #105 succeeded on commit `31874834`: unit tests, `:app:assembleDebug`, debug APK artifact upload and preview release publishing all passed.
- New cancellation-safety change on commit `218f5faf`: update checks and APK-download/install hand-offs now use `runBloomCancellable`, which converts ordinary exceptions into failures but rethrows `CancellationException` so a cancelled Compose coroutine does not continue as though it had failed normally.
- Added three unit tests for successful results, ordinary exceptions and cancellation propagation.
- The current code head has not yet been validated by GitHub Actions.
- Launcher discovery regression fixed on `37ded1fe`: queries use flags `0` instead of `MATCH_DEFAULT_ONLY`, which can exclude launcher filters that omit `CATEGORY_DEFAULT`.
- Runs #102–#105 passed the Android unit-test and debug-APK build jobs; #104 and #105 also published preview debug releases.
- Earlier local pure-Kotlin smoke checks: 5 profile-catalog assertions passed; prior checks covered version comparison, trusted release URLs, app sorting and update status. The new cancellation helper was also checked locally with standalone Kotlin assertions.
- Local Android build: **NOT RUN**; this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater downloads a release APK and hands it to Android's package installer for user approval. It is not an OTA or firmware updater, and the installation flow has not been verified on hardware.

PR #1 remains open and diverged from `main`; GitHub reports merge conflicts. No merge or rebase was performed.
