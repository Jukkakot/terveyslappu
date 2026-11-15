# Design: custom-charts

## Context

Historia (`ui/HistoryScreen.kt`) is a `LazyColumn`: period chips, a `PrimaryScrollableTabRow` and a
`HorizontalPager` with pages 0..n-1 = `Quantity` (Paino, BMI: `QuantityPage`, Vico chart in
`WeightChart.kt`), `ENERGY_PAGE` (`EnergyPage`, Canvas, `food/EnergySeries`) and `STEPS_PAGE`
(`StepsTab`, Canvas, `steps/StepsSeries`). Energia and Askeleet draw the weights under their bars
with `PeriodWeights` (`ui/EnergyPage.kt`). Over 90 days `EnergySeries`/`StepsSeries` are weekly and
have no `average` line. Data: weights in `MainUiState`, food in `FoodUiState.items` (all own items
are read), steps in `StepsUiState.days` (read from the widest `StepsViewModel.show(from)` asked).
Tab entry triggers reads (`actions.onEnergyShown`, `stepsActions.onShown`). After
`home-card-layout` the front page is a `LazyVerticalGrid` of `HomeCard(id, size)` from `HomeLayout`,
stored by `HomeLayoutCodec` with the known ids as a parameter, and edit mode has "Lisää kortti".
Chart knowledge: global bank `~/.claude/knowledge.md` "Charts" (whole period on x, one scale rule
per quantity, units on every axis, direction not by colour alone).

## Goals / Non-Goals

**Goals:** any number of user charts; per data bars or line; stacked or overlaid; one definition
in a Historia tab and optionally a front page card; a small create/edit/delete flow; reuse the
existing series and drawing; readable on a phone.
**Non-Goals:** BMI as data, data order inside a chart, tab order, statistics or a list on chart
tabs, export, zoom.

## Decisions

1. **Model.** `CustomChart(id: Int, name: String, series: List<ChartSeries>, layout: ChartLayout)`,
   `ChartSeries(kind: SeriesKind {WEIGHT, ENERGY, STEPS}, style: SeriesStyle {BARS, LINE})`,
   `ChartLayout {STACKED, OVERLAID}`. Two charts may share a name (no uniqueness rule; tabs and
   cards are told apart by place); the name field stops input at 24 characters. `series` is kept sorted by `kind` order and non-empty, at most
   one entry per kind. Ids are ints from a stored `next` counter, never reused, so a deleted chart's
   card id cannot point at a new chart. Pure `CustomCharts` rules: `newDraft(existing)` (name "Kaavio
   n", lowest n not used, defaults per spec), `validName` (trimmed, 1-24 chars), `toggle(kind)` (the
   last one stays), `setStyle`, `add`, `update`, `delete`.
2. **One definition, two places (Q18 recommendation).** The tab is always there; the card is a
   `HomeCard("chart:<id>", size)` in `HomeLayout`. The codec's known ids are the fixed ids plus
   `chart:<id>` of every existing chart, so a deleted chart's card is dropped on the next decode;
   `MainViewModel` also removes it from the saved layout at delete time (no wait for a restart).
   Charts not on the front page are not in `hidden` by default; "Lisää kortti" lists `hidden` plus
   every chart whose id is in neither list, then "Uusi kaavio".
3. **Storage:** `ChartStore` over the `chart` prefs file, key `customCharts`, kotlinx.serialization
   JSON `{"v":1,"next":3,"charts":[{"id":1,"name":"…","layout":"stacked","series":[{"kind":"weight","style":"line"},…]}]}`;
   decode failures = no charts (logged once, `chart.custom.decode failed`). Chart card periods in
   the same file, key `chartDays:<id>`, `HomeSummary.homeDays` rule, default 30. `chart.xml` already
   moves with Android transfer (app-data-transfer), so nothing changes in `data_extraction_rules.xml`.
   Singleton in `TerveysApp`, state in `MainUiState.charts`.
4. **Rendering: own Canvas, not Vico.** Vico (already used for Paino/BMI) can overlay column and line
   layers with start and end axes, but it cannot draw one crosshair through several stacked charts,
   its own scroll/zoom/marker gestures would fight the pager's swipe and the long-press scrub, and it
   has no axis for a third scale. The Energia and Askeleet tabs already draw these exact bars and
   lines in Canvas; extracting them gives one renderer for tabs, the new charts and the cards. No
   other Compose chart library was checked beyond Vico (network to GitHub is blocked in the
   container); the extraction is small and keeps one look.
5. **Shared drawing (`ui/chart/` or `ui/ChartPanels.kt`).** Pure `ChartAxis(from, to, weekly,
   days)` with `x(day) = (daysBetween(from, day) + 0.5) × dayW` (day centres) used by every panel,
   bar and crosshair. Panel drawers take `(axis, data, style, scale, area, crosshairDay)` and draw
   into a `DrawScope` area: `drawBars`, `drawLine`, `drawWeights` (line + dots), each with the
   existing rules (bar 70 % of its slot, gaps for missing days, 2 dp lines). `EnergyChart`,
   `StepsChart` and `PeriodWeights` call them, unchanged in look (their tests stay green); the
   x mapping on those two tabs moves to day centres too (bars already sit centred; the weight line
   shifts half a day, accepted).
6. **Data per style** (pure `ChartData.of(chart, period, today, weights, foodItems, stepDays)`):
   weight line = non-"?" weights in range; weight bars = per day (week over 90 days) average of
   non-"?" weights, drawn from the `WeightScale` bottom (Q16); energy bars = `EnergySeries.bars`;
   energy line = `EnergySeries.average`, or over 90 days the weekly bars' values at the weeks'
   centres (Q17); steps likewise from `StepsSeries`. Scales: weight `WeightScale.of(shown kg)`;
   kcal `ceil(max × 1.1 / 500) × 500` (as Energia, min 500); steps rounded up to 1 000 × 1.1
   (as Askeleet). One `ChartData` per chart feeds the tab, the card and the readout.
7. **Stacked:** panels in kind order; tab heights 112/136/160 dp per 3/2/1 panels; each panel's
   scale top label with unit at its top left (`labelSmall`); "viikoittain" note once above;
   Energia's legend line under its panel when bars are shown (food, treats); a line-style Energia
   or Askeleet panel adds "7 pv keskiarvo" (over 90 days "viikoittain") after its scale label, so
   the line is not read as daily values; one date row under the stack. Card: the chart area split evenly between
   panels, no labels below big, min 24 dp per panel.
8. **Overlaid:** 200 dp on a tab. Bars first, lines on top. With k bar data the day slot is split
   in k equal sub-slots (each bar 70 % of its sub-slot), in kind order. Tones: weight `secondary`,
   energy `primary` (no treats split, so tones stay distinct), steps `tertiary`. Left axis = first
   shown kind, right axis = second, both with 3 labels (bottom, middle, top) with unit in the data's
   tone; a third data gets no axis and its legend entry reads its scale "Askeleet 0–12 000" (Q15).
   Legend under the plot: swatch (block for bars, short line for lines) + name per data; a line
   of Energia or Askeleet reads "Energia (7 pv ka.)" / "Askeleet (7 pv ka.)". One data =
   the same plot as a stacked single panel (left axis only).
9. **Crosshair and readout** (from the first draft, now per chart): pure `ChartReadout.of(day,
   data)`: daily: date, last non-"?" weight ("ei painoa"), kcal ("ei kirjauksia"), steps ("ei
   tietoa"); weekly: "vk 46 (10.–16.11.)", week average weight (non-"?"), average kcal per logged
   day and steps per day with steps, today left out (the bars' numbers), "ei tietoa" for an empty
   part; data without permission left out; today unmarked. Gestures on the chart's `pointerInput`:
   `detectTapGestures` sets the day; `detectDragGesturesAfterLongPress` scrubs and owns both axes
   until release (the pager and the list do not move). Over 90 days snap to the week, line at the
   week's centre. State `rememberSaveable(chart.id, period) { day }`; cleared on tab change, period
   change, editor save and leaving Historia; kept on data reload while its day is still on the axis
   (cleared when a Kaikki start moves past it). The readout slot keeps its height when empty and
   shows the hint "Napauta päivää nähdäksesi arvot", so the chart does not jump. Readout above the chart; a tap on
   it clears. Line 1.5 dp `onSurface` through the whole stack or plot. TalkBack: custom actions
   "Edellinen päivä"/"Seuraava päivä" on the chart (first use picks the newest day), readout a
   polite live region updated on tap, drop or action only.
10. **Historia tabs:** the pager's page count = 4 fixed + charts + 1. Page index → `HistoryPage`
    sealed type (`Fixed(quantity)`, `Energy`, `Steps`, `Custom(id)`, `NewChart`) through a pure
    `HistoryPages.of(charts)` so index math lives in one tested place. Tab text = name, `maxLines =
    1`, ellipsis, max width 160 dp. After a create, `animateScrollToPage` to the new chart's page;
    after a delete, to the page before it. A chart page: a top row with the "Muokkaa" `TextButton`
    (icon `ic_edit`, new vector, Material "edit") at the end, the readout slot, the chart. The
    "Uusi kaavio" page: one line (`chart_new_intro`) and a `FilledTonalButton` "Uusi kaavio" (not
    filled: the front page's Punnitse rule is home-only, but a tonal button keeps Historia calm).
11. **Editor:** a `Dialog` (`usePlatformDefaultWidth = false`, 24 dp side margin) with a title
    ("Uusi kaavio" / "Muokkaa kaaviota"), `OutlinedTextField` name (single line, 24 chars max,
    error text "Anna nimi" when blank), three rows: name + `Switch` + `SingleChoiceSegmentedButtonRow`
    (Pylväät, Viiva; disabled when off), "Asettelu": segmented Allekkain / Päällekkäin with the line
    "Jokainen tieto omassa kuvassaan" / "Kaikki samassa kuvassa, kaksi asteikkoa", and at the bottom
    "Poista kaavio" (error tone TextButton, only for an existing chart) at the start and Peruuta /
    Tallenna at the end. The editor works on a local draft (`rememberSaveable`); Tallenna calls
    `MainActions.onSaveChart(chart)`; Poista asks in an `AlertDialog` and calls `onDeleteChart(id)`.
    No preview (the chart behind the dialog updates on save).
12. **Front page card (`ChartCard`):** title row (icon `ic_chart`, new vector, Material
    "insert_chart_outlined"; name; chevron), the chart via the shared drawers filling the free space
    (decision 7/8 card rules), period label (`HomePeriodLabel`, own `chartDays:<id>`), dates at
    wide and big, overlaid legend at wide and big, scale labels at big. Tap (except the period label)
    → `actions.onOpenChart(id)` = Historia on that page (a `MainUiState.historyChart` id like
    `historyOnSteps`). Long-press → edit mode (from `home-card-layout`). Loading on home: when a
    chart card with steps is shown, `MainActivity` asks `stepsModel.show(from)` for the widest of the
    steps card's and those charts' periods; food is read in full already.
13. **"Lisää kortti" from the front page → "Uusi kaavio":** opens the same editor over the home
    screen; on Tallenna the chart is saved at once (Historia has it) and `HomeCard("chart:<id>",
    WIDE)` is appended to the edit draft. Peruuta on the edit mode then leaves the chart in
    Historia only (spec).
14. **"Palauta oletus" in edit mode** restores the fixed cards' default layout and takes every
    chart card off the edited layout (the charts stay in Historia and in "Lisää kortti").
15. **Permissions and reads:** a chart page calls `onEnergyShown` when it shows Energia and food
    access is GRANTED or to refresh the state (it never calls `foodActions.onGrant` by itself),
    and `stepsActions.onShown(from)` when it shows Askeleet, both on page entry and on a period
    change, like the fixed tabs. Kaikki start = min of the first days of the shown, loaded data.
16. **Logging:** `chart.custom create|edit|delete id= series=weight:line,energy:bars layout=stacked`
    and `chart.custom shown id= period=` on page entry; never names or values. Added to the event
    list in `.claude/CLAUDE.md`.
17. **Spec wording:** "One quantity at a time" is MODIFIED here (fixed tabs, then own charts, then
    Uusi kaavio). home-screen, help-page and app-data-transfer get ADDED requirements instead of
    edits to the requirements `home-card-layout` changes, so the two changes do not edit the same
    block; at archive, check that "Hidden cards" (Lisää kortti disabled when nothing is hidden) and
    "Chart cards" (always enabled) read together: the later "Chart cards" rule wins, and the archive
    task rewords "Hidden cards" accordingly.

## Risks / Trade-offs

- Overlaid charts with two scales invite reading false correlation (the reason the first draft
  refused them); the user asked for the choice, and a new chart defaults to Allekkain.
- Weight bars drawn from the scale bottom break the "bars start at zero" rule; from zero they would
  all look the same height (Q16).
- Extracting the drawing touches the working Energia and Askeleet tabs; their smoke tests must stay
  green, and the half-day shift of their weight line is a visible change.
- Many charts make a long tab row; it scrolls, and names are capped.
- A small chart card with three stacked panels is tiny (about 40 dp per panel); the user picks the size.
