# Design: custom-charts

## Context

Historia (`ui/HistoryScreen.kt`) is a `LazyColumn`: period chips, a `PrimaryScrollableTabRow` and a
`HorizontalPager` with pages 0..n-1 = `Quantity` (Paino, BMI: `QuantityPage`, Vico chart in
`WeightChart.kt`), `ENERGY_PAGE` (`EnergyPage`, Canvas, `food/EnergySeries`) and `STEPS_PAGE`
(`StepsTab`, Canvas, `steps/StepsSeries`). Energia and Askeleet draw the weights under their bars
with `PeriodWeights` (`ui/EnergyPage.kt`). Over 90 days `EnergySeries`/`StepsSeries` are weekly
averages (ISO weeks, Monday start) with no `average` line; those fixed tabs stay as they are.
Data: weights in `MainUiState`, food in `FoodUiState.items` (all own items are read), steps in
`StepsUiState.days` (read from the widest `StepsViewModel.show(from)` asked). Tab entry triggers
reads (`actions.onEnergyShown`, `stepsActions.onShown`). `Period` (`chart/Period.kt`) is
`Last(days) | All | Custom(from, to)`; the shared chips and the Oma calendar
(`RangeCalendarDialog`) set it. After `home-card-layout` the front page is a `LazyVerticalGrid` of
`HomeCard(id, size)` from `HomeLayout`, stored by `HomeLayoutCodec` (fixed ids plus an allowed
extra id set), and edit mode has "Lisää kortti". After `steps-source-and-axes` there are the pure
`chart/AxisTicks` (`bars`, `line`, `dates`: y labels bottom/middle/top, 4-5 x dates) and the shared
`ChartLegend`. Chart knowledge: global bank `~/.claude/knowledge.md` "Charts" (whole period on x,
one scale rule per quantity, units on every axis, direction not by colour alone).

## Goals / Non-Goals

**Goals:** any number of user charts; per data bars or line; stacked or overlaid; real daily
values; each chart's own period; one definition in a Historia tab and optionally a front page card;
a small create/edit/delete flow; reuse the existing series, the shared axis helper and the drawing;
readable on a phone.
**Non-Goals:** BMI as data, data order inside a chart, tab order, statistics or a list on chart
tabs, export, zoom, weekly averages in custom charts.

## Decisions

1. **Model.** `CustomChart(id: Int, name: String, series: List<ChartSeries>, layout: ChartLayout,
   period: Period)`, `ChartSeries(kind: SeriesKind {WEIGHT, ENERGY, STEPS}, style: SeriesStyle
   {BARS, LINE})`, `ChartLayout {STACKED, OVERLAID}`. `series` is kept sorted by `kind` order and
   non-empty, at most one entry per kind. Ids are ints from a stored `next` counter, never reused,
   so a deleted chart's card id cannot point at a new chart. Names: trimmed, 1 to 24 grapheme
   clusters (`BreakIterator`, so an emoji counts one), stored trimmed, duplicates allowed (tabs and
   cards are told apart by place); only the default name "Kaavio n" is checked: lowest n for which
   no existing name equals "Kaavio n" case-insensitively after trimming. Pure `CustomCharts`
   rules: `newDraft(existing)` (defaults per spec, period `Last(30)`), `validName`, `toggle(kind)`
   (the last one stays), `setStyle`, `setPeriod`, `add`, `update`, `delete`.
2. **One definition, two places.** The tab is always there; the card is a `HomeCard("chart:<id>",
   size)` in `HomeLayout`. The codec's `allowedExtra` ids are `chart:<id>` of every existing chart
   (extra ids are kept when listed and allowed and NEVER appended, `home-card-layout` design 7), so
   a deleted chart's card is dropped on the next decode and a chart not on the front page is in
   neither list. `MainViewModel` also removes it from the saved layout at delete time. If the
   removal leaves `shown` empty (the chart was the only shown card) the **default layout is saved
   at once** (the three fixed cards shown, nothing hidden). "Lisää kortti" lists `hidden` plus every
   chart whose id is in neither list, **in tab order**, then "Uusi kaavio". **Piilota on a chart
   card removes its id from both lists** (not into `hidden`); the chart then reappears in
   "Lisää kortti" and comes back wide. `HomeLayout.isDefault` = the fixed cards in default order and
   sizes and **no chart id in either list**; `reset()` ("Palauta oletus") also clears chart ids.
3. **Storage:** `ChartStore` over the `chart` prefs file, key `customCharts`, kotlinx.serialization
   JSON `{"v":1,"next":3,"charts":[{"id":1,"name":"…","layout":"stacked","period":{"k":"last","days":30},"series":[{"kind":"weight","style":"line"},…]}]}`;
   period `k` = `last` (with `days`), `all` or `custom` (with `from`, `to` as ISO dates); decode
   failures = no charts (logged once, `chart.custom.decode failed`). The period is part of the
   definition: the tab's chips and the card read and write the same field (no separate card
   period). `chart.xml` already moves with Android transfer (app-data-transfer), so nothing changes
   in `data_extraction_rules.xml`. Singleton in `TerveysApp`, state in `MainUiState.charts`.
4. **Rendering: own Canvas, not Vico.** Vico (already used for Paino/BMI) can overlay column and line
   layers with start and end axes, but it cannot draw one crosshair through several stacked charts,
   its own scroll/zoom/marker gestures would fight the pager's swipe and the long-press scrub, and it
   has no axis for a third scale. The Energia and Askeleet tabs already draw these exact bars and
   lines in Canvas; extracting them gives one renderer for tabs, the new charts and the cards. No
   other Compose chart library was checked beyond Vico (network to GitHub is blocked in the
   container); the extraction is small and keeps one look.
5. **Axis, ticks and legend: reuse `steps-source-and-axes`.** Value labels (bottom, middle, top with
   unit in a left column of fixed width), the 4-5 dates under the plot and the legend (a swatch per
   line and bar) come from its pure `AxisTicks.bars/line/dates` and shared `ChartLegend`; custom-charts
   writes no axis rule of its own. Scale tops are the same as the fixed tabs': weight
   `WeightScale.of(shown kg)` (`AxisTicks.line`), kcal `ceil(max × 1.1 / 500) × 500` (min 500),
   steps `ceil(max × 1.1 / 2000) × 2000` (min 2 000), i.e. `AxisTicks.bars`; here `max` is the
   highest daily value drawn.
6. **Shared drawing (`ui/chart/` or `ui/ChartPanels.kt`).** Pure `ChartAxis(from, to, weekly)` with
   `x(day) = (daysBetween(from, day) + 0.5) × dayW` (day centres) used by every panel, bar and
   crosshair; `weekly` is true only for the fixed Energia and Askeleet tabs (ISO weeks, Monday
   start; the only place weeks remain), always false for custom charts. Drawers take
   `(axis, data, style, scale, area, crosshairDay)` into a `DrawScope` area: `drawBars`, `drawLine`,
   `drawWeights` (line + dots when they fit). Bars are 70 % of their slot but never narrower than
   1 dp (the minimum-width clamp that makes daily bars at 90 days, 1 v or Kaikki thin lines, not
   gaps); days without data are gaps; lines are 2 dp and join the days that have a value (as the
   weight line does). `EnergyChart`, `StepsChart` and `PeriodWeights` call them, unchanged in look
   (their tests stay green); their x mapping moves to day centres too (bars already sit centred;
   the weight line shifts half a day, accepted).
7. **Data (pure `ChartData.of(chart, today, weights, foodItems, stepDays)`), real values only.**
   The period is the chart's own (`Period.resolve`, Kaikki = first day with data among the shown,
   loaded data). Weight (line or bars) = each day's **last** weight not marked "?" in range, never
   an average; weight bars rise from the bottom of the `WeightScale` (Q16). Energy and steps = each
   day's own value from the daily series (`EnergySeries` and `StepsSeries` expose a daily entry
   point that ignores the weekly switch over 90 days; the fixed tabs keep calling the weekly one):
   bars one per logged day (Energia with the treats' share in its tone when stacked), lines join the
   days' own values. No 7-day average and no weekly value exists in `ChartData`. One `ChartData`
   per chart feeds the tab, the card and the readout.
8. **Stacked:** panels in kind order; tab heights 112/136/160 dp per 3/2/1 panels; each panel with
   its left label column (`AxisTicks`, unit); one date row under the stack (`AxisTicks.dates`,
   offset by the label column) and one `ChartLegend` under it naming every drawn line and bar
   (paino, ruoka, herkut when Energia bars, askeleet). No "viikoittain" or "7 pv keskiarvo" notes.
   Card: the chart area split evenly between panels, no labels below big, min 24 dp per panel.
9. **Overlaid:** 200 dp on a tab. Bars first, lines on top. With k bar data the day slot is split
   in k equal sub-slots (each bar 70 % of its sub-slot, min 1 dp), in kind order. Tones: weight
   `secondary`, energy `primary` (no treats split, so tones stay distinct), steps `tertiary`. The
   **first two data actually drawn** (kinds with values and access, in kind order) get the left and
   right value axis (`AxisTicks`, 3 labels, unit, in the data's tone); a **third drawn data gets no
   scale at all**, recognised by its colour and its legend name only (the legend does not carry a
   scale). Legend under the date row via `ChartLegend`, a swatch per drawn line or bar. **One data
   drawn = exactly the stacked single panel** (left axis only, same height rules as 1 panel).
10. **Crosshair and readout.** Pure `ChartReadout.of(day, data)`: date, that day's last non-"?"
    weight ("ei painoa"), kcal ("ei kirjauksia"), steps ("ei tietoa"); data without permission left
    out; today shows its running values unmarked; always daily (no week form, no averages).
    Gestures on the plot areas' `pointerInput` only (buttons inside panels, such as the grant
    button, stay buttons; taps outside drawn plot areas place nothing): a tap sets the day; a
    **press-and-hold released without moving places the crosshair** (haptic tick) and a hold
    followed by a drag scrubs and owns both axes until release (the pager and the list do not
    move). The 1.5 dp `onSurface` line is drawn only over panels that draw data (an empty or gated
    panel has none). State `rememberSaveable(chart.id) { day }`; cleared on tab change, period
    change, editor save and leaving Historia; kept on data reload and cleared only when its day
    leaves the axis `[start, end]` (a Kaikki start moving past it, a shorter period). The readout
    slot keeps its height when empty and shows the hint "Napauta päivää nähdäksesi arvot"; a tap on
    it clears. TalkBack: custom actions "Edellinen päivä"/"Seuraava päivä" on the chart (the first
    use starts from today), readout a polite live region updated on tap, drop or action only.
11. **Historia tabs and the chart's own period.** The pager's page count = 4 fixed + charts + 1.
    Page index → `HistoryPage` sealed type (`Fixed(quantity)`, `Energy`, `Steps`, `Custom(id)`,
    `NewChart`) through a pure `HistoryPages.of(charts)`. Tab text = name, `maxLines = 1`,
    ellipsis, max width 160 dp. After a create, `animateScrollToPage` to the new chart's page; after
    a delete, to the page before it. **The period chips above the tabs follow the current page:** on
    a `Custom(id)` page the row shows the same chips (1 vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with the
    calendar) bound to that chart's `period`, via `MainActions.onChartPeriod(id, period)` (saved at
    once into `ChartStore`, so the front page card follows); on every other page the shared chips
    as today, and the shared chips never affect a chart page. A chart page: a top row with the
    "Muokkaa" `TextButton` (icon `ic_edit`, new vector, Material "edit") at the end, the readout
    slot, the chart. The `NewChart` page: one line (`chart_new_intro`) and a `FilledTonalButton`
    "Uusi kaavio". New UI field `MainUiState.historyChart: Int?` (the chart to open, like
    `historyOnSteps`), cleared by `MainUiState.back()` and not kept when passing through Ruoka.
12. **Editor:** a `Dialog` (`usePlatformDefaultWidth = false`, 24 dp side margin) with a title
    ("Uusi kaavio" / "Muokkaa kaaviota"), `OutlinedTextField` name (single line, 24 graphemes max,
    error text "Anna nimi" when blank), three rows: name + `Switch` + `SingleChoiceSegmentedButtonRow`
    (Pylväät, Viiva; disabled when off), "Asettelu": segmented Allekkain / Päällekkäin with the line
    "Jokainen tieto omassa kuvassaan" / "Kaikki samassa kuvassa, kaksi asteikkoa", and at the bottom
    "Poista kaavio" (error tone TextButton, only for an existing chart) at the start and Peruuta /
    Tallenna at the end. The period is not in the editor (it is the chips on the tab); saving keeps
    it. The editor works on a local draft (`rememberSaveable`); Tallenna calls
    `MainActions.onSaveChart(chart)`; Poista asks in an `AlertDialog` and calls `onDeleteChart(id)`.
    No preview (the chart behind the dialog updates on save).
13. **Front page card (`ChartCard`):** title row (icon `ic_chart`, new vector, Material
    "insert_chart_outlined"; name; chevron), the chart via the shared drawers filling the free space
    (decision 8/9 card rules), the chart's period as a **plain label** (e.g. "1 kk", "Kaikki",
    "1.9.–15.10."; not tappable, no menu: the period is changed on the tab), dates at wide and big,
    the legend at wide and big, scale labels at big. Tap anywhere → `actions.onOpenChart(id)` =
    Historia on that page (`MainUiState.historyChart`). Long-press → edit mode (`home-card-layout`).
    Loading on home: when a chart card with steps is shown, `MainActivity` asks
    `stepsModel.show(from)` for the earliest of the steps card's and those charts' period starts
    (Kaikki as the Askeleet tab asks it); food is read in full already.
14. **"Lisää kortti" from the front page → "Uusi kaavio":** opens the same editor over the home
    screen (a local dialog, not an outside intent: the edit draft survives); on Tallenna the chart
    is saved at once (Historia has it) and `HomeCard("chart:<id>", WIDE)` is appended to the edit
    draft. Peruuta on the edit mode then leaves the chart in Historia only (spec).
15. **Permissions, loading and failure:** a chart page calls `onEnergyShown` when it shows Energia
    and `stepsActions.onShown(from)` when it shows Askeleet, on page entry and on a period change,
    like the fixed tabs; it NEVER calls `foodActions.onGrant` or the steps grant by itself (the
    grant buttons in a panel are ordinary buttons). Energia's and Askeleet's own tab taps stay
    exactly as now. While the weights are null or failed the chart page shows Historia's spinner or
    failure notice instead of the chart.
16. **Empty Kaikki:** Kaikki with no data in any shown data: the axis is just today (one date) and
    the chart reads "Ei tietoja tällä jaksolla" (home-screen and weight-chart specs).
17. **Logging:** `chart.custom create|edit|delete id= series=weight:line,energy:bars layout=stacked`,
    `chart.custom period id= period=last30|all|custom` and `chart.custom shown id= period=` on page
    entry; never names or values. Added to the event list in `.claude/CLAUDE.md`.
18. **Accessibility inherited:** chart cards use `home-card-layout`'s edit-mode semantics (place
    announced after a move and a resize).
19. **Spec wording:** "One quantity at a time" is MODIFIED here (fixed tabs, then own charts, then
    Uusi kaavio). home-screen, help-page and app-data-transfer get ADDED requirements instead of
    edits to the requirements `home-card-layout` changes, so the two changes do not edit the same
    block; at archive, check that "Hidden cards" (Lisää kortti disabled when nothing is hidden) and
    "Chart cards" (always enabled) read together: the later "Chart cards" rule wins, and the archive
    task rewords "Hidden cards" accordingly.

## Risks / Trade-offs

- Overlaid charts with two scales invite reading false correlation (the reason the first draft
  refused them); the user asked for the choice, and a new chart defaults to Allekkain. A third data
  with no scale can only be read by shape and through the crosshair readout.
- Weight bars drawn from the scale bottom break the "bars start at zero" rule; from zero they would
  all look the same height (Q16).
- Daily bars over a year or Kaikki are about 1 dp wide lines; readable as a silhouette, the
  crosshair gives the numbers.
- Extracting the drawing touches the working Energia and Askeleet tabs; their smoke tests must stay
  green, and the half-day shift of their weight line is a visible change.
- Many charts make a long tab row; it scrolls, and names are capped.
- A small chart card with three stacked panels is tiny (about 40 dp per panel); the user picks the size.
