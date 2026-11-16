## 1. Compact rows

- [ ] 1.1 `ui/FoodRow` (FoodScreen.kt): the name `maxLines = 1`, `TextOverflow.Ellipsis`, in a
  `weight(1f, fill = false)` slot so the treat sign stays visible; drop the clock text
  from the details line everywhere (amount only; `clock` stays only if used elsewhere).
- [ ] 1.2 Smoke test (Robolectric, made-up items): a timed item's row (sheet and Ruoka) shows the amount
  and no clock text; a long name renders on one line (`maxLines` via semantics or text layout).

## 2. Folding add panel

- [ ] 2.1 In `AddFoodContent`, a `folded` state (`rememberSaveable`, false on each open) and a
  handle row (32×4 dp bar, `onSurfaceVariant`, 48 dp touch height, centred) at the panel's top,
  test tag `addPanelHandle`. Drag with Compose foundation's `draggable` (vertical) on the handle
  and the heading/chip area: a downward drag past ~24 dp folds, an upward one unfolds; a tap
  toggles. The handle consumes its drags so the sheet frame's drag-to-close never sees them.
- [ ] 2.2 Folded: the heading, `FlowRow` and field are left out with `AnimatedVisibility`
  (expand/shrink vertically, ~200 ms); folding clears focus and hides the keyboard; unfolding does
  not open it. The message line stays above the handle. The handle is hidden while the query is
  non-empty. `chipRows` is computed as today.
- [ ] 2.3 TalkBack: the handle's `contentDescription` "Piilota lisäys" / "Näytä lisäys"
  (strings in `values/strings.xml`), `Role.Button`, click toggles.
- [ ] 2.4 Smoke tests: tapping the handle hides `addSuggestions` and `addField`; tapping
  again shows both; a new `AddFoodContent` starts unfolded.

## 3. Wrap-up

- [ ] 3.1 `./gradlew test lint assembleDebug` green.
- [ ] 3.2 Bump MINOR in `gradle.properties` (`appVersion`).
- [ ] 3.3 Roadmap backlog: add "food-sheet-compact" as done with the version; note the later idea
  (rows with name and kcal only plus a details button, user 2026-11-16) as an open backlog item.
