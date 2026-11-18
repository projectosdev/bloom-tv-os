# Bloom TV OS Lite

A lightweight, remote-first Android TV launcher concept with a liquid-glass-inspired interface and blossom-inspired boot screen.

## Included
- Native Android application using Kotlin and Jetpack Compose.
- Android TV / Google TV launcher entry points with a dedicated TV banner and Bloom launcher icon.
- Animated welcome screen and Bloom branding.
- Discovery and launching of installed Android TV and standard Android launcher apps, with launch requests handled by Android.
- Lightweight home screen designed for TV remote navigation.
- Settings screen with software update information and motion preferences.
- Store entry point for the official F-Droid Android app repository. Bloom opens the installed F-Droid client, or directs users to F-Droid's official site to install it. F-Droid handles repository verification, app downloads, installation and updates.
- Software Update checks the official GitHub Releases feed, downloads a published APK, and hands it to Android's package installer for explicit user approval. It does not silently install updates or replace device firmware.

## Get a test APK
A free GitHub Actions workflow builds a debug APK whenever the feature branch is updated or its pull request is checked.

1. Open the [latest successful Bloom debug build](https://github.com/projectosdev/bloom-tv-os/actions/workflows/android-debug.yml).
2. Open the newest run marked **Success**.
3. Scroll to **Artifacts** and download `bloom-tv-os-debug`.
4. Extract the ZIP file. It contains the debug APK.
5. Transfer the APK to your Android TV / Google TV device, open it, and allow installation from the file manager if Android asks.
6. Launch **Bloom TV OS Lite**. If you want to test it as your Home app, use your device's Home-app settings to select it.

GitHub keeps each build artifact for 7 days. The workflow also publishes preview APKs as GitHub Releases so Bloom's Software Update screen can find them. These are debug-signed test builds, not production releases. Android may ask you to allow Bloom to install unknown apps before an update can be handed to the package installer. Hosted runners may use different debug signing keys between runs, so a newer APK may not install over an older one. If Android rejects an update because the signature differs, uninstall the previous Bloom test build before installing the new APK; this clears Bloom's local profile and settings. You may need to sign in to GitHub to download an artifact.

## Build locally
Open the project in Android Studio with JDK 17 and Android SDK 35 installed. Sync Gradle and build the app module, or run:

```bash
gradle --no-daemon :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.

## App store
Bloom does not invent app listings or route users to Google Play on devices that do not have it. The Store opens the official F-Droid client (`org.fdroid.fdroid`) when installed; otherwise it opens [F-Droid's official website](https://f-droid.org/) so the user can install the client. F-Droid is responsible for signed repository metadata, app downloads, installation prompts and app updates. App availability depends on Android version, CPU architecture and device compatibility.

This integration is for Android-based Raspberry Pi images. It does not make this Android launcher run on Raspberry Pi OS/Linux; that would require a separate Linux application and packaging approach.

## Platform reality
This repository is a launcher foundation, not replacement firmware or a complete operating system image. A true standalone TV OS requires a supported platform such as AOSP or Linux, device-specific drivers, boot integration and hardware testing.

Streaming services control their own apps, DRM and distribution. Bloom does not bundle, reproduce or guarantee Netflix or other services. Availability depends on the device, certification, region and official app support.

## Testing status
The GitHub Actions workflow compiles and packages a debug APK when its build job succeeds; check the latest run before expecting an artifact. No physical Android TV / Google TV device or emulator test has been recorded. Remote focus, accessibility, device compatibility and real-world app launching still need hands-on testing before daily use.
