# Proposal

## Why

The app knows the weight and, since owner-profile, the owner's height, birth year and gender, but
shows only kilos. BMI, the normal-weight range for one's height and the basal metabolic rate are
the standard values those give; the user wants them on the home screen and as their own history
(user 2026-11-09). Background with sources: `knowledge.md` → Derived body values.

## What Changes

- New pure calculations: BMI (kg / m²), its category in the WHO adult classes with a traffic-light
  tone (green normal, yellow borderline, red bad), the normal-weight range for the height (BMI
  18.5–25 in kg), and the basal metabolic rate by Mifflin-St Jeor (kcal/day).
- Home screen: under the latest weight card a short row of the latest BMI with its category, the
  normal-weight range and the basal metabolic rate. A value whose owner details are missing is left
  out and replaced by a short reason naming the missing details ("Perusaineenvaihdunta vaatii
  sukupuolen"), which opens Asetukset → Omat tiedot.
- Historia: the chart and its period statistics swipe left/right between one quantity at a time:
  Paino, BMI, Perusaineenvaihdunta (tabs above the chart, also tappable; all three always shown,
  a quantity that cannot be computed shows its reason and a button to Omat tiedot). The BMI chart
  shows the category bands as faint coloured backgrounds. The weight list stays as it is.
- Fix (user, screenshot 2026-11-09): the chart's line starts at a gap from the y-axis; every chart
  starts right at the axis.
- Derived values are computed for every weight, old ones too, with the current height; the age for
  the basal metabolic rate is the age in the weight's year.
- Nothing derived is written to Health Connect or the export/backup (calculated, not measured).
- Not in this change: body fat from impedance, waist-to-height ratio (user 2026-11-09: not wanted).

## Capabilities

### New Capabilities
- `derived-values`: BMI, BMI category with tone, normal-weight range and basal metabolic rate from a
  weight and the owner details, and when each can be computed.

### Modified Capabilities
- `home-screen`: the simple home screen gains the derived-values row (or the hint) under the card.
- `weight-chart`: Historia's chart and statistics show one quantity at a time, switched by swipe or
  tab; scale rule, units and point details extended to BMI and basal metabolic rate; the plot
  starts right at the y-axis (bug fix).

## Impact

- Parts touched: app screens (home, Historia). Not touched: Health Connect writes, scale, widget,
  export/backup, build/CI.
- Code: new pure `chart/`-side calculations (or a `derived/` package), `HomeSummary`, `PeriodStats`,
  `WeightScale`, `ui/HomeScreen`, `ui/HistoryScreen`, `ui/WeightChart`, `MainViewModel` (reads
  `ProfilePrefs`).
- No new dependency: the formulas are a few lines each (no library offers more than the formula);
  the swipe uses Compose's own pager, the bands Vico's decorations already in the build.
