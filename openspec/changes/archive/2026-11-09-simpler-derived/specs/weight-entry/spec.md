## MODIFIED Requirements

### Requirement: Grouped list
Historia's list SHALL group the period's weights by year, month and week, newest first on every
level, with the weights as plain rows under their week (no day level: a weight row already shows its
day). Weeks run Monday to Sunday and SHALL be cut at month borders. Every year, month and week that
holds a weight SHALL be its own row, even with a single weight (user 2026-11-09: one systematic
rule). When all the period's weights fall in one year, the year row SHALL be left out and the list
SHALL start with the months. A period with at most 14 weights SHALL stay a flat list of weight rows
without groups.

#### Scenario: A week, one weight a day
- **GIVEN** the period 1 vk holds one weight on each of seven days
- **WHEN** Historia is shown
- **THEN** the list shows seven weight rows and no group rows

#### Scenario: Two weights in a day
- **GIVEN** a period of over 14 weights where a day holds weights at 7.05 and 21.30
- **WHEN** that day's week is open
- **THEN** both weights show as weight rows under the week, with no day row

#### Scenario: Several years
- **GIVEN** the period Kaikki holds weights from 2022 to 2026
- **WHEN** Historia is shown
- **THEN** the top level shows year rows 2026, 2025, 2024, 2023, 2022

#### Scenario: Week across a month border
- **GIVEN** weights on Thu 30.10. and Mon 3.11. and Tue 4.11.
- **THEN** 30.10. sits under Lokakuu and 3.–4.11. under Marraskuu, each in its own part of the week

#### Scenario: Month with one weight
- **GIVEN** a period of over 14 weights where March 2024 holds a single weight
- **THEN** March 2024 is a month row with a week row under it, and the weight under that week

#### Scenario: One year
- **GIVEN** the period 1 v holds 200 weights all from 2026
- **THEN** the top level shows month rows and no year row

### Requirement: Group rows
A group row SHALL show the group's name (a year "2024", a month with year "Marraskuu 2025", a week
with its days "Vk 45 · 3.–9.11."; the year shown when not the current year), the first and the last
weight of the group in time ("92,3 → 91,0 kg"), their difference with the list's arrow and neutral
colours, the number of weights, and a sign that it opens. A group with a single weight SHALL show
that weight and no difference. Tapping it SHALL open or close it.

#### Scenario: Month row
- **GIVEN** November 2025 holds 20 weights, the first 92,3 kg and the last 91,0 kg
- **THEN** its row reads "Marraskuu 2025", "92,3 → 91,0 kg", −1,3 with an arrow down and 20

#### Scenario: Open and close
- **WHEN** the user taps a closed month row
- **THEN** its weeks appear under it; tapping again hides them

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
