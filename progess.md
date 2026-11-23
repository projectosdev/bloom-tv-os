# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-23

## Current status

**Stage:** Settings, launcher reliability, accessibility, focus, update-preview improvements, launcher-query correction and cancellation-safe update operations on PR #1.  
**Build status:** GitHub Actions run #105 passed tests, debug APK build, artifact upload and preview release publishing on commit `31874834`. The cancellation-safety changes on `218f5faf` are awaiting CI.  
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
- Update checks and installer hand-offs now preserve `CancellationException` rather than swallowing coroutine cancellation; ordinary exceptions are surfaced as `Result` failures. Three regression tests cover success, failure and cancellation.
- GitHub Actions builds and uploads a debug APK; feature-branch code pushes publish preview releases.

## Validation

- GitHub Actions run #105: **SUCCESS** on commit `31874834`. Unit tests, debug APK build, artifact upload and preview release publishing passed.
- Current cancellation-safety code commit `218f5faf` has not yet completed CI.
- Added 3 unit tests for the cancellation-safe result helper. Standalone Kotlin smoke assertions also passed locally.
- Run #102: **SUCCESS** on `55107663`; unit tests, APK build, artifact upload and preview release v0.1.102 passed.
- Runs #100 and #101 failed at Kotlin compilation because the focus/icon-loading refactor treated `requestFocus()` as returning Boolean and smart-cast a delegated bitmap. Both issues were corrected in `55107663`; the subsequent runs passed.
- Local pure-Kotlin profile smoke checks: **5 assertions passed**. Earlier checks covered version comparison, trusted release URLs, app sorting and update status.
- Local Android build: **NOT RUN**, because this environment has no Gradle installation or Android SDK.
- No Android TV emulator or physical-device test has been run.

## Known limitations

- This is an installable Android launcher app, not a bootable TV OS image.
- App availability, streaming-service support, DRM and store installation are controlled by Android, device certification, publishers and Google Play.
- Preview APKs use debug signing. Hosted runner debug keys may differ between builds, so Android may require an older preview to be uninstalled before a newer one can be installed.
- D-pad focus, Back behaviour, app discovery/launching and update installation still need hands-on testing on target TV hardware.
- PR #1 is open but not mergeable at present: it is diverged from current `main`, with 23 commits behind and merge conflicts, and GitHub reports conflicts. The branch was not merged or rebased into main.

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

- Added `BloomCancellableResult.kt` and three tests to preserve cancellation while surfacing ordinary errors.
- Wired the helper into automatic/manual update checks and APK download/install hand-offs; busy/checking flags reset in `finally` blocks.
- Added job-level preview release concurrency to `.github/workflows/android-debug.yml` so newer pushes supersede stale publish jobs.
- Run #108 passed tests, debug build, artifact upload and preview release on `218f5faf`. Run #109 is validating the workflow change.
- Local Android build and TV hardware/emulator testing remain NOT RUN: no Gradle/Android SDK or device is available.

## 2026-11-23 — Validation complete

- Run #109 passed the workflow concurrency change and published preview v0.1.109; it is verified as the latest release.
- Runs #106–#108 passed unit tests, debug APK builds, artifact uploads and preview-release publishing.
- The code commit `218f5faf` passed run #108 after the cancellation-safe update changes.
- The local Android build and physical TV/emulator tests remain NOT RUN because no Gradle/Android SDK or device is available.
