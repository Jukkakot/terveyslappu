# app-log Specification

## Purpose
Gives the developer a way to see what the app did on the phone: the app keeps a log file and the
user can share it from the app, since there is no cable or debugger in this project's workflow.

## Requirements

### Requirement: Log file
The app SHALL write a log file on the phone with timestamped lines for app start (with version and
the commit it was built from), Health Connect availability and permission results, saves, reads,
deletes and every error with its cause. The file SHALL keep at most the latest 1 MB of lines.

#### Scenario: Start is logged
- **WHEN** the app starts
- **THEN** the log gets a start line with the version (e.g. 1.0.24) and the commit

#### Scenario: Save is logged
- **WHEN** a weight is saved
- **THEN** the log gets a line with the time, the event and the outcome

#### Scenario: Crash is logged
- **WHEN** the app crashes
- **THEN** the crash and its stack trace are in the log after the next start

#### Scenario: Size limit
- **WHEN** the log grows past 1 MB
- **THEN** the oldest lines are dropped and the newest kept

### Requirement: Share the log
The app SHALL offer "Jaa loki" in the main screen menu, which opens the Android share sheet with
the log file attached.

#### Scenario: Share with Quick Share
- **WHEN** the user taps Jaa loki and picks Quick Share
- **THEN** the log file is sent as a text file

### Requirement: Log stays on the phone
The app SHALL NOT send the log anywhere by itself; it leaves the phone only when the user shares it.

#### Scenario: No automatic upload
- **WHEN** the app runs
- **THEN** it makes no network request with log content
