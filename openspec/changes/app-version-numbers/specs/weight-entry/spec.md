## MODIFIED Requirements

### Requirement: Version visible
The app SHALL show its version on the main screen menu as `v<major>.<minor>.<build>`, where the
build number grows by one with every commit that changes the app and never resets, so a shared
log and a report can be matched to a build.

#### Scenario: Check the version
- **WHEN** the user opens the menu
- **THEN** the version is shown, e.g. "v1.0.24"

#### Scenario: Docs-only commit
- **WHEN** a commit changes only specs or documentation
- **THEN** the next build's number is the same as without that commit
