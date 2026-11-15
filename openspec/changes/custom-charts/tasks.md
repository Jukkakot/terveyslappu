## 0. Prerequisite

- [ ] 0.1 Check that `home-card-layout` is implemented and archived (`openspec list` does not show it; `openspec/specs/home-screen/spec.md` has "Card grid" and "Hidden cards"; `HomeLayout`, `HomeLayoutCodec` exist). If not, stop and report: this change builds on its grid. Verify: those files and requirements exist

## 1. Rules and storage (pure + prefs)

- [ ] 1.1 `CustomChart`, `ChartSeries`, `SeriesKind`, `SeriesStyle`, `ChartLayout` and the `CustomCharts` rules (design 1): `newDraft(existing)` ("Kaavio n" with the lowest free n; all three on; weight LINE, energy and steps BARS; STACKED), `validName` (trimmed 1-24 chars), `toggle` (the last stays on, a switched-off kind keeps its style in the draft), `setStyle`, `add` (id from `next`), `update`, `delete`. Verify: unit tests for each rule, incl. "Kaavio 2" when "Kaavio 1" exists and "Kaavio 1" again after it was renamed
- [ ] 1.2 `ChartStore` (design 3): JSON in the `chart` prefs file, key `customCharts` (`v`, `next`, `charts`), decode failure = no charts with one `chart.custom.decode failed` log; chart card periods `chartDays:<id>` (default 30, `HomeSummary.homeDays`); singleton in `TerveysApp`; `MainUiState.charts` loaded at start; `MainActions.onSaveChart`, `onDeleteChart` (also drops `chart:<id>` from the saved `HomeLayout` and its `chartDays` key), logging `chart.custom create|edit|delete id= series= layout=`. Verify: unit tests for the codec round trip and a bad JSON; a view-model test that delete removes the card from the saved layout
- [ ] 1.3 Pass `chart:<id>` of every chart as known ids to `HomeLayoutCodec` (design 2); default size of a chart card WIDE. Verify: unit test that a layout naming a deleted chart decodes without it
- [ ] 1.4 `ChartAxis` (day-centre `x`, weekly over 90 days, partial weeks clipped, Kaikki = first day with data among the shown loaded kinds) and `ChartData.of(chart, period, today, weights, foodItems, stepDays)` (design 6: weight line and weight bars from non-"?" weights, energy and steps bars from `EnergySeries`/`StepsSeries`, lines = their 7-day averages or over 90 days the weekly values at week centres, the scales). Verify: unit tests with made-up data for Kaikki start, weekly mode, weight bar averages, the weekly line, scales
- [ ] 1.5 `ChartReadout.of(day, data, access)` (design 9: daily and weekly texts, "?" left out, missing parts, gated kinds left out, today unmarked) and `HistoryPages.of(charts)` (design 10: index ↔ page, "Uusi kaavio" last, the page after a create and after a delete). Verify: unit tests for each case

## 2. Shared drawing

- [ ] 2.1 Extract the bar, line and weight drawing of `EnergyChart`, `StepsChart` and `PeriodWeights` into shared drawers over `ChartAxis` (design 5); the two tabs use them with the same look (the weight line moves to day centres). Verify: the existing Energia and Askeleet smoke tests stay green; `./gradlew test`
- [ ] 2.2 `CustomChartView(chart, data, mode = TAB | CARD(size), crosshairDay, …)`: stacked panels (design 7: heights 112/136/160 on a tab, split evenly on a card, scale top labels, one "viikoittain" note, Energia legend line, one date row) and overlaid plot (design 8: 200 dp on a tab, bars behind lines, sub-slots for several bar kinds, tones secondary/primary/tertiary, left and right axes with units in the data's tone, third kind's scale in the legend, legend under the plot); empty, gated and loading per spec "Chart data without values". Verify: Robolectric smoke tests: three stacked panels shown; overlaid with weight + energy shows both axis labels and the legend; no steps permission in stacked shows the reason at the panel's place

## 3. Historia

- [ ] 3.1 Tabs and pages from `HistoryPages` (design 10): own chart tabs (name, one line, ellipsis, max 160 dp) and the last "Uusi kaavio" tab; the chart page with the "Muokkaa" button (new `ic_edit`), the readout slot and the chart; the "Uusi kaavio" page with its line and button; no list under either; Historia still opens on Paino; after a create the new chart's tab, after a delete the tab before. Verify: tests "two charts give seven tabs in order", "Historia reopens on Paino", "after delete the previous tab is shown"
- [ ] 3.2 Reads on page entry and period change (design 15): energy and steps like the fixed tabs, no permission request by the chart; the history notice as on the other tabs; `chart.custom shown id= period=`. Verify: a test that opening a chart page with steps calls the steps read with the period's start and does not ask the nutrition permission
- [ ] 3.3 Crosshair (design 9): the readout slot with its hint when empty; tap sets, long-press drag scrubs and owns both axes until release, weekly snap, a tap on the readout clears, cleared on tab/period change, editor save and leaving Historia, kept on reload; TalkBack actions and the polite live region. Verify: tests "a tap shows the readout and a tap on it clears", "a fling still pages", "Edellinen päivä with no crosshair picks the newest day"
- [ ] 3.4 Editor dialog (design 11): name, three rows (switch + Pylväät/Viiva), Asettelu with its lines, Peruuta/Tallenna, "Poista kaavio" with its confirmation; local draft survives rotation; back and outside tap discard without asking. Verify: tests "new chart defaults", "the last data stays on", "blank name disables Tallenna", "delete asks and removes the tab", "rename changes the tab text"

## 4. Front page

- [ ] 4.1 `ChartCard` at three sizes (design 12, spec "Chart cards"): title row with `ic_chart` (new vector) and the name, the chart filling the free space, own period label, dates and overlaid legend at wide/big, scale labels at big, gated data left out with the short panel lines, "Ei tietoja tällä jaksolla"; a tap (except the period label) opens Historia on the chart's page (`MainUiState.historyChart`), back returns home. Verify: smoke tests for a small and a big chart card; a test that a tap opens Historia on that tab
- [ ] 4.2 "Lisää kortti" lists hidden feature cards, charts not on the front page and "Uusi kaavio" (always enabled); a chart is added last WIDE; "Uusi kaavio" opens the editor over home and on save appends the new chart's card to the draft (design 13); Piilota on a chart card only takes it off the front page; Palauta oletus takes all chart cards off (design 14). Verify: tests "add Liike from Lisää kortti then Valmis shows it last and wide", "new chart from the front page has a Historia tab even after Peruuta"
- [ ] 4.3 Steps read on home covers the widest shown chart card with steps (design 12). Verify: a test or a `MainActivity`-level check that `show(from)` gets the earlier of the steps card's and a 90-day chart card's start

## 5. Help, docs, version

- [ ] 5.1 Ohje: the own charts section (where they are made, bars or line, stacked or overlaid, tap or press-and-hold for a day's values). Verify: the help test still passes
- [ ] 5.2 Update `.claude/CLAUDE.md`: the map (`CustomCharts`, `ChartStore`, `ChartAxis`, `ChartData`, `ChartReadout`, `HistoryPages`, shared drawers, `ChartCard`, the editor) and the event list (`chart.custom …`); roadmap "combined chart" marked done; product.md if it names Historia's tabs; add to the global knowledge bank's Charts section only if research taught something new. Verify: `git diff` shows those files
- [ ] 5.3 Bump MINOR in `gradle.properties` `appVersion` (the last task); run `./gradlew test lint assembleDebug`. Verify: all green

## Workflow follow-up

- At archive: reword home-screen "Hidden cards" so "Lisää kortti" is always enabled and lists the charts and "Uusi kaavio" (design 17), and merge "Custom charts in Ohje" into "Ohje screen" if that reads better.
