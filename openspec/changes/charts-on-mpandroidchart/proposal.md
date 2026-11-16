# Proposal

## Why

Every Historia chart is drawn by hand on a Compose Canvas: bars, lines, axes, legend, the crosshair
and its gestures, about 3 000 lines spread over ten files. Each new chart need means another
hand-written feature, and the basics a chart library already has are missing (a marker card at the
chosen day, pinch zoom, panning). The user decided on 2026-11-16 to move the charts onto a
maintained library ("Libraries first", "Stay on the library") and to take its defaults for the
basics.

## What Changes

- All Historia charts move to MPAndroidChart 4.0.1: the fixed tabs Paino, BMI, Syöty, Askeleet,
  Kulutus, the own charts' tabs (Allekkain and Päällekkäin) and the chart shown on the "Uusi
  kaavio" path (the editor saves straight into a tab, so the own chart tab is the preview).
- One generic chart component is fed by a list of series (bars, stacked bars or a line; left axis,
  right axis or no axis; colour, name, unit; average line). The fixed tabs become built-in chart
  definitions on it. The special parts (BMI bands, treat share, lepo/aktiivinen, weekly bars over
  90 days, average line, lone weight dots, side-by-side bars) are small extensions on top of the
  library.
- **BREAKING (look and feel):** where the current requirements differ from the library's default
  behaviour, the library default wins:
  - the one-line readout above the chart becomes the library's marker: a card at the chosen day
    with the date (and time for a weight) and one row per series with its colour sign, name and
    value with unit; it never runs off the chart's edges;
  - a tap selects the nearest value and a drag scrubs at once (no press-and-hold, no haptic tick);
    tapping the chosen value again, or empty plot, clears it;
  - the chosen bar takes the library's highlight tint (the other bars no longer dim to 60 %); bars
    get no vertical line, lines keep the library's vertical highlight line;
  - pinch zoom and pan on the time axis (stacked panels zoom together); the chart owns sideways
    gestures inside its plot, so the Historia pager is swiped outside the plot;
  - x-axis date labels are placed by the library (3 to 5, fitted to the width), no longer pinned to
    the period's first and last day;
  - average lines are drawn over all data; bars have no 1 dp minimum width;
  - the "Napauta päivää nähdäksesi arvot" placeholder and its reserved row are gone.
  The full list with reasons is in design.md "Requirements changed to library defaults".
- Kept: TalkBack actions "Edellinen päivä" / "Seuraava päivä" and a polite announcement of the
  marker's text; the selection shared by the stacked panels; units on the value axes and every
  number; the y-scale rules; light and dark theme; text that follows the system font size; the
  period range line; legends; "Avaa päivä ›" on Syöty is dropped (the day list opens Ruoka).
- The unused Vico dependency is removed. JitPack is added as a repository restricted to
  MPAndroidChart's group only.
- Out of scope: the front page cards' small charts (weight, food, steps, Kulutus and own chart
  cards) stay on their current drawing; moving them is a later change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `weight-chart`: point details, chart axes (date labels), chart crosshair and crosshair on every
  Historia chart (marker card, gestures, clearing), one quantity at a time (pager swipe outside the
  plot), custom charts (average line order, bar width), stacked layout (date row), new chart zoom.
- `energy-burn`: the Kulutus tab's readout rows on the marker card.

## Impact

- App screens: Historia (every chart tab and the own chart tabs). Front page, Ruoka, Asetukset,
  widgets, Health Connect, the scale and backups are untouched.
- Code: `ui/WeightChart`, `EnergyPage`, `StepsPage`, `BurnPage`, `CustomChartView` (tab mode),
  `Crosshair`, `ChartAxes`, `ChartPage`, `HistoryScreen`; `chart/TabReadouts`, `AxisTicks`;
  new pure chart-definition builders under `chart/` and a library-backed chart under `ui/`.
  `ChartPanels` and the card mode of `CustomChartView` stay for the front page cards.
- Build/CI: new dependencies `com.github.PhilJay.MPAndroidChart:MPChartLib:v4.0.1` and
  `MPChartCompose:v4.0.1` from JitPack (exclusive content), Vico removed; the release build's R8
  must keep the library working. Tests gain Robolectric native-graphics render tests that save PNGs
  under `app/build/`.
