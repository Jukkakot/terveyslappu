## ADDED Requirements

### Requirement: Grouped day lists
The day lists of Energia, Askeleet and Kulutus SHALL be grouped by the same rule as the weight list
(weight-entry "Grouped list" and "Open by default"): year, month and week (Monday to Sunday, cut at
month borders), newest first on every level, the days as plain rows under their week, every year,
month and week holding a listed day its own row, the year level left out when all listed days fall
in one year, and a flat list without groups when the period lists at most 14 days. Every group SHALL
be closed when the tab is shown and after a period change; open groups SHALL NOT be remembered after
leaving Historia. There SHALL be no gap rows. Day rows SHALL look and act as before (an Energia day
opens Ruoka; an Askeleet or Kulutus day opens nothing).

A group row SHALL show the group's name as a weight group does ("2024", "Marraskuu 2025", "Vk 45 ·
3.–9.11."), the per-day average of the tab's main value over the group's listed days, the number of
those days ("12 pv"), and the open sign; tapping it SHALL open or close it:
- Energia: "ka 1 850 kcal/pv" and "herkkuja ka 230" (left out when zero).
- Askeleet: "ka 8 400 askelta/pv".
- Kulutus: "ka 2 300 kcal/pv" and "aktiivinen ka 450" (left out when no day of the group has an
  active value).
Today SHALL count in a group's average only when the tab's statistics count it.

#### Scenario: A week stays flat
- **GIVEN** the period 1 vk with seven logged days on Energia
- **THEN** the list shows seven day rows and no group rows

#### Scenario: Months of steps
- **GIVEN** the period 1 v with steps on 200 days, all in 2026
- **WHEN** Askeleet is shown
- **THEN** the top level shows closed month rows, newest first, each with its average steps per day
  and its number of days, and no year row

#### Scenario: Open a week on Energia
- **GIVEN** the period 3 kk with 60 logged days
- **WHEN** the user opens Marraskuu and then "Vk 46 · 9.–15.11."
- **THEN** the week's days show under it, and tapping 12.11. opens Ruoka on that day

#### Scenario: Period change closes groups
- **GIVEN** a month row is open on Kulutus
- **WHEN** the user picks another period
- **THEN** every group is closed
