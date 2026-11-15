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

## Platform reality

This repository is an Android launcher foundation, not replacement firmware or a complete operating system image. A true standalone TV OS requires a supported platform such as AOSP or Linux, device-specific drivers, boot integration and hardware testing.

The Quest headset is being used as a convenient test device. It is not the target platform. The primary target remains Android-based TV-style devices, with responsive behaviour where practical.

Streaming services control their own apps, DRM and distribution. Bloom does not bundle, reproduce or guarantee Netflix or other services. Availability depends on the device, certification, region and official app support.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35 installed. Sync Gradle and build the app module.

The current Android application ID is dev.projectos.bloom. Keep this ID stable for upgrades. Android also requires updates to be signed with the same signing certificate as the installed version. Changing either the application ID or signing identity can cause an install conflict.

## Current limitations

- This is an early preview and has not yet been verified on a range of physical Android TV devices.
- The Settings page currently links to Android system settings and shows project information.
- Full-device reboot and privileged system update installation are not implemented. A regular third-party launcher cannot silently reboot or replace the device OS.
- A Gradle wrapper and automated build pipeline still need to be added before CI can produce a dependable APK artifact.

## Development direction

The visual direction is an original premium television interface: spacious layouts, restrained glass-like surfaces, readable typography, cinematic colour, clear focus states and controller-friendly navigation. It takes broad inspiration from polished TV interfaces without copying proprietary branding or assets.
