# Design: combined-chart

## Context

Historia (`ui/HistoryScreen.kt`) is a `LazyColumn` with period chips, a scrollable tab row and a `HorizontalPager`: pages 0..n-1 are `Quantity` (Paino, BMI, `QuantityPage`), then `ENERGY_PAGE` (`EnergyPage`, `EnergySeries`) and `STEPS_PAGE` (`StepsTab`, `StepsSeries`, from `steps`). Energia and Askeleet draw their own Canvas bars with the weight line under them (`PeriodWeights`, shared). Data: weights from the main state, food items from `FoodUiState`, steps from `StepsUiState`; permissions and reads are triggered by tab entry (`onEnergyShown`, `onStepsShown`, `steps.onShown`). Specs: weight-chart (+ steps delta). Knowledge: no chart section in project knowledge.md; the global bank `~/.claude/knowledge.md` is not in this container (read it at apply time).

## Goals / Non-Goals

**Goals:** see weight, food and steps move together on one time axis, readable on a phone, the user picks the panels; reuse the existing series (no new reads, no new rules for bars and averages).
**Non-Goals:** two-axis plots, BMI panel (Q13), user-orderable panels, a day list or statistics on this tab, a front page card, a goal or trend analysis.

## Decisions

Open taste choices are Q11-Q13; the rest is decided here.

1. **Stacked panels on one shared time axis, not an overlay.** A weight line (about 60-120 kg) against kcal (about 0-4000) or steps (0-20000) needs two value axes or a normalised index, both hard to read and easy to misread as correlation. Small multiples with the same x each keep their honest scale (and the existing weight scale and units on the chart). Alternative rejected: normalised 0-100 % overlay (loses units).
2. **A new fifth tab "Yhdistelmä", not a mode on an existing tab (Q11).** The single tabs stay simple and keep their lists and statistics; a mode switch on Energia or Askeleet would make "what am I looking at" depend on hidden state, and the weight line under their bars would have to be redefined. The tab row is already scrollable. Pager page `COMBINED_PAGE = STEPS_PAGE + 1`; the page is not opened from anywhere else, and Historia still opens on Paino.
3. **Panel order fixed: Paino, Energia, Askeleet.** Weight on top as the outcome, then food (input) and steps (activity); the same order as the front page default and the tabs. User-orderable would add UI for little.
4. **Selection by FilterChips, at least one on, default all (Q12)**, remembered in a preference `combinedPanels` ("weight,energy,steps") next to the period prefs, not in the data file or backup (a viewing choice).
5. **Shared axis and period.** The panels take Historia's `state.period` and one `from`/`to` (`ChartAxis` for the span: from `StepsSeries.dates`/`EnergySeries` logic, Kaikki = earliest first day of the shown panels' data or today). One x mapping `x(day) = daysBetween(from, day) * dayW` for every panel so crosshair x is identical; the date labels sit under the bottom shown panel only. Weekly bars over 90 days as in the single tabs: the crosshair then snaps to the week.
6. **Reuse by extraction, not copy:** the Canvas bodies of `EnergyChart`, `StepsChart` and the weight line in `PeriodWeights` become panel composables taking `(series, from, to, height, crosshairDay, modifier)` and drawing their own y scale label; the single tabs call them with a taller height and keep their tap details. The weight panel uses the weight scale of the Paino chart (`chart/WeightScale`) over the shown period, drawn as a line like `PeriodWeights` (dots when they fit stay a Paino-only detail). Panel height about 112 dp for three panels (fits a phone with the chips and tab row; scrolls with the rest of the LazyColumn); a single shown panel gets the single tab's 160 dp.
7. **Crosshair (pure `CombinedReadout.of(day, weights, energy, steps)` makes the readout text).** Input: tap sets the day; press-and-hold then drag scrubs (`detectDragGesturesAfterLongPress`), because a plain horizontal drag belongs to the pager. State is local `rememberSaveable` (day), dropped on period or chip change. One readout line above the panels, so no panel's drawing is hidden under a finger. The line is drawn per panel in the on-surface tone with 1.5 dp stroke.
8. **Weight value in the readout:** the day's last weight not marked "?" (the same weights the chart uses); no weight that day: "ei painoa" (no interpolation).
9. **Empty and gated panels keep their place** (a fixed one-line state, `heightIn` of the panel) so toggling or loading does not reflow the others. Reasons and grant buttons are the single tabs' own composables (`EnergyReason`, `StepsReason`); the tab never requests permissions by itself (Energia does on open, steps only on tap; a combined tab asking food permission on open would surprise). Data loading is triggered like the single tabs: `onEnergyShown` when the tab is on and the energy panel is shown, `steps.onShown` likewise.
10. **Accessibility:** each panel has a contentDescription ("Paino, viivakaavio, 31 päivää"), the readout is a live text; the chips are the keyboard path. The tap crosshair needs no precision (day columns, minimum width per day clamp).
11. **Weight-chart spec wording:** "One quantity at a time" (and the steps delta's "All four tabs") must be reworded at archive: the five tabs and this exception (task 3.2).
12. Log `chart.combined panels=weight,energy,steps period=` when the tab is shown; no values.

## Risks / Trade-offs

- Three panels on a phone are small (about 112 dp each): fewer axis labels (max label per panel only), no day list; acceptable since the single tabs hold the details.
- Press-and-hold scrubbing is not discoverable: tap works without it, and Ohje explains it.
- Weekly mode (over 90 days) mixes granularities: the weight panel stays a daily line, kcal and steps are weekly bars; the readout then names the week and gives the week's average weight, kcal and steps.
- Extracting the single tabs' drawing touches working code: their existing smoke tests must stay green.
