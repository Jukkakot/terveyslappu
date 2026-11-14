## 1. Rules (pure)

- [ ] 1.1 `CombinedAxis.of(period, today, panels, firstDays)` (shared from/to, Kaikki from the earliest first day of the shown panels, weekly mode over 90 days)
- [ ] 1.2 `CombinedReadout.of(day, weights, energy, steps)`: date, weight (last non-"?" weight of the day, weekly mode the week's average), kcal, steps, "ei painoa" / "ei tietoa" parts
- [ ] 1.3 `CombinedPanels` selection rule (parse/serialise `weight,energy,steps`, at least one stays on, fixed order)
- [ ] 1.4 Unit tests with made-up data: Kaikki start, weekly mode, readout with missing parts, selection rule

## 2. Historia: Yhdistelmä tab

- [ ] 2.1 Extract the Canvas panels from `EnergyChart`, `StepsChart` and `PeriodWeights` into reusable panel composables (height, shared axis, optional crosshair day); the single tabs use them unchanged in behaviour; existing smoke tests stay green
- [ ] 2.2 Fifth tab and pager page `COMBINED_PAGE`, `combinedPanels` preference (default all on), filter chips, panels stacked on the shared axis with dates under the bottom panel only
- [ ] 2.3 Crosshair: tap, tap again or outside to clear, press-and-hold drag scrub, clears on period or chip change; one readout line above the panels
- [ ] 2.4 Empty and gated panels (no data lines, reasons with grant buttons, loading indicator); no automatic permission request; trigger the energy and steps reads when their panels are shown
- [ ] 2.5 Accessibility descriptions per panel; `chart.combined` log event, added to the event list in `.claude/CLAUDE.md`
- [ ] 2.6 Smoke tests: three panels shown and a chip hides one; a tap shows the readout; no-steps-permission panel shows its reason while the others draw

## 3. Help, context

- [ ] 3.1 Ohje: a short section on Yhdistelmä (chips, tap, press-and-hold)
- [ ] 3.2 At archive, reword "One quantity at a time" in weight-chart (and the steps delta's "All four tabs") for five tabs and the combined exception; update the map in `.claude/CLAUDE.md` (`CombinedAxis`, `CombinedReadout`, `CombinedPanels`, panel composables), product.md if needed, mark the roadmap item done; add a chart knowledge note if research taught something; bump MINOR; fold the answers to Q11-Q13 and delete their rows
