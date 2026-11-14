# Bloom TV OS Lite

A lightweight, remote-first Android TV launcher concept with a liquid-glass-inspired interface and blossom-inspired boot screen.

## Included
- Native Android application using Kotlin and Jetpack Compose.
- Android TV / Google TV launcher entry points.
- Animated welcome screen and Bloom branding.
- Detection and launching of selected official streaming apps already installed on the device.
- Lightweight home screen designed for TV remote navigation.

## Platform reality
This repository is a launcher foundation, not replacement firmware or a complete operating system image. A true standalone TV OS requires a supported platform such as AOSP or Linux, device-specific drivers, boot integration and hardware testing.

Streaming services control their own apps, DRM and distribution. Bloom does not bundle, reproduce or guarantee Netflix or other services. Availability depends on the device, certification, region and official app support.

## Build
Open the project in Android Studio with JDK 17 and Android SDK 35 installed. Sync Gradle and build the app module. Test on an Android TV / Google TV device that allows third-party launcher installation.

This is an early preview and needs device testing, focus/accessibility refinement, and further setup and settings screens before daily use.
