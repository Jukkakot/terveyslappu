## MODIFIED Requirements

### Requirement: Version visible
The app SHALL show its version in Asetukset as `v<major>.<minor>.<build>`, where the build number
grows by one with every commit that changes the app and starts again from 0 at the commit that
changes `<major>.<minor>`, so a shared log and a report can be matched to a build.

#### Scenario: Check the version
- **WHEN** the user opens Asetukset
- **THEN** the version is shown, e.g. "v1.0.24"

#### Scenario: Docs-only commit
- **WHEN** a commit changes only specs or documentation
- **THEN** the next build's number is the same as without that commit

#### Scenario: New minor version
- **GIVEN** the latest build is 1.23.60
- **WHEN** a commit raises the version to 1.24
- **THEN** that build is 1.24.0 and the next app commit is 1.24.1
