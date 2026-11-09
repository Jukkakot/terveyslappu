# Proposal

## Why

The version `0.1.<commit count>-<sha>` is hard to read and jumps with every docs commit. The user
wants a plain, growing version like `1.0.24` (user 2026-11-09).

## What Changes

- Version name `MAJOR.MINOR.BUILD`, starting at 1.0. BUILD is the build number: the number of
  commits that produce a build (everything except specs, docs and Claude files, the same paths CI
  ignores), so docs-only commits do not raise it. It never resets.
- MAJOR.MINOR live in one place in the build files. MINOR goes up when a change adds or changes
  something the user notices (bumped in that change's last task); MAJOR when an update breaks
  something (reinstall, data format). Fixes only raise BUILD.
- The internal Android version code stays the total commit count, so it keeps growing and every
  build installs over the previous one (the new build number is smaller than the old commit count).
- The menu shows `v1.0.24`; the log's start line and the release title keep the commit next to it.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-entry`: "Version visible" shows `v<major>.<minor>.<build>` instead of build number and commit.
- `app-log`: the start line carries the version and the commit.

## Impact

Build/CI only (version computation, release title) plus the menu text and the log's start line.
No app screen layout changes.
