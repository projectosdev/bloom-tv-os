# Bloom TV OS Lite — Development Progress

Last updated: 2026-11-15

## Current status

**Stage:** Android TV launcher preview, visual redesign committed to `main`.

## Completed in this session
- Reworked the main Compose screen with a cinematic featured panel, Home / Apps / Settings navigation, restrained dark surfaces and focus-aware app tiles.
- Added an About panel and clarified that Quest is a test device, not the target platform.
- Kept Android application ID `dev.projectos.bloom` stable.
- Bumped app version metadata to 0.2.0 and added CI-overridable version properties.
- Added a GitHub Actions workflow to build a debug APK and upload it as a 14-day artifact.
- Corrected Android SDK setup after the first workflow run failed because the setup action attempted to install the obsolete `tools` package.
- Simplified the workflow to use the provisioned Gradle 8.9 installation directly rather than generating a wrapper in CI.

## Validation
- The first workflow attempt failed during Android SDK setup; the error was `Failed to find package 'tools'`.
- A corrected workflow run was queued after explicitly requesting Android SDK platform 35 and build-tools 35.0.0.
- The latest run must complete before a successful APK build can be claimed.
- No physical-device or Quest installation test has been performed in this session.

## Remaining work
1. Confirm the latest GitHub Actions run builds and uploads `app-debug.apk`.
2. Add a Gradle wrapper if local command-line builds are required.
3. Establish a consistent signing key for repeatable APK upgrades; do not expose a production signing key in the repository.
4. Integrate the fuller settings, app discovery, profile and software-update work without losing the new visual direction.
5. Test D-pad navigation, Back behaviour and installation on actual target hardware.
6. Implement device reboot only where supported by platform privileges; a normal launcher cannot reboot the whole device.


## 2026-11-24 — Bloom feature hub increment

- Added a Features destination to the TV launcher navigation.
- Added Bloom Features hub with local installed-app search, persisted recent-app shortcuts, and status cards for the planned Bloom services.
- Added Store and Privacy Centre shortcuts to supported Android intents.
- Clearly labelled cloud, AI, security-scanning, certificate and companion features that are not connected or implemented yet.
- Android build run 45431414615 was pending at the time of this update; do not treat the change as build-verified until CI completes.
- No physical TV hardware test has been performed.
