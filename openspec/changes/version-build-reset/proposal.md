# Proposal: version-build-reset

## Why

The build number never resets, so 1.23.60 would be followed by 1.24.61. The user expects the
usual reading (2026-11-14): the last number starts again from 0 whenever MAJOR.MINOR changes, so
the first food-log release is 1.24.0.

## What Changes

- BUILD = the commits that change the app (outside the CI-ignored paths) since the commit that
  last changed `appVersion` in `gradle.properties`; that commit itself is `.0`.
- The version code stays the count of all commits, so every build still installs over the
  previous one, and Obtainium still sees a newer version (1.23.60 → 1.24.0 is an upgrade).
- Ships in the same push as food-log, so no 1.24.61 is ever published.

## Capabilities

### Modified Capabilities
- `weight-entry`: "Version visible": the build number resets when MAJOR.MINOR changes.

## Impact

`app/build.gradle.kts` (build number), `.claude/CLAUDE.md` release notes. CI already fetches the
whole history (`fetch-depth: 0`).
