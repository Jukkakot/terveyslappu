# Design

## Context

Eight small fixes from the 2026-11-19 review plus one found in the screenshot tour (changes that
disagree with the shown weights: the tour's own fixtures show "82,3 → 81,8 kg" with "−0,6 kg").
`everyday-feedback` is merged and archived (v1.64), so its files (the weigh-in dialog's change
line, `auto/AutoText`) are in scope for the shown-change rule. Specs: the six deltas.

## Goals / Non-Goals

**Goals:** fix each item with the smallest change that follows Material 3 and the app's existing
patterns; every item gets a test that fails on the old behaviour.

**Non-Goals:** the weight card's first-day label (12.11.
vs Historia's 13.11. for 1 vk comes from the rolling 7 × 24 h window and needs its own decision),
any new screen or term (no glossary change needed).

## Decisions

1. **Units: non-breaking characters in the strings, FlowRow for the average.**    Number–unit gaps
   become U+00A0 and the compound units get a word joiner after the slash (`askelta/⁠pv`, as
   `steps_per_day` already does). Every card string is listed (`kg_value`, `home_spark_range` with
   no break before "kg" and around "→", `food_kcal` in the big card's scale label, the average
   strings, `steps_value`, the Kulutus equivalents), and one unit test runs over that list. The
   card's bottom row is a `FlowRow` of the period label and the average. Small card: the average
   is always a Column of two non-wrapping lines ("keskiarvo", value), end-aligned; the no-average
   text ("ei annoksia") is one wrapping line. Wide and big: one non-wrapping line in the long form.
   The layout moves the average under the label when it does not fit, at every size (new split
   string resources as needed). Alternative: measuring with
   `SubcomposeLayout` (more code, same result).
2. **One decimal for Viikossa, unit "kg/vk".** The scale and the shown weights have 0.1 kg
   resolution, so hundredths were false precision, and two decimals next to one decimal read as a
   contradiction. "kg/vk" follows "kcal/pv" (every number carries its unit, show-units-everywhere);
   the label stays "Viikossa". A slow year reads ±0,0 kg/vk: honest at this resolution, and Muutos
   beside it still shows the year's change. Alternative kept out: a new label ("Tahti") needs a
   glossary word.
3. **Shown change = difference of rounded values, one rounding function.** One pure helper
   (`derived/ShownChange.kt`) holds `shownTenths(value): Long`, rounded exactly like `formatKg`'s
   `%.1f` (half up on the decimal text, so 27.049999999999997 is 270, not 271 or 269 by float
   luck), and `shownDelta(from, to)` = whole tenths of `to` minus whole tenths of `from`, as a Long.
   `formatKg`, `formatChange` and every caller use it, so display and arithmetic cannot differ.
   Callers: `HomeSummary.sparkChange`, `PeriodStats.change`/`perWeek`, `ListGroups` (row and group
   change), BMI the same way, and, since everyday-feedback is merged, the weigh-in dialog's change
   (`WeightChange` built in `scale/WeighIn.kt` and `WeighInViewModel`) and the notification
   (`auto/AutoText.change` and its "Poikkeaa" deviation). `DerivedUi.changeText` only formats.
   Viikossa = shown change (tenths) ÷ the period's exact span in weeks as `PeriodStats` computes
   it, null under one day, rounded half away from zero to one decimal; a value that rounds to 0
   reads "±0,0 kg/vk" with no arrow, never "−0,0". Unit tests include 27.049999999999997, a
   Viikossa of −0.05 (→ −0,1) and −0.0049 (→ ±0,0).
4. **Large text: no cap, reflow above 130 %.** `rowUnit()` = `ROW_UNIT × max(1, fontScale)` with
   no upper bound. Above 1.3 (`fontScale > 1.3f`, Float; at exactly 1.3f nothing reflows and food
   and steps still share a row) the grid draws each SMALL card as WIDE (own row) at render time
   only, keeping the cards' order (the left of a shared row first); `HomeLayout` and the stored
   sizes stay untouched. Edit mode draws the same reflowed grid, with Pieni still checked in the
   menu and no hint. Why: at
   200 % a half-width card (~156 dp of content) cannot hold a 600-weight title, which is never
   shortened (home-screen "Card titles"), nor "askelta/pv" whole; reflow to one column is the
   standard answer (WCAG 1.4.10). Alternative: shrink text in cards (forbidden: never clamp the
   font scale).
5. **"Uusi kaavio" as a "+" icon tab** rather than a ⋮ menu item. The page with its one-line
   explanation stays (first-time users learn what own charts are), the add lives next to the
   charts it adds, and "+" at the end of a tab strip is a familiar pattern (browser and spreadsheet
   tabs). The ⋮ menu is shared with the front page and holds daily actions only; a Historia-only
   item there would make one menu differ by screen. Icon tab: `Tab(icon = …)` with an `Icon`
   whose contentDescription is "Uusi kaavio" (TalkBack: a tab, with its selected state), min width
   48 dp; the test tag `tab-NEW` stays and tests assert by tag, not by text.
6. **Theme and Värit as radio rows, not a SegmentedButton.** The brief suggested a segmented
   button, but the code records that one was tried and broke "Järjestelmä" mid-word at 360 dp: the
   card leaves ~296 dp, a third is ~98 dp, and "Järjestelmä" at labelLarge plus 12 dp padding each
   side (plus the check icon when chosen) needs ~110–135 dp, before any text scaling. Radio rows
   (`Modifier.selectableGroup()`, `selectable(role = RadioButton)` on each 48 dp row) are Android's
   own convention for the theme setting, never wrap at 200 %, and the Ulkoasu screen has the room.
   Other chip groups (gender, Tunnistusnopeus, units) stay: they fit one row.
7. **Järjestä in a ⋮ menu**, shown under the sort icon's old rule (granted, day loaded, more than one
   annos that day), so the 200 % `LargeTextTest` fixture needs at least two annokset. Järjestä is a rare action; a text button next to Viikko would push
   the title out at 200 % text (back + "Ruoka" + "Viikko" + "Järjestä" ≈ 420 dp at 360 dp width),
   and no icon is conventional for manual reordering. A ⋮ ("Lisää toimintoja", the name the app
   menu already uses) with "Järjestä annokset" is labelled, scales, and keeps Viikko visible. One
   extra tap on a rare path.
8. **Food card: one `clickable` on the card**, like `StepsCard`: the inner title/bars/average
   clickables go; the period label keeps its own menu (as the steps card). Permission NEEDS → asks
   for it; DENIED (Android no longer shows its dialog) and GRANTED → Ruoka on today, which offers
   the grant. The help text `help_food_body` (Ohje) is updated to say the card opens the day's
   annokset (it names the bars opening Historia today); no help-page requirement names the tap. `onOpenEnergy` stays in `MainActions` (Ruoka's Viikko uses it) but the
   card no longer takes it.
9. **Recent amounts: a pure rule in `food/`** (e.g. `FoodRules.recentAmounts(items, key, unit,
   exclude, limit = 3)`): annokset matched by `FoodItem.key` (not the typed name) and the amount
   unit, over all days, newest first by day then logging time, the edited annos left out, Yhteensä
   annokset skipped, distinct by the amount's shown text, first three; no chips while the annokset
   load. The field keeps its number when the unit switches between kpl and g. `FoodEditor` gets the
   list via a parameter from its two callers (`MainScreen`, `FoodAddDialog`), computed from
   `food.items` (all of the app's own annokset are loaded). Chips: `FilterChip` with `selected` =
   value equals the field, in a `FlowRow` under the amount field; tap sets the field text with
   `numberText`. The key comes from the name the editor opened with, so typing a new name does not
   reshuffle the chips.
10. **Stale names:** the deltas use Historia's tabs as they are (Paino, BMI, Syöty, Askeleet,
    Kulutus; the old "Energia" is gone), the one-tap-target rule covers every feature card.

11. **Screenshot tour at 200 %:** a second loop with `RuntimeEnvironment.setFontScale(2f)` (or a
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
