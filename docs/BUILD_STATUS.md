# Bloom TV OS Lite build status

Last verified: 2026-11-23

- GitHub Actions run #108 succeeded on code commit `218f5faf`: unit tests, `:app:assembleDebug`, APK artifact upload and preview-release publishing all passed.
- Run #107 succeeded on `74dde204`, and run #106 succeeded on `68c25b93`; both passed unit tests, debug APK build, artifact upload and preview-release publishing.
- Cancellation-safety change: `runBloomCancellable` wraps ordinary exceptions in `Result` but rethrows `CancellationException`, so cancelled update checks and APK download/install operations stop instead of being treated as normal failures. Checking/downloading flags reset in `finally`.
- Added three unit tests for success, ordinary exception and cancellation propagation. Standalone Kotlin smoke checks: **3 assertions passed**.
- Preview release workflow now serializes publishing and cancels older queued/running preview-publish jobs, preventing an older run from marking its APK latest after a newer preview. Local YAML smoke check: **2 assertions passed**.
- Run #109 is validating the workflow change; unit tests passed and the debug build was in progress at last check.
- Local Android build: **NOT RUN**; this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater downloads a release APK and hands it to Android's package installer for user approval. It is not an OTA or firmware updater, and the installation flow has not been verified on hardware.

PR #1 remains open and diverged from `main`; GitHub reports merge conflicts. No merge or rebase was performed.
