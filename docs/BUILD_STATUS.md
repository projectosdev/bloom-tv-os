# Bloom TV OS Lite build status

Last verified: 2026-11-20

- GitHub Actions run #98 succeeded on commit `28be41f0a2709eac1e1a7c63138505143e757534`: `:app:testDebugUnitTest`, `:app:assembleDebug`, and APK artifact upload all passed.
- GitHub Actions run #94 also passed tests, build and artifact upload on commit `de1cee33`.
- The `bloom-tv-os-debug` artifact from run #98 was uploaded (16.1 MB) and expires 2026-11-27. Run #95–#97 exposed compile errors that were corrected before run #98.
- Preview debug releases are intended for app-source pushes. Run #98's release job was skipped by an unreliable changed-file condition; the workflow is being corrected. These are not OTA or firmware updates.
- Local pure-Kotlin checks passed 17 assertions covering version comparison, trusted release URLs, app sorting, and update-state resolution.
- A local Android build was **NOT RUN**: this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater is an APK-download and Android package-installer hand-off in code. Its installation flow has not been verified on hardware. Bloom remains a launcher app, not replacement firmware.

PR #1 remains open. Its branch is 23 commits behind current `main` and GitHub reports merge conflicts; no merge/rebase was performed during this work.
