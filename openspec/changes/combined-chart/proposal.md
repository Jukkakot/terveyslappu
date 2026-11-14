# Proposal: combined-chart

## Why

Weight, food and steps are now all in Historia, one quantity per tab. Energia and Askeleet show the weight line under their bars, but there is no view that puts the three together. Roadmap "combined chart" (user 2026-11-14): weight, kcal and steps in one chart, the user picks which are shown; avoid a two-axis plot (hard to read), e.g. stacked panels on one time axis. General chart knowledge (global bank `~/.claude/knowledge.md`, not present in this container; project knowledge.md has no chart section) was not available, so the choice rests on the design below.

## What Changes

- A fifth Historia tab **Yhdistelmä** after Askeleet. It shows the chosen quantities as **stacked panels on one shared time axis** (no two-axis plot): Paino (weight line, weight scale), Energia (daily kcal bars with the treats' share, 7-day average line) and Askeleet (daily step bars, 7-day average line), always in that order, top to bottom.
- **Choice chips** on the tab (Paino, Energia, Askeleet; any combination, at least one stays on), remembered, all three on by default.
- Uses Historia's period chips; one shared x-axis (dates under the bottom panel only), the same weekly bars over 90 days as the single tabs.
- **Crosshair:** tapping, or press-and-hold then dragging, puts one vertical line through all panels at that day, and one readout above the panels shows that day's weight, kcal and steps. A plain horizontal swipe still switches tabs.
- A panel with no data or no permission keeps its place with a one-line reason (and a grant button), so the layout does not jump.
- Nothing new is read or stored beyond the chip choice; no list of days under the panels. Not in this change: BMI panel (Q13), user-orderable panels, a front page combined card, exports of the chart.
- Parts touched: Historia (HistoryScreen, EnergyPage/StepsPage drawing extracted for reuse), app preferences (chip choice), Ohje.

## Impact

weight-chart spec (Combined chart ...). Depends on `steps` (Askeleet, `StepsSeries`, `PeriodWeights`) being merged first. Questions for the user: Q11-Q13 in context/questions.md.
