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
   item). 248 dp is the starting estimate; the final value comes from the size matrix test (task
   2.4: every card type at every size at 360 dp and font scale 1.3, key nodes displayed): raise it
   until the matrix passes. It must hold today's wide weight card and small food card; each
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
4. **Edit state = a draft, at `HomeScreen` level** (above the grid and the top bar, so the edit bar
   and the cards share it). `rememberSaveable` holds `editing: Boolean` and the draft layout
   (serialised with the same codec as the preference, decision 7), so rotation and background keep
   both. Valmis writes the draft through `HomePrefs` into `MainUiState.homeLayout` (only when it
   differs from the saved one), then leaves; Peruuta and back drop the draft. A HOME-only
   `BackHandler(enabled = editing)` does the back; it is a deliberate exception to the one back rule
   (`MainUiState.back()`): the draft is local to the page and ends with it. Any outside intent discards the
   draft and leaves edit mode first: the food sheet (`FoodAddActivity` hand-off, `openFrom`), the
   weigh-in, a notification action, another screen (`state.screen` leaves HOME,
   `LaunchedEffect(state.screen)`), and losing Health Connect access (the home screen's gated
   states replace the cards).
5. **Entering by long-press over the card's own clickables, continuing into a drag.** A wrapper
   modifier on every card uses `pointerInput` with `awaitEachGesture` on `PointerEventPass.Initial`:
   it watches the first down without consuming; if the pointer stays down past
   `viewConfiguration.longPressTimeoutMillis` without moving past touch slop, it fires
   `onEditStart` (haptic `LongPress`) and consumes every later change of that gesture, so the inner
   `clickable`, `Button` or period `Surface` sees a cancelled gesture and does not fire. A movement
   past slop before the timeout lets the scroll have it. **The same gesture then continues into a
   drag** (like a launcher home screen): once edit mode is on, if the held finger moves past slop,
   the interceptor hands the pointer to the card's drag (Reorderable's state-level start/drag/stop
   from the pressed card's key and the pointer position, the interceptor forwarding the move events
   until release); a release without movement just leaves edit mode on. The implementer checks
   that Reorderable 3.1.0 can start a drag from outside its own gesture detector; if not, the
   interceptor keeps owning the gesture and drives the state's drag itself, and says so in the
   commit. **In edit mode the entry interceptor is off** (no new long-press entry, no consuming).
   Semantics: `customActions = "Muokkaa etusivua"` on each card.
6. **Edit-mode rendering.** The real card composable is drawn at its draft size inside a `Box`:
   content at `alpha = 0.55f`, `clearAndSetSemantics` (the card's own nodes are hidden from
   TalkBack), and a full-size transparent overlay that consumes **only taps** (`detectTapGestures`; no inner action
   fires, but drags and long-presses pass through to the card body's `longPressDraggableHandle()`,
   so a long-press drag on the card body reorders). On
   top, a 48 dp strip (`surfaceContainerHighest` at 92 % opacity) with `ic_drag_handle`
   (`draggableHandle()`), the card's name (ellipsis) and an `ic_more_vert` menu button (new vector
   asset, Material "more_vert"). The whole card also has `longPressDraggableHandle()`. Outline 2 dp
   `primary`, card shape unchanged. The dragged card is drawn at full opacity with 8 dp shadow. The
   edit top bar: `TopAppBar` with `containerColor = secondaryContainer`, navigation icon `ic_close`
   (new vector asset, Material "close"; contentDescription "Peruuta"), title "Muokkaa etusivua", action `TextButton("Valmis")`.
   Edit-only content under the grid: `OutlinedButton("Lisää kortti")` (icon `ic_add`) and
   `TextButton("Palauta oletus")`, side by side, full-span item. "Lisää kortti" opens a
   `DropdownMenu` anchored to the button (scrolling: a max height with `verticalScroll`, since
   `custom-charts` adds many items), one item per hidden card (icon + name). Card names in the
   strip, the menu and TalkBack: "Paino", "Ruoka", "Askeleet" (`ic_scale`, `ic_restaurant`,
   `ic_walk`). Valmis with no change leaves edit mode without writing or logging a layout.
7. **Persistence (two id sets):** `HomePrefs` over the existing `chart` prefs file, key `homeLayout`, one JSON
   string via kotlinx.serialization: `{"v":1,"shown":[{"id":"weight","size":"wide"},…],"hidden":[…]}`.
   Being in `chart.xml` it moves with Android's transfer like the periods (app-data-transfer);
   `DataTransferRulesTest` needs no change. Pure `HomeLayoutCodec`: decode (null, blank, bad JSON or
   an unknown `v` = default; unknown ids dropped; a duplicate id keeps its first entry; an unknown
   size = that card's default size; **fixed** ids (`weight`, `food`, `steps`) missing from both lists appended to `shown` at their
   default size, in default order; an empty `shown` = default), encode. The codec takes two id sets:
   the fixed ids with default sizes (appended when missing) and an `allowedExtra` set (`custom-charts`
   passes `chart:<id>` of every existing chart): extra ids are kept when listed and allowed, dropped
   when not, and NEVER appended. Test: a stored layout without `chart:2` while chart 2 exists
   decodes without it.
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
     "+ Annos" at the end of the period row; big adds the scale's top label (the same rule as
     Energia/Askeleet) and a dashed average line
     (`PathEffect.dashPathEffect`, `onSurfaceVariant`, 1 dp) when `averageKcal`/`averageSteps` exists.
     The scale top is `steps-source-and-axes`' `AxisTicks.bars` top: kcal
     `ceil(max × 1.1 / 500) × 500` (min 500); steps `ceil(max × 1.1 / 2000) × 2000` (min 2 000), the
     real Askeleet rule (11 400 steps give 14 000).
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
    Pieni, Leveä, Iso, Piilota (only the applicable ones); after a move and after a resize
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
  device: drag only from the handle (`draggableHandle`) and keep the menu path. The long-press that continues into a drag needs a drag started from outside
  Reorderable's own detector (decision 5); if that cannot be made to work the entering long-press
  ends at edit mode (a second press drags) and the commit says so.
- A lone small card leaves an empty half; the user can make it wide.
- Landscape and wide screens keep two columns with 248 dp rows, so the page scrolls sooner; the
  app is used in portrait.
- Hiding the weight card removes Punnitse from the front page (weigh-in stays on the widget, tile
  and shortcut); the user's choice.

## Implementation notes (apply, 2026-11-15)

Choices the spec left open, taken in the simplest reading:

- Long-press into drag (decision 5): Reorderable 3.1.0 keeps its drag start internal, but
  `draggableHandle(dragGestureDetector = …)` takes a custom detector. `LongPressEntry` is that
  detector (Initial pass, long-press timeout without slop, then consumes the gesture and feeds the
  drag); its `onDragStarted` enters edit mode. So the first link of the chain works: the press that
  starts edit mode continues into the drag, and in edit mode the same detector is the card body's
  long-press drag. Verified only under Robolectric.
- Reorderable moves a card once the dragged card covers the other card's centre; a small card
  dragged onto a wide one has to reach its middle.
- `ROW_UNIT` stays 248 dp: the size matrix passes there (200 dp fails).
- Pure code lives in a new `home/` package (`HomeLayout`, `HomeLayoutCodec`, `HomePrefs`,
  `HomeEditSignal`); the grid and edit mode in `ui/HomeEdit.kt`, the cards in `ui/HomeScreen.kt`.
- A listed allowed-extra card with an unknown size decodes as wide.
- Outside intents: `HomeEditSignal` from `MainActivity.onNewIntent`, `FoodAddActivity`,
  `WeighInActivity` and `AutoActionReceiver`; besides those the draft is dropped when the add
  sheet, the food editor, the AI preview, manual entry or the import dialog opens, access is lost
  or the screen leaves HOME (the last one without a `home.edit cancel` line: the page leaves
  composition).
- The Lisää kortti menu is a `DropdownMenu` with a max height; it scrolls by itself.
- The menu marks the current size with a check icon; TalkBack's actions leave out the current size.
- Small weight card: the period row holds the period label and the period's change.
- Big weight card: the scale's top and bottom labels sit in a narrow column left of the line.
- Big food and steps cards: the scale top label reuses `food_kcal` / `steps_value`.
- The steps card's gated states (permission, unavailable, loading, failed) moved from the number
  slot into the centred free space, like the food card's.
