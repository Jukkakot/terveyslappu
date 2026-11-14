## ADDED Requirements

### Requirement: Backup does not silently shrink
When the history permission is not granted, the backup status SHALL say the copy holds recent
weights only (like the export does). An automatic backup that would write fewer than half of the
last successful count SHALL be refused: the file is left as it was, the status is failed with the
reason, and `backup.run refused reason=shrink` is logged. "Varmuuskopioi nyt" is not guarded.

#### Scenario: Permission missing
- **GIVEN** the last backup held 100 weights and the history permission is revoked
- **WHEN** an automatic backup would write 20
- **THEN** it is refused and the file is unchanged
