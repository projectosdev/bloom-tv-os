# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-25

## Current status

**Stage:** Settings, launcher reliability, accessibility, cancellation-safe focus, activity-specific app icons and safer APK installer hand-off on PR #1.  
**Build status:** GitHub Actions runs #115–#118 passed unit tests, debug APK builds, artifact uploads and preview release publishing. Preview v0.1.118 is verified as latest and contains app-debug.apk.  
**Device status:** No physical Android TV / Google TV or emulator test has been recorded.

Bloom is a TV-first Android launcher built with Kotlin and Jetpack Compose. It is not replacement firmware or a complete standalone TV operating system.

## Implemented

- Native Android TV / Google TV launcher entry points.
- Installed-app discovery through Android launcher intents, with launch requests handled by Android. Activities are deduplicated by package/component, TV category is preferred, and launch targets the exact discovered activity before Android launcher fallbacks. Queries use flags `0` so launcher filters that omit `CATEGORY_DEFAULT` are not hidden by `MATCH_DEFAULT_ONLY`; enabled/exported/title eligibility is covered by unit tests.
- App discovery runs off the UI thread, refreshes on resume, filters disabled/unexported activities and exposes loading/error states.
- App icons are loaded and rasterized asynchronously; long app labels are kept to one line in home tiles.
- Home focus is grouped and restored only when the home screen needs focus, avoiding focus stealing while app discovery finishes. Header actions and theme swatches use focus groups.
- Local profile names are trimmed, sorted deterministically and deduplicated case-insensitively.
- The Store screen uses the shared frame-aware focus helper.
- Home screen, app library, Bloom Search, curated store listings and Android settings shortcuts.
- Local Bloom profiles, accent themes, home layout preferences and a quick settings panel.
- Bloom Ambient screen and motion preferences; screen/focus transitions now honour Reduce Motion.
- Initial focus targets for Control Centre and Ambient, labelled switches and accessible theme-selection semantics.
- Settings, About and Software Update screens.
- Software Update checks the project's GitHub Releases feed, compares dotted versions, accepts only the expected APK from the trusted release path, downloads it and hands it to Android's package installer for explicit user approval.
- Failed/cancelled downloads are removed. Update installation remains user-controlled; this is not an OTA firmware updater.
- Update checks and APK installer hand-offs preserve coroutine cancellation instead of swallowing `CancellationException`; checking/downloading flags reset in `finally` blocks.
- Preview-release publishing now has a concurrency group that cancels older queued/running publish jobs so stale previews cannot race to become latest.
- Update checks and installer hand-offs preserve `CancellationException` rather than swallowing coroutine cancellation; ordinary exceptions are surfaced as `Result` failures. Three regression tests cover success, failure and cancellation.
- GitHub Actions builds and uploads a debug APK; feature-branch code pushes publish preview releases.

## Validation

- GitHub Actions run #118: **SUCCESS** on workflow commit `e1ed22d0`. Unit tests, debug APK build, artifact upload, artifact-ID download and preview release publishing passed. Preview v0.1.118 contains `app-debug.apk`.
- Runs #115–#117: unit tests and debug APK builds passed; #116 and #117 exposed the nested artifact-path issue that was fixed in #118.
- Static source-contract checks: **9/9 passed**. Earlier standalone Kotlin checks covered cancellation, profile sorting, version comparison, trusted release URLs, app sorting and update status.
- Local Android build: **NOT RUN**, because this environment has no Gradle installation or Android SDK.
- No Android TV emulator or physical-device test has been run.

## Known limitations

- This is an installable Android launcher app, not a bootable TV OS image.
- App availability, streaming-service support, DRM and store installation are controlled by Android, device certification, publishers and Google Play.
- Preview APKs use debug signing. Hosted runner debug keys may differ between builds, so Android may require an older preview to be uninstalled before a newer one can be installed.
- D-pad focus, Back behaviour, app discovery/launching and update installation still need hands-on testing on target TV hardware.
- PR #1 is open but not mergeable at present: it is diverged from current `main`, with 29 commits behind and merge conflicts. The branch has not been merged or rebased into `main`.

## Next steps

1. Resolve the PR #1 conflicts against current `main` without losing the settings/update work.
2. Test directional navigation, focus visibility, accessibility announcements and Back behaviour on Android TV / Google TV.
3. Test installed-app discovery and launching on more than one Android TV device.
4. Test the release-check, download and Android installation-approval flow on hardware.
5. Decide on a stable signing and release process before distributing builds beyond preview testing.
6. Treat standalone firmware work as a separate platform project with a chosen target device.

## 2026-11-23 — Cancellation-safe update operations

- Added `BloomCancellableResult.kt` with a small helper that preserves coroutine cancellation and wraps ordinary exceptions.
- Replaced cancellation-swallowing `runCatching` around automatic/manual update checks and APK install hand-offs. Busy/checking flags are reset in `finally` blocks.
- Added `BloomCancellableResultTest.kt` for success, failure and cancellation.
- GitHub Actions run #105 on the prior commit `31874834` succeeded. The new code head `218f5faf` still needs CI.
- Local Android build and device tests remain NOT RUN: no Gradle/Android SDK or Android TV hardware/emulator is available.

## 2026-11-23 — Update cancellation and release ordering

- Added `BloomCancellableResult.kt` and three tests to preserve cancellation while surfacing ordinary exceptions.
- Wired the helper into automatic/manual update checks and APK download/install hand-offs; checking/downloading flags reset in `finally` blocks.
- Added job-level preview release concurrency to `.github/workflows/android-debug.yml` so newer pushes supersede stale publish jobs.
- Run #108 passed tests, debug build, artifact upload and preview release on `218f5faf`. Run #109 validated the workflow change and published preview v0.1.109.
- Local Android build and TV hardware/emulator testing remain NOT RUN: no Gradle/Android SDK or device is available.

## 2026-11-23 — Validation complete

- Run #109 passed the workflow concurrency change and published preview v0.1.109; it was verified as the latest release at that point.
- Runs #106–#108 passed unit tests, debug APK builds, artifact uploads and preview-release publishing.
- The code commit `218f5faf` passed run #108 after the cancellation-safe update changes.
- The local Android build and physical TV/emulator tests remain NOT RUN because no Gradle/Android SDK or device is available.

## 2026-11-24 — Launcher and update reliability

- AppTile now requests the exact discovered activity icon, with an application-icon fallback; its asynchronous state is keyed by package/activity rather than package alone.
- APK download cleanup now covers cancellation and failures until Android's installer activity has successfully launched. A completed APK is not removed after the installer hand-off succeeds.
- Shared TV focus requests now preserve coroutine cancellation. The Home screen attempts its first app tile, then falls back to the Store action if that focus request cannot be made.
- The preview-release workflow passes the upload-artifact ID as a job output and uses actions:read in the release job before downloading that exact artifact.
- Run #115 passed tests, debug APK build, artifact upload and preview release v0.1.115 on commit `1397ed74`.
- Run #116 passed tests/build/artifact upload and completed the release job, but v0.1.116 had no APK asset because the downloaded file was nested below dist. Run #117 had the same asset-path issue.
- Run #118 passed unit tests, debug APK build, artifact upload, artifact-ID download and release publishing. Preview v0.1.118 contains `app-debug.apk` (16,604,362 bytes; SHA-256 `2e212ddd3496ad98baa41048c2a8ad2c780f322c533cfe72c271e3b8d7137f6c`).
- Earlier run #110/#111 release steps failed to resolve an artifact by name even though upload had completed; explicit artifact-ID hand-off fixed that. Runs #113/#114 exposed a return-type mismatch in the focus helper; it was corrected before run #115.
- Static source-contract checks: **9/9 passed**. Local Android build remains NOT RUN because Gradle and the Android SDK are not installed in this environment. No emulator or physical TV test has been run.

## 2026-11-25 — TV focus request result correction

- Found a correctness bug in `requestBloomFocus`: `Result.isSuccess` treats `Result.success(false)` as a successful focus request. Compose can return false without throwing when the target cannot accept focus, so Home's Store fallback could be skipped.
- Changed the helper to inspect the returned Boolean. It now reports true only when Compose grants focus; ordinary failures return false, and cancellation still propagates.
- Added `TvFocusTest.kt` for granted, declined and failed focus requests.
- Run #119 exposed a Kotlin type-inference error in the focus helper call; the `runBloomCancellable<Boolean>` type argument is now explicit. Run #121 is validating the correction.
- Local Android build and TV device/emulator tests remain NOT RUN because this environment has no Gradle/Android SDK or test device.


## 2026-11-25 — TV focus target sizing

- Increased the shared Bloom Back button and About-screen Back button to 48 dp, device-settings action buttons to 48 dp, and theme swatches to 48 dp with larger labels.
- Kept the existing D-pad focus borders and theme radio-button selected semantics.
- Run #119 exposed a Kotlin type-inference error in the focus helper call; the `runBloomCancellable<Boolean>` type argument is now explicit. Run #121 is validating the correction.
