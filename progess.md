# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-20

## Current status

**Stage:** Settings, launcher reliability, accessibility and update-preview improvements on PR #1.  
**Build status:** GitHub Actions run #94 passed tests and APK build on commit `de1cee33`. Run #95 exposed a Kotlin compile error in the new list-based discovery return; it is corrected in the current commit and run #97 is validating it.  
**Device status:** No physical Android TV / Google TV or emulator test has been recorded.

Bloom is a TV-first Android launcher built with Kotlin and Jetpack Compose. It is not replacement firmware or a complete standalone TV operating system.

## Implemented

- Native Android TV / Google TV launcher entry points.
- Installed-app discovery through Android launcher intents, with launch requests handled by Android. Activities are deduplicated by package/component, TV category is preferred, and launch targets the exact discovered activity before Android launcher fallbacks.
- App discovery runs off the UI thread, refreshes on resume, filters disabled/unexported activities and exposes loading/error states.
- Home screen, app library, Bloom Search, curated store listings and Android settings shortcuts.
- Local Bloom profiles, accent themes, home layout preferences and a quick settings panel.
- Bloom Ambient screen and motion preferences; screen/focus transitions now honour Reduce Motion.
- Initial focus targets for Control Centre and Ambient, labelled switches and accessible theme-selection semantics.
- Settings, About and Software Update screens.
- Software Update checks the project's GitHub Releases feed, compares dotted versions, accepts only the expected APK from the trusted release path, downloads it and hands it to Android's package installer for explicit user approval.
- Failed/cancelled downloads are removed. Update installation remains user-controlled; this is not an OTA firmware updater.
- GitHub Actions builds and uploads a debug APK; feature-branch code pushes publish preview releases.

## Validation

- GitHub Actions run #93: **SUCCESS** on commit `382427d9`. Unit tests, debug APK build and artifact upload passed; preview-release publishing was skipped because no app source changed.
- GitHub Actions run #94: **SUCCESS** on commit `de1cee33`; tests and APK build passed.
- GitHub Actions run #95: **FAILURE** during Kotlin compilation (`discovered.values` on a list). This was corrected in the current commit; run #98 is pending.
- GitHub Actions run #93: **SUCCESS** on commit `382427d9` (tests, build and artifact upload passed).
- Runs #86–#90 exposed compile errors in accessibility semantics and the theme-picker parameter; these were corrected before #91/#92.
- Local pure-Kotlin checks: **17 assertions passed** for version comparison, release-URL validation, app sorting and update-status resolution.
- Local Android build: **NOT RUN**, because this environment has no Gradle installation or Android SDK.
- No Android TV emulator or physical-device test has been run.

## Known limitations

- This is an installable Android launcher app, not a bootable TV OS image.
- App availability, streaming-service support, DRM and store installation are controlled by Android, device certification, publishers and Google Play.
- Preview APKs use debug signing. Hosted runner debug keys may differ between builds, so Android may require an older preview to be uninstalled before a newer one can be installed.
- D-pad focus, Back behaviour, app discovery/launching and update installation still need hands-on testing on target TV hardware.
- PR #1 is open but not mergeable at present: it is 23 commits behind current `main` and has conflicts. The branch was not merged or rebased into main.

## Next steps

1. Resolve the PR #1 conflicts against current `main` without losing the settings/update work.
2. Test directional navigation, focus visibility, accessibility announcements and Back behaviour on Android TV / Google TV.
3. Test installed-app discovery and launching on more than one Android TV device.
4. Test the release-check, download and Android installation-approval flow on hardware.
5. Decide on a stable signing and release process before distributing builds beyond preview testing.
6. Treat standalone firmware work as a separate platform project with a chosen target device.
