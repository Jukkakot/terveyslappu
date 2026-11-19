# Design

## Context

Eight small fixes from the 2026-11-19 review plus one found in the screenshot tour (changes that
disagree with the shown weights: the tour's own fixtures show "82,3 → 81,8 kg" with "−0,6 kg").
`everyday-feedback` runs at the same time and owns `ui/FoodSearch.kt`, `ui/AppSnackbar.kt`,
`ui/WeighInDialog.kt`, `ui/WeighInClose.kt`, the notification and the widgets; this change does
not edit them. Specs: the four deltas.

## Goals / Non-Goals

**Goals:** fix each item with the smallest change that follows Material 3 and the app's existing
patterns; every item gets a test that fails on the old behaviour.

**Non-Goals:** the weigh-in dialog's change line (same rounding rule, but its file is
everyday-feedback's; a follow-up can adopt the helper), the weight card's first-day label (12.11.
vs Historia's 13.11. for 1 vk comes from the rolling 7 × 24 h window and needs its own decision),
any new screen or term (no glossary change needed).

## Decisions

1. **Units: non-breaking characters in the strings, FlowRow for the average.** Number–unit gaps
   become U+00A0 and the compound units get a word joiner after the slash (`askelta/⁠pv`, as
   `steps_per_day` already does). The small card's bottom row becomes a `FlowRow` of the period
   label and the average; the average is a Column of two non-wrapping lines ("keskiarvo", value),
   so FlowRow moves it under the label when it does not fit. Alternative: measuring with
   `SubcomposeLayout` (more code, same result).
2. **One decimal for Viikossa, unit "kg/vk".** The scale and the shown weights have 0.1 kg
   resolution, so hundredths were false precision, and two decimals next to one decimal read as a
   contradiction. "kg/vk" follows "kcal/pv" (every number carries its unit, show-units-everywhere);
   the label stays "Viikossa". A slow year reads ±0,0 kg/vk: honest at this resolution, and Muutos
   beside it still shows the year's change. Alternative kept out: a new label ("Tahti") needs a
   glossary word.
3. **Shown change = difference of rounded values.** One pure helper (e.g.
   `derived/ShownChange.kt`: `shownDelta(from, to, decimals = 1)` = round(to) − round(from)) used by
   `HomeSummary.sparkChange`, `PeriodStats.change`/`perWeek` (perWeek from the shown change) and
   `ListGroups` (the row's `Weight.change` and the group's `change`; `DerivedUi.changeText` only
   formats). BMI uses the same helper on the BMI
   values. Arrows already follow the shown value (`shownDirection`).
4. **Large text: no cap, reflow above 130 %.** `rowUnit()` = `ROW_UNIT × max(1, fontScale)` with
   no upper bound. Above 1.3 the grid draws each SMALL card as WIDE (own row) at render time only;
   `HomeLayout` and the stored sizes stay untouched, edit mode still shows Pieni checked. Why: at
   200 % a half-width card (~156 dp of content) cannot hold a 600-weight title, which is never
   shortened (home-screen "Card titles"), nor "askelta/pv" whole; reflow to one column is the
   standard answer (WCAG 1.4.10). Alternative: shrink text in cards (forbidden: never clamp the
   font scale).
5. **"Uusi kaavio" as a "+" icon tab** rather than a ⋮ menu item. The page with its one-line
   explanation stays (first-time users learn what own charts are), the add lives next to the
   charts it adds, and "+" at the end of a tab strip is a familiar pattern (browser and spreadsheet
   tabs). The ⋮ menu is shared with the front page and holds daily actions only; a Historia-only
   item there would make one menu differ by screen. Icon tab: `CompactTab` with `ic_add`
   (contentDescription "Uusi kaavio"), min width 48 dp.
6. **Theme and Värit as radio rows, not a SegmentedButton.** The brief suggested a segmented
   button, but the code records that one was tried and broke "Järjestelmä" mid-word at 360 dp: the
   card leaves ~296 dp, a third is ~98 dp, and "Järjestelmä" at labelLarge plus 12 dp padding each
   side (plus the check icon when chosen) needs ~110–135 dp, before any text scaling. Radio rows
   (`Modifier.selectableGroup()`, `selectable(role = RadioButton)` on each 48 dp row) are Android's
   own convention for the theme setting, never wrap at 200 %, and the Ulkoasu screen has the room.
   Other chip groups (gender, Tunnistusnopeus, units) stay: they fit one row.
7. **Järjestä in a ⋮ menu.** Järjestä is a rare action; a text button next to Viikko would push
   the title out at 200 % text (back + "Ruoka" + "Viikko" + "Järjestä" ≈ 420 dp at 360 dp width),
   and no icon is conventional for manual reordering. A ⋮ ("Lisää toimintoja", the name the app
   menu already uses) with "Järjestä annokset" is labelled, scales, and keeps Viikko visible. One
   extra tap on a rare path.
8. **Food card: one `clickable` on the card**, like `StepsCard`: the inner title/bars/average
   clickables go; the period label keeps its own menu (as the steps card). NEEDS → grant, DENIED and
   GRANTED → Ruoka on today. `onOpenEnergy` stays in `MainActions` (Ruoka's Viikko uses it) but the
   card no longer takes it.
9. **Recent amounts: a pure rule in `food/`** (e.g. `FoodRules.recentAmounts(items, key, unit,
   exclude, limit = 3)`): annokset with the same name key and amount unit, newest first by day then
   logging time, the edited annos left out, distinct by value, first three. `FoodEditor` gets the
   list via a parameter from its two callers (`MainScreen`, `FoodAddDialog`), computed from
   `food.items` (all of the app's own annokset are loaded). Chips: `FilterChip` with `selected` =
   value equals the field, in a `FlowRow` under the amount field; tap sets the field text with
   `numberText`. The key comes from the name the editor opened with, so typing a new name does not
   reshuffle the chips.
10. **Screenshot tour at 200 %:** a second loop with `RuntimeEnvironment.setFontScale(2f)` (or a
    second test method with `@Config(fontScale = 2f)`) renders etusivu (scrolled to the end as a
    second shot), Ruoka and Ulkoasu, brand colours, both themes, with `-200` in the file name.
    Assertions live in `LargeTextTest`, not in the tour.

## Risks / Trade-offs

- [The 130 % reflow makes the front page long at big text] → it already scrolls for larger
  layouts; nothing is cut, which is the requirement.
- [FlowRow moves the average under the label at 130 % and steals plot height] → the existing
  56 dp plot test at 130 % (`HomeDashboardTest`) must still pass; if not, the row unit grows by the
  line's height at that size.
- [One-decimal Viikossa hides slow trends] → Muutos shows the period's change; the trend line
  shows the shape.
- [The ⋮ hides Järjestä one level deeper] → it is named and rare; the hint line in order mode is
  unchanged.
