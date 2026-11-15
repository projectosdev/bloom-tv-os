# Bloom TV OS Lite

A lightweight, remote-first Android TV launcher concept with a liquid-glass-inspired interface and blossom-inspired boot screen.

## Included
- Native Android application using Kotlin and Jetpack Compose.
- Android TV / Google TV launcher entry points.
- Animated welcome screen and Bloom branding.
- Detection and launching of selected official streaming apps already installed on the device.
- Lightweight home screen designed for TV remote navigation.
- Settings screen with software update information and motion preferences.

## Get a test APK
A free GitHub Actions workflow builds a debug APK whenever the feature branch is updated or its pull request is checked.

1. Open the [latest successful Bloom debug build](https://github.com/projectosdev/bloom-tv-os/actions/workflows/android-debug.yml).
2. Open the newest run marked **Success**.
3. Scroll to **Artifacts** and download `bloom-tv-os-debug`.
4. Extract the ZIP file. It contains the debug APK.
5. Transfer the APK to your Android TV / Google TV device, open it, and allow installation from the file manager if Android asks.
6. Launch **Bloom TV OS Lite**. If you want to test it as your Home app, use your device's Home-app settings to select it.

GitHub keeps each build artifact for 7 days. These are unsigned debug builds for testing, not production releases. You may need to sign in to GitHub to download an artifact.

## Build locally
Open the project in Android Studio with JDK 17 and Android SDK 35 installed. Sync Gradle and build the app module, or run:

```bash
gradle --no-daemon :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.

## Platform reality
This repository is a launcher foundation, not replacement firmware or a complete operating system image. A true standalone TV OS requires a supported platform such as AOSP or Linux, device-specific drivers, boot integration and hardware testing.

Streaming services control their own apps, DRM and distribution. Bloom does not bundle, reproduce or guarantee Netflix or other services. Availability depends on the device, certification, region and official app support.

## Testing status
The debug APK has successfully compiled and packaged in GitHub Actions. It has not yet been installed or tested on a physical Android TV / Google TV device. Device testing, focus/accessibility refinement and further setup work are still needed before daily use.
