# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-15

## Current status

**Stage:** Android TV launcher preview  
**Build status:** GitHub Actions successfully built the current pull-request head as a debug APK.  
**Device status:** No physical Android TV / Google TV or emulator test has been recorded.

Bloom is a TV-first Android launcher built with Kotlin and Jetpack Compose. It is not replacement firmware or a complete standalone TV operating system.

## Implemented

- Native Android launcher entry points for Android TV / Google TV and the Android Home intent.
- Installed-app discovery through Android launcher intents, with app launching delegated to Android.
- Home screen, app library, Bloom Search, curated store listings and Android settings shortcuts.
- Local Bloom profiles, accent themes, home layout preferences and a quick settings panel.
- Bloom Ambient screen and animated focus feedback, with persistent motion preferences.
- Settings, About and Software Update screens.
- Software Update checks the project's GitHub Releases feed, compares dotted version numbers, accepts only APK links from this repository's release-download path, downloads the APK and hands it to Android's package installer.
- Android installation approval remains user-controlled. Bloom does not silently install updates or replace device firmware.
- GitHub Actions builds and uploads a debug APK artifact; the feature-branch push workflow also publishes preview APK releases.

## Validation

- GitHub Actions run #73: **SUCCESS** for commit `750f7a5`.
- GitHub Actions run #75: **SUCCESS** for commit `ad43d8f`.
- GitHub Actions run #77: **SUCCESS** for current pull-request head `0c0d1dc`; the debug APK artifact was uploaded.
- The earlier run #71 failed because a multiline Kotlin string in the profile screen was not escaped. That compile error was corrected in commit `750f7a5`.
- A local build was not run because this working environment has no Gradle installation or Android SDK.
- No Android TV emulator or physical-device test has been run.

## Known limitations

- This is an installable Android launcher app, not a bootable TV OS image.
- App availability, streaming-service support, DRM and store installation are controlled by Android, device certification, publishers and Google Play.
- Preview APKs use debug signing. Hosted runner debug keys may differ between builds, so Android may require an older preview to be uninstalled before a newer one can be installed.
- D-pad focus, Back behaviour, package discovery and update installation still need hands-on testing on target TV hardware.

## Next steps

1. Test directional navigation, focus visibility and Back behaviour on an Android TV / Google TV device or emulator.
2. Test installed-app discovery and launching on more than one Android TV device.
3. Test the release-check, download and Android installation-approval flow.
4. Decide on a stable signing and release process before distributing builds beyond preview testing.
5. Treat any future standalone firmware work as a separate platform project with a chosen target device.
