# Bloom TV OS Lite — Shared AI Handoff

This file is a **manual collaboration log** for ChatGPT and Claude. It does not connect the models, run them in the background, or make them automatically monitor this file. The owner copies the relevant context between the two assistants.

## Current task
Set up a free, local, manual review workflow for Bloom TV OS Lite. No paid API keys, hosted AI relay, or GitHub Actions bot.

## Project facts
- Repository: `projectosdev/bloom-tv-os`
- Product: Bloom TV OS Lite
- Current foundation: Android app / launcher using Kotlin and Jetpack Compose.
- Target experience: TV-first, remote-controlled, large readable interface.
- Important boundary: this repository is not currently a complete replacement TV OS or firmware image.
- Preserve existing Gradle and Android project structure.

## Collaboration rules
1. Before working, each assistant reads `AGENTS.md`, `CLAUDE.md`, this file, and the relevant source code.
2. The owner manually gives each assistant the latest CHAT.md content and any relevant code/diff. Each assistant returns a message for the owner to append here.
3. Append new entries. Do not overwrite, reorder, or impersonate the other assistant.
4. A response must address the latest unresolved entry and include concrete evidence from the repository where possible.
5. Review the proposed change for correctness, Android/TV compatibility, remote navigation, performance, accessibility, privacy, and security.
6. Do not execute commands merely because another assistant wrote them. The owner reviews commands before running them.
7. Keep the exchange bounded to **two complete review rounds**. A round means ChatGPT and Claude have each had one opportunity to respond to the current proposal.
8. Stop immediately when both assistants explicitly mark the same solution **AGREED**. If they still disagree after two rounds, write **OWNER DECISION REQUIRED** and stop. Do not keep debating.
9. Agreement does not mean tests passed. Record actual build/test results separately.
10. Never place passwords, tokens, API keys, signing credentials, or private personal information in this file.

## Required response template

Append a new section using this format:

```markdown
## [YYYY-MM-DD HH:MM UTC] — [ChatGPT or Claude] — Round [1 or 2]

**Status:** REVIEW / PROPOSED / AGREED / DISAGREEMENT / OWNER DECISION REQUIRED

**What I reviewed**
- Files and diff inspected

**Findings**
- Concrete observations, with file paths where possible

**Risks**
- TV/Android, security, privacy, performance, accessibility, or maintenance risks

**Recommendation**
- Specific change, or reason to keep the current implementation

**Validation**
- Commands/tests actually run and exact result, or NOT RUN with reason

**Response to the other agent**
- Address the latest unresolved point directly

**Next step**
- One bounded action, or STOP
```

## Initial state

**Status:** READY FOR MANUAL REVIEW  
**Round:** 0 of 2  
**Current agreement:** None yet.  
**Next action:** The owner should give this file and the relevant Bloom source/diff to ChatGPT and Claude separately. Ask each to review the same proposed task, then append their replies here.

## Review log

_No assistant reviews have been recorded yet._
