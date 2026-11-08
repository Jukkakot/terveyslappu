# Proposal

## Why

NFR-7 says a failing weigh-in the user shares becomes a replay test. Today that means writing a
Kotlin test by hand for each log. A fixture folder makes it one file per case.

## What Changes

- Test resources folder `scale-replays/`: each `.replay` file (not `.log`: `.gitignore` blocks real logs) holds `scale.packet` lines (weights
  made up, NFR-1) and a first line `# expect kg=<weight>` or `# expect none`.
- One JVM test runs every file through the weight decision and checks the expectation.

## Capabilities

### New Capabilities
- (none) Test tooling only; no app behaviour changes (`skip_specs`).

### Modified Capabilities
- (none)

## Impact

- Tests only (`app/src/test`). `.claude/CLAUDE.md` describes how to add a case.
