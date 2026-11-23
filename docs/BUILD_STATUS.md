# Bloom TV OS Lite build status

Last verified: 2026-11-23

- GitHub Actions run #109 succeeded on workflow commit `a08e93d7`: unit tests, `:app:assembleDebug`, APK artifact upload and preview release publishing all passed. Preview release v0.1.109 is published and is the latest release.
- Run #108 succeeded on code commit `218f5faf`: unit tests, debug APK build, artifact upload and preview-release publishing passed.
- Runs #106 and #107 also succeeded; both passed unit tests, debug APK build, artifact upload and preview-release publishing.
- Cancellation-safety change: `runBloomCancellable` wraps ordinary exceptions in `Result` but rethrows `CancellationException`, so cancelled update checks and APK download/install operations stop instead of being treated as normal failures. Checking/downloading flags reset in `finally`.
- Added three unit tests for success, ordinary exception and cancellation propagation. Standalone Kotlin smoke checks: **3 assertions passed**.
- Preview-release publishing is serialized with a concurrency group that cancels older queued/running publish jobs, preventing stale previews from racing to become latest. Local YAML smoke check: **2 assertions passed**.
- Local Android build: **NOT RUN**; this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater downloads a release APK and hands it to Android's package installer for user approval. It is not an OTA or firmware updater, and the installation flow has not been verified on hardware.

PR #1 remains open and diverged from `main`; GitHub reports merge conflicts. No merge or rebase was performed.
