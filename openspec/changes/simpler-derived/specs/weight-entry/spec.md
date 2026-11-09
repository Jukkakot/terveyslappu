## MODIFIED Requirements

### Requirement: Open by default
When Historia is shown, every group SHALL be closed, so the list starts as an overview of its top
level; tapping a group opens it. Open and closed groups SHALL NOT be remembered after leaving
Historia; changing the period SHALL reset them to closed. A list short enough to stay flat (no
groups) shows its weights as before.

#### Scenario: Kaikki opened
- **GIVEN** the period Kaikki with weights from 2022 to today
- **WHEN** Historia is shown
- **THEN** the list shows only the closed year rows (and gap rows between them); no weight row shows
  until a group is opened
