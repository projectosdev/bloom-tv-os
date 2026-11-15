# Bloom TV OS Lite

Bloom is a lightweight, remote-first Android TV launcher concept with a calm, cinematic interface and a blossom-inspired identity.

## Current preview

- Native Android application built with Kotlin and Jetpack Compose.
- Android TV / Google TV launcher entry points.
- Landscape-first, television-sized home screen.
- Cinematic featured panel and focused app shelves.
- Home, Apps and Settings navigation.
- Focus-aware app tiles with subtle scaling and selection treatments.
- Detection and launching of selected official streaming apps already installed on the device.
- A simple boot presentation and an About screen.

## Get a test APK

Every push to the main branch starts a GitHub Actions Android build. When a run succeeds:

1. Open the [Android build workflow](https://github.com/projectosdev/bloom-tv-os/actions/workflows/android-build.yml).
2. Open the latest run marked **Success**.
3. Download the `bloom-tv-os-lite-debug` artifact from the run's Artifacts section.
4. Extract the ZIP and install `app-debug.apk` on a compatible Android device.

Artifacts are temporary CI outputs, not permanent releases. The workflow retains them for 14 days.

## Platform reality

This repository is an Android launcher foundation, not replacement firmware or a complete operating system image. A true standalone TV OS requires a supported platform such as AOSP or Linux, device-specific drivers, boot integration and hardware testing.

The Quest headset is being used as a convenient test device. It is not the target platform. The primary target remains Android-based TV-style devices, with responsive behaviour where practical.

Streaming services control their own apps, DRM and distribution. Bloom does not bundle, reproduce or guarantee Netflix or other services. Availability depends on the device, certification, region and official app support.

## Build

The GitHub Actions workflow uses JDK 17, Android SDK 35 and Gradle 8.9. To build locally, open the project in Android Studio with JDK 17 and Android SDK 35 installed, sync Gradle, and build the `app` module. The workflow invokes the installed Gradle distribution directly; a checked-in Gradle wrapper has not yet been added.

The current Android application ID is `dev.projectos.bloom`. Keep this ID stable for upgrades. Android also requires updates to be signed with the same signing certificate as the installed version. CI debug builds use Android's debug signing configuration; an APK signed with a different key cannot update an existing installation. If Android reports a signature conflict, uninstall the old test build once before installing the new one. Do not uninstall if you need to preserve app data.

## Current limitations

- This is an early preview and has not yet been verified on a range of physical Android TV devices.
- The Settings page currently links to Android system settings and shows project information.
- Full-device reboot and privileged system update installation are not implemented. A regular third-party launcher cannot silently reboot or replace the device OS.
- CI build status must be checked in Actions; a committed workflow is not proof of a successful APK build.

## Development direction

The visual direction is an original premium television interface: spacious layouts, restrained glass-like surfaces, readable typography, cinematic colour, clear focus states and controller-friendly navigation. It takes broad inspiration from polished TV interfaces without copying proprietary branding or assets.


## Bloom Command Centre (separate owner application)

The separate `:owner` Android module is the **Bloom Command Centre**, not a second operating system. It has its own launcher application ID (`dev.projectos.bloom.owner`) and remains separate from the standard Bloom TV OS Lite application (`dev.projectos.bloom`). The standard launcher is not modified by owner-console preferences or screens.

The owner console is intended for platform operations: announcements, device fleet management, application catalogue, release management, security, automation, diagnostics, audit history and configuration. The current preview includes a local announcement-draft studio. Drafts are stored on the device running the Command Centre and are **not broadcast** to other installations. Fleet-wide announcements, user administration, remote device commands, authentication, live analytics and staged deployment require a real authenticated backend and device agents; they are not represented as working until that infrastructure exists.

The searchable capability catalogue is a roadmap/index, not a claim that every listed action is implemented. Controls must be labelled by state (working locally, backend required, planned, or unavailable) and must not report success for operations that have not actually run.

The Android build workflow builds both app modules. Download the `bloom-tv-os-lite-debug` artifact and inspect its APK outputs for both editions.
