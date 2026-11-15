# Design: home-card-layout

## Context

`ui/HomeScreen.kt` `HomeContent` is a scrolling Column: the notices (`BackupNotice`, `CheckNotice`,
the clipboard offer line), `LatestCard` (full width), then a `Row` with `FoodCard` and `StepsCard`
(equal halves, `IntrinsicSize.Min`). The cards' small charts are fixed at `SMALL_CHART` (48 dp).
Periods live in `chart/PeriodPrefs` (prefs file `chart`, carried by Android transfer and cloud
backup, `res/xml/data_extraction_rules.xml`). `MainActivity` asks steps for the steps card's period
(`stepsModel.show(...)` while home is shown). Back rules are `MainUiState.back()`. Ruoka has its own
hand-rolled reorder list (`OrderList` in `ui/FoodScreen.kt`), a 1-D list; it stays as is.
`custom-charts` adds chart cards to this grid next.

## Goals / Non-Goals

**Goals:** a user-chosen order, size and visibility per card in a two-column grid; edit mode on the
real cards, unmistakable, with no live actions; discard on back; drag plus a non-drag path; a card
model that takes more card types (`custom-charts`).
**Non-Goals:** chart cards themselves, per-card settings other than size, narrow-tall cards, other
screens, changing Ruoka's order list.

## Decisions

1. **Grid model.** A card is `HomeCard(id, size)`, `size` one of `SMALL`, `WIDE`, `BIG`. The layout
   is `HomeLayout(shown: List<HomeCard>, hidden: List<HomeCard>)`. Ids are strings: `weight`, `food`,
   `steps` (`custom-charts` adds `chart:<n>`). Span: small 1 column, wide and big 2. Height: small
   and wide one row unit, big two units plus the 8 dp gap. Since big is always full width, the grid
   never has to pack a 2x2 card next to anything: rows are either two small cards, one small card
   with an empty right half, or one full-width card. Pure `HomeLayout.rows(shown)` returns the rows
   (for tests and for the TalkBack place numbers). No backfilling of a gap by a later small card
   (it would put cards out of the order the user sees in edit mode).
2. **Row unit height is fixed**, `ROW_UNIT = 248.dp × max(1, min(fontScale, 1.3))`, so every card in
   a row has the same height without intrinsic measuring (a lazy grid cannot measure a row's tallest
   item). 248 dp holds today's wide weight card and small food card at the default text size; each
   card lays out its fixed parts and gives its chart `Modifier.weight(1f)` (min 32 dp), so the chart
   grows with the size. Above 130 % text the cards may clip, accepted (the spec promises 130 %).
3. **Library: `sh.calvin.reorderable` (Calvin-LL/Reorderable) 3.1.0**, Apache-2.0, on Maven Central,
   last release 2026-04 (checked 2026-11-15 on Maven Central; GitHub API not reachable from the
   container). It reorders items of a `LazyVerticalGrid` with mixed spans (`ReorderableItem`,
   `Modifier.draggableHandle()`, `Modifier.longPressDraggableHandle()`), animates the others making
   room and has haptics hooks. The front page becomes one `LazyVerticalGrid(GridCells.Fixed(2))`:
   notices as full-span items above the cards (outside edit mode), the cards, and in edit mode the
   full-span "Lisää kortti"/"Palauta oletus" item under them. Writing 2-D drag in a grid by hand is
   exactly what the library is for. Ruoka's 1-D `OrderList` stays (it works; moving it is not this
   change's job). Gradle goes through Google's Maven Central mirror in cloud containers (install.sh).
4. **Edit state = a draft.** `rememberSaveable` holds `editing: Boolean` and the draft layout
   (serialised with the same codec as the preference, decision 7), so rotation and background keep
   both. Valmis writes the draft through `HomePrefs` into `MainUiState.homeLayout` (only when it
   differs from the saved one), then leaves; Peruuta and back drop the draft. A HOME-only
   `BackHandler(enabled = editing)` does the back; it is a deliberate exception to the one back rule
   (`MainUiState.back()`): the draft is local to the page and ends with it. When `state.screen`
   leaves HOME (an intent from a notification or widget), the draft is dropped
   (`LaunchedEffect(state.screen)`).
5. **Entering by long-press over the card's own clickables.** A wrapper modifier on every card uses
   `pointerInput` with `awaitEachGesture` on `PointerEventPass.Initial`: it watches the first down
   without consuming; if the pointer stays down past `viewConfiguration.longPressTimeoutMillis`
   without moving past touch slop, it fires `onEditStart` (haptic `LongPress`) and consumes every
   later change of that gesture, so the inner `clickable`, `Button` or period `Surface` sees a
   cancelled gesture and does not fire. A movement past slop before the timeout lets the scroll
   have it. Semantics: `customActions = "Muokkaa etusivua"` on each card.
6. **Edit-mode rendering.** The real card composable is drawn at its draft size inside a `Box`:
   content at `alpha = 0.55f`, `clearAndSetSemantics` (the card's own nodes are hidden from
   TalkBack), and a full-size transparent overlay that consumes taps (so no inner action fires). On
   top, a 48 dp strip (`surfaceContainerHighest` at 92 % opacity) with `ic_drag_handle`
   (`draggableHandle()`), the card's name (ellipsis) and an `ic_more_vert` menu button (new vector
   asset, Material "more_vert"). The whole card also has `longPressDraggableHandle()`. Outline 2 dp
   `primary`, card shape unchanged. The dragged card is drawn at full opacity with 8 dp shadow. The
   edit top bar: `TopAppBar` with `containerColor = secondaryContainer`, navigation icon `ic_close`
   (new vector asset, Material "close"; contentDescription "Peruuta"), title "Muokkaa etusivua", action `TextButton("Valmis")`.
   Edit-only content under the grid: `OutlinedButton("Lisää kortti")` (icon `ic_add`) and
   `TextButton("Palauta oletus")`, side by side, full-span item.
7. **Persistence:** `HomePrefs` over the existing `chart` prefs file, key `homeLayout`, one JSON
   string via kotlinx.serialization: `{"v":1,"shown":[{"id":"weight","size":"wide"},…],"hidden":[…]}`.
   Being in `chart.xml` it moves with Android's transfer like the periods (app-data-transfer);
   `DataTransferRulesTest` needs no change. Pure `HomeLayoutCodec`: decode (null, blank, bad JSON or
   an unknown `v` = default; unknown ids dropped; a duplicate id keeps its first entry; an unknown
   size = that card's default size; known ids missing from both lists appended to `shown` at their
   default size, in default order; an empty `shown` = default), encode. The set of known ids is a
   parameter so `custom-charts` can add the chart ids without touching the rule.
8. **Pure edits on `HomeLayout`:** `move(from, to)` within `shown`, `moveEarlier(id)`,
   `moveLater(id)`, `resize(id, size)`, `hide(id)` (refused when `shown.size == 1`), `show(id)`
   (from `hidden`, appended last with its kept size), `reset()` = default, `isDefault`. The drag's
   `onMove(from, to)` from Reorderable maps item keys (card ids) to indices of `shown`.
9. **Card looks per size** (spec Card sizes): each card composable takes `size: CardSize`. The
   wide-vs-small differences are an `if` per part, not three copies:
   - weight: small uses `headlineMedium` for the number, puts the change chip in the `FlowRow` (it
     wraps under), drops the range text, keeps `sparkChange`; big passes `scaleLabels = true` to
     `Sparkline` (top and bottom `kg` labels at the left, dots per `ChartDots.fit`).
   - food and steps: wide puts the average in its long form (new strings `home_food_average_long`
     "keskim. %1$s kcal/pv", `home_steps_average_long` "keskim. %1$s askelta/pv") and, for food,
     "+ Annos" at the end of the period row; big adds the scale's top label (the same rounding as
     Energia/Askeleet: `ceil(max × 1.1 / 500) × 500` kcal, steps to 1 000) and a dashed average line
     (`PathEffect.dashPathEffect`, `onSurfaceVariant`, 1 dp) when `averageKcal`/`averageSteps` exists.
   - gated states (permission, unavailable, loading, failed): unchanged texts, centred in the
     remaining space (`Modifier.weight(1f)` box).
10. **Data loading is unchanged.** A hidden card's data still loads as today (the food and steps
    view models read for Ruoka, Historia and the widget anyway); hiding is visual only. This keeps
    the change to the front page.
11. **Notices are hidden in edit mode**: their taps navigate away, which would drop the draft; they
    come back on leaving edit mode.
12. **Hide guard:** the last shown card cannot be hidden, so the front page is never empty and there
    is always a card to long-press.
13. **Accessibility:** in edit mode each card's semantics: contentDescription "<name>, <place>/<count>,
    <size>" (sizes "pieni", "leveä", "iso"), custom actions Siirrä aiemmaksi, Siirrä myöhemmäksi,
    Pieni, Leveä, Iso, Piilota (only the applicable ones); after a move or resize
    `announceForAccessibility` of the new place ("Ruoka, 1/3"). The menu is the visible non-drag path.
14. **Logging:** `home.edit open`, `home.edit cancel changed=true|false`, and on Valmis
    `home.layout set=weight:wide,food:small,steps:small hidden=` (ids and sizes only; with custom
    charts the chart ids `chart:<n>`, never names). Added to the event list in `.claude/CLAUDE.md`.
15. **Spec wording:** "Simple home screen", "Food row" and "Steps card" are MODIFIED in this change
    (steps is archived, so its text is in the main spec); "Steps card height" is REMOVED in favour of
    the grid's one-height rows.

## Risks / Trade-offs

- A fixed row height can leave free space in a sparse card (steps without permission) and clip
  above 130 % text; the chart absorbs the difference at normal sizes.
- The long-press interceptor on `PointerEventPass.Initial` is delicate: it must not delay taps
  (it never consumes before the timeout) nor block the page scroll (it gives up past slop). A
  Robolectric test covers "long-press on + Annos starts edit mode and does not open the sheet" and
  "a plain tap on + Annos still opens it".
- A new dependency (Reorderable) in a lazy grid with mixed spans; fallback if it misbehaves on the
  device: drag only from the handle (`draggableHandle`) and keep the menu path.
- A lone small card leaves an empty half; the user can make it wide.
- Hiding the weight card removes Punnitse from the front page (weigh-in stays on the widget, tile
  and shortcut); the user's choice.
