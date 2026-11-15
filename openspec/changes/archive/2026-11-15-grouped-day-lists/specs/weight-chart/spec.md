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
Details:
- Listed days are the days each list already shows (Energia: days with kcal; Askeleet: days with
  steps; Kulutus: days with burned energy); the 14 counts those.
- Today counts in "N pv" but not in the averages, as in the tab's statistics; a group whose only
  day is today shows today's value (the week bars' fallback).
- Secondary averages use the stats' denominators (treats over all listed days, active over the days
  with an active value), rounded as the stats (whole numbers, grouped thousands); a part that
  rounds to 0 or is missing is left out.
- Group row layout reuses the weight group row: name left, "ka … /pv" right on the first line; the
  secondary part and "N pv" on the second; the open sign; rows indented by depth. A month shows its
  year only when not the current year.
- Each list keeps its own open groups; every list closes all groups on a period change and on a
  tab switch (this also applies to the weight list).
- List weeks are cut at month borders and may differ from the weekly chart bars.
- The crosshair opens no group; empty and permission states stay as they are.

#### Scenario: Numbers in a week group
- **GIVEN** today is Wednesday and Askeleet lists Monday 8 000, Tuesday 10 000 and today 3 000 in a
  period of over 14 listed days
- **THEN** the week's row reads "ka 9 000 askelta/pv" and "3 pv"

#### Scenario: Only today in the week
- **GIVEN** today is Monday with 2 000 kcal logged and the period lists over 14 days
- **THEN** this week's Energia row reads "ka 2 000 kcal/pv" and "1 pv"

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

## MODIFIED Requirements

### Requirement: Energia list
On the Energia tab the list under the statistics SHALL show the period's logged days, newest first
and grouped as in "Grouped day lists", each with its date, total kcal and treat share; tapping a day
SHALL open the Ruoka screen on that day.

#### Scenario: Open a day
- **WHEN** the user taps 12.11. in the Energia list
- **THEN** the Ruoka screen opens on 12.11.; back returns to Historia on Energia

### Requirement: Askeleet list
On the Askeleet tab the list under the statistics SHALL show the period's days with steps, newest
first and grouped as in "Grouped day lists", each with its date and steps. A row SHALL NOT open
anything.

#### Scenario: Newest first
- **GIVEN** steps for 12.11., 13.11. and 14.11.
- **THEN** the list reads 14.11., 13.11., 12.11. with their steps
