# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-22

## Current status

**Stage:** Settings, launcher reliability, accessibility, focus, update-preview improvements and launcher-query correction on PR #1.  
**Build status:** GitHub Actions run #104 passed tests, debug APK build, artifact upload and preview release v0.1.104 on commit `37ded1fe`.  
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
- GitHub Actions builds and uploads a debug APK; feature-branch code pushes publish preview releases.

## Validation

- GitHub Actions run #104: **SUCCESS** on commit `37ded1fe`. Unit tests, debug APK build, artifact upload and preview release v0.1.104 passed.
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
- PR #1 is open but not mergeable at present: it is 80 commits ahead and 23 behind current `main`, and GitHub reports conflicts. The branch was not merged or rebased into main.

## Next steps

1. Resolve the PR #1 conflicts against current `main` without losing the settings/update work.
2. Test directional navigation, focus visibility, accessibility announcements and Back behaviour on Android TV / Google TV.
3. Test installed-app discovery and launching on more than one Android TV device.
4. Test the release-check, download and Android installation-approval flow on hardware.
5. Decide on a stable signing and release process before distributing builds beyond preview testing.
6. Treat standalone firmware work as a separate platform project with a chosen target device.
