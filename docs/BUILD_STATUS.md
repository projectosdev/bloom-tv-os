# Bloom TV OS Lite build status

Last verified: 2026-11-15

- GitHub Actions run #77 successfully executed `:app:assembleDebug` for pull request #1 head `0c0d1dc39486da6be84dbd03f65ff05c362250c8`.
- The workflow uploaded the `bloom-tv-os-debug` APK artifact.
- The earlier Kotlin compilation error in the profile screen was fixed and the subsequent CI build passed.
- A local build was not run because this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

This is a native Android launcher foundation, not replacement firmware. Official streaming apps must be installed and supported by the target device. Preview APKs are debug-signed and are not production releases.
