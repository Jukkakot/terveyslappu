## 1. Rules (pure)

- [ ] 1.1 `CombinedAxis.of(period, today, panels, firstDays)` (shared from/to, Kaikki from the first day with data among shown panels whose data is loaded, weekly mode over 90 days, partial first/last weeks clipped)
- [ ] 1.2 `CombinedReadout.of(day, weights, energy, steps)`: daily: date, last non-"?" weight of the day ("ei painoa"), kcal ("ei kirjauksia"), steps ("ei tietoa"); weekly: "vk 46 (10.–16.11.)" with the week's average non-"?" weight, average kcal and steps per logged day (today left out, same numbers as the bars), "ei tietoa" for a part with no days; gated panels left out; today's running values unmarked
- [ ] 1.3 `CombinedPanels` selection rule (parse/serialise `weight,energy,steps`, at least one stays on, fixed order)
- [ ] 1.4 Unit tests with made-up data: Kaikki start, weekly mode and week snapping, readout with missing parts, selection rule

## 2. Historia: Yhdistelmä tab

- [ ] 2.1 Extract the Canvas panels from `EnergyChart`, `StepsChart` and `PeriodWeights` into reusable panel composables taking explicit from/to (not their own span), height and an optional crosshair day; one `x(day)` using day centres, `(daysBetween(from, day) + 0.5) * dayW`, for every panel including the weight dots on this tab; partial first/last weeks clipped; the single tabs use them unchanged in behaviour; existing smoke tests stay green
- [ ] 2.2 Fifth tab and pager page `COMBINED_PAGE`, `combinedPanels` preference (default all on), filter chips; panel heights 112 dp (3 panels), 136 dp (2), 160 dp (1); one shared "viikoittain" note above the panels over 90 days; Energia's legend as one line under its panel; one date row under the whole stack, always shown; Historia stays on Yhdistelmä while composed, reopening shows Paino
- [ ] 2.3 Crosshair: tap sets it, a long-press drag scrubs and owns both axes until release; panel taps never open anything; a tap on the readout or the panels' empty margin clears it; chip and tab changes, period change and leaving Historia clear it; it survives data reloads; weekly snapping with the line at the week's centre; one readout above the panels
- [ ] 2.4 Empty and gated panels keep their height with the message centred (no data lines, reasons with grant buttons, loading indicator); no automatic permission request; the tab triggers the energy and steps reads (the steps read also for the combined start) and shows the same history notice as other tabs
- [ ] 2.5 TalkBack: panel descriptions; custom actions "Edellinen päivä" / "Seuraava päivä" on the stack starting from the newest day; readout as a polite live region updated only on tap or drop; `chart.combined` log event on tab entry and on each chip change, added to the event list in `.claude/CLAUDE.md`
- [ ] 2.6 Smoke tests: three panels shown and a chip hides one; a tap shows the readout and a tap on the readout clears it; a fling still pages tabs; no-steps-permission panel shows its reason at the same height while the others draw

## 3. Help, context

- [ ] 3.1 Ohje: a short section on Yhdistelmä (chips, tap, press-and-hold)
- [ ] 3.2 At archive, reword "One quantity at a time" in weight-chart (and the steps delta's "All four tabs") for all five tabs with a reason each, and Yhdistelmä as the one tab whose quantities are chosen and stacked; update the map in `.claude/CLAUDE.md` (`CombinedAxis`, `CombinedReadout`, `CombinedPanels`, panel composables), product.md if needed, mark the roadmap item done; add a chart knowledge note if research taught something; bump MINOR; fold the answers to Q11-Q13 and delete their rows
