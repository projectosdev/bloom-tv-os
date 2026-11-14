# Bloom TV OS Lite — Progress Log

Last updated: 2026-11-14

## Current status

**Stage:** Repository review and project planning  
**Build status:** Not yet verified on a device  
**Important:** This is an honest progress log. Planned features are not marked as completed.

## What has actually been done

- Located and inspected the GitHub repository: `projectosdev/bloom-tv-os`.
- Read the existing `README.md`.
- Inspected the root Gradle configuration in `build.gradle.kts` and `settings.gradle.kts`.
- Inspected the Android app module configuration in `app/build.gradle.kts`.
- Confirmed the project is an Android application using Kotlin and Jetpack Compose.
- Confirmed the README describes an early Android TV / Google TV launcher foundation, not a complete replacement TV operating system.
- Confirmed the existing Gradle files are relevant to the Android TV launcher and should not be deleted just because the project is TV-focused.

## Existing project foundation

- Android application module: `:app`
- Kotlin and Jetpack Compose
- Android TV / Google TV launcher entry points are described in the README
- App configuration currently uses namespace and application ID `dev.projectos.bloom`
- The project is configured with compile SDK 35 and Java 17

## Decisions

- Product name: **Bloom TV OS Lite**
- Product type: TV-first operating system experience, designed for remote control.
- Preserve the existing Android project as the starting point.
- Do not delete or replace repository files without first checking their purpose.
- Treat a true standalone TV OS / firmware build as a separate, much larger engineering step. The current Android launcher is not itself replacement firmware.
- Bloom AI is a planned feature. A secure backend and separate API billing would be needed for OpenAI API integration.

## Planned work — not completed yet

1. Inspect the full app source code and Android manifest.
2. Build and run the current app to establish a working baseline.
3. Review TV launcher registration and remote-control focus navigation.
4. Implement the Bloom TV home screen and visual theme.
5. Add the blossom-inspired startup experience where the platform permits it.
6. Add core TV settings and app discovery / launching.
7. Design Bloom AI integration with secure credential handling.
8. Test on Android TV hardware or a suitable emulator.
9. Assess a separate Raspberry Pi / Linux TV image only after the target hardware and platform are chosen.

## Notes and test results

- No source-code changes or app features have been implemented by this progress-log update.
- No successful build or hardware test has been confirmed yet.
- The repository is being preserved; no files have been deleted.

## Update format

For each future update, record:
- Date
- Files changed
- Features implemented
- Build/test result
- Known issues
- Next steps

## 2026-11-14 — Free local AI hand-off setup

### Files added
- `AGENTS.md` — shared repository rules for AI coding agents, with Bloom TV scope, safe code-review requirements, TV-specific engineering priorities, and bounded collaboration rules.
- `CLAUDE.md` — Claude-specific entry point that defers to AGENTS.md and explains the manual hand-off process.
- `CHAT.md` — starter append-only collaboration log for manually exchanging reviews between ChatGPT and Claude.

### Workflow design
- No GitHub Actions workflow, hosted bot, API key, or paid API usage is required for this manual approach.
- The owner manually shares the same CHAT.md content and relevant source/diff with each assistant, then appends each response.
- Collaboration stops when both agents mark the same proposal AGREED, or after two complete review rounds. Unresolved disagreements become OWNER DECISION REQUIRED.
- The files explicitly state that CHAT.md does not create a live connection or automatic background conversation between the models.

### Validation
- All three files were successfully created in the repository's default branch.
- No application source code was changed.
- No Android build or device test was run as part of this documentation setup.
