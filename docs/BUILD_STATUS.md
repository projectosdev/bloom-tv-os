# Bloom TV OS Lite build status

Last verified: 2026-11-20

- GitHub Actions run #94 succeeded on commit `de1cee334988181a23ba3faa402d4b3b278170ea`: `:app:testDebugUnitTest`, `:app:assembleDebug`, and APK artifact upload all passed.
- The `bloom-tv-os-debug` artifact was uploaded (16.1 MB) and expires 2026-11-26. Run #93 also passed the test/build/upload steps. Run #95 failed Kotlin compilation on a list-versus-map return; the correction is committed and run #97 is pending.
- The feature-branch workflow also published a preview debug APK release. This is not an OTA or firmware update.
- Local pure-Kotlin checks passed 17 assertions covering version comparison, trusted release URLs, app sorting, and update-state resolution.
- A local Android build was **NOT RUN**: this environment has no Gradle installation or Android SDK.
- No Android TV / Google TV emulator or physical-device test has been recorded.

The updater is an APK-download and Android package-installer hand-off in code. Its installation flow has not been verified on hardware. Bloom remains a launcher app, not replacement firmware.

PR #1 remains open. Its branch is 23 commits behind current `main` and GitHub reports merge conflicts; no merge/rebase was performed during this work.
