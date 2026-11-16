## MODIFIED Requirements

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500). With Keskiarvo on, its line is
the average total per day with data, today left out. An own chart SHALL hold one to four data; in
Päällekkäin up to four bar data stand side by side in a day's slot and only the first two drawn
data have a scale. The crosshair readout SHALL show Kulutus as "2 300 kcal · aktiivinen 450 kcal" (a
day without a split: "2 300 kcal"), "ei tietoa" without a value. Its Allekkain panel without values
SHALL read "Ei kulutustietoja tällä jaksolla". A new chart SHALL start with Kulutus off.

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, each with its kcal scale

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
- Energia: "ka 1 850 kcal/pv" and "herkkuja ka 230 kcal/pv" (left out when zero).
- Askeleet: "ka 8 400 askelta/pv".
- Kulutus: "ka 2 300 kcal/pv" and "aktiivinen ka 450 kcal/pv" (left out when no day of the group has an
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

## ADDED Requirements

### Requirement: Units in Historia lists
Every number in Historia's lists SHALL carry its unit. A weight row's and a weight group row's
change from the previous weight SHALL read with `kg` ("−0,3 kg", "±0,0 kg"), next to its direction
arrow as now. BMI values stay without a unit; their "BMI" label names them.

#### Scenario: Change on a weight row
- **GIVEN** a weight of 82,1 kg follows one of 82,4 kg
- **THEN** its row in the Historia list shows "−0,3 kg" with an arrow down
