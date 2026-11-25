# Bloom TV OS Lite build status

Last verified: 2026-11-24

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

## 2026-11-24 — Launcher, update hand-off and workflow reliability

- App tiles now load the icon for the exact discovered launcher activity, falling back to the application icon when that activity has no icon. Icon state is keyed by package/activity so separate launch activities do not reuse the wrong image.
- APK download cleanup now covers failures and cancellation through the Android installer hand-off, while retaining a completed APK once the installer has actually launched.
- Focus requests use the cancellation-safe helper. Home now falls back to the Store focus target if its first app focus request cannot be made.
- The release job receives the artifact ID directly from the build job and has explicit actions:read permission, avoiding a name-based artifact lookup race.
- GitHub Actions run #115 passed unit tests, debug APK build, artifact upload and preview release publishing on code commit 1397ed74.
- Run #116 passed unit tests, debug APK build, artifact upload and release-job execution on workflow commit 3bc9ac19. Run #117 also passed tests, debug APK build, artifact upload and release-job execution on code/documentation head a2fc15dc.
- The v0.1.116 and v0.1.117 release records initially had no APK asset: downloading by artifact ID placed the APK in a nested artifact directory, so the dist/*.apk upload glob matched nothing. Workflow run #118 passed after adding merge-multiple: true; it uploaded app-debug.apk to preview release v0.1.118.
- Preview release v0.1.118 is verified as latest and contains app-debug.apk (16,604,362 bytes; SHA-256 2e212ddd3496ad98baa41048c2a8ad2c780f322c533cfe72c271e3b8d7137f6c).
- Runs #110 and #111 passed tests/build/artifact upload but the release job could not resolve the artifact by name; explicit artifact-ID hand-off fixed that lookup. Runs #113 and #114 caught a return-type mistake in the focus helper; it was corrected before run #115.
- Static source-contract checks for activity icons, installer cleanup, focus fallback, artifact-ID wiring and APK flattening: 9/9 passed.
- Local Android build: NOT RUN; no local Gradle installation or Android SDK is available. Physical Android TV / Google TV and emulator tests remain NOT RUN.

## 2026-11-25 — TV focus request result correction

- Found that `requestBloomFocus` used `Result.isSuccess`, which reports whether the call threw rather than whether `FocusRequester.requestFocus()` returned true. A normal `false` result was therefore incorrectly treated as granted focus, preventing the Home screen's Store fallback from running when the first app target declined focus.
- Changed the helper to return the Boolean carried by the Result. Ordinary failures return false; `CancellationException` still propagates through `runBloomCancellable`.
- Added unit tests for granted, declined and failed focus requests.
- GitHub Actions run #119 failed at Kotlin compilation because the generic result type was not inferred at the focus helper call. The call now explicitly specifies `Boolean`; run #121 is validating the correction.
- Local Android build: **NOT RUN**; no Gradle installation or Android SDK is available. Physical Android TV / Google TV and emulator tests remain NOT RUN.
- PR #1 remains open, 104 commits ahead and 29 behind current `main`; GitHub reports merge conflicts. No merge, rebase or push to `main` was performed.


## 2026-11-25 — TV focus target sizing

- Increased the shared Bloom Back button and About-screen Back button to 48 dp, raised device-settings action buttons to 48 dp, and enlarged theme swatches to 48 dp with more legible labels.
- These changes give remote/touch accessibility targets a more forgiving minimum size while retaining the existing focus border and selected-state semantics.
- GitHub Actions validation for the latest code commit: **PENDING**.
