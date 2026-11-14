## 1. Order rule and storage (pure + prefs)

- [ ] 1.1 Extract Ruoka's `OrderList` (`ui/FoodScreen.kt`: handle drag, up/down, `ic_drag_handle`, `ic_arrow_up`, `ic_arrow_down`, "Valmis") into a shared composable over generic rows; Ruoka uses it unchanged in behaviour (its tests stay green); no new dependency
- [ ] 1.2 `HomeOrder` (parse: empty or corrupt = default, unknown dropped, duplicates removed, missing appended in default order; move up/down; move to index; serialise) and `HomeLayout.rows(order)` (each card has a width attribute; half cards pair greedily left to right, a leftover half card full width)
- [ ] 1.3 Unit tests: layout for all six permutations of the three cards, greedy pairing, parse edge cases (empty, corrupt, unknown, duplicate, later-added card appended in default order), moves at the ends, reset is a no-op on the default
- [ ] 1.4 `HomePrefs` `homeOrder` string, loaded into `MainUiState`, saved on change (not in the data file or backup); `home.order set=` log event (arrows at once, a drag once on drop), added to the event list in `.claude/CLAUDE.md`

## 2. Front page

- [ ] 2.1 Render `HomeContent` from the order through `HomeLayout.rows`; each card picks its half or full (long forms) variant from its actual width; equal-height rows
- [ ] 2.2 Top-bar edit button (`ic_sort`, contentDescription "Järjestä kortit"); local edit state (`rememberSaveable`: survives rotation, ends on any screen change or when the app goes to background); HOME-only `BackHandler` leaving edit mode
- [ ] 2.3 Edit mode: the shared order list of compact rows (title, up, down, handle) in pure order; visible "Valmis" text button; "Palauta oletusjärjestys" (disabled at the default); notices stay visible and tappable
- [ ] 2.4 Accessibility: rows read "<card>, <pos>/<count>", the new position is announced after a move, arrow descriptions name the card
- [ ] 2.5 Tests: layout test of a lone half card at full width (long forms); default order layout at about 360 dp; one edit-mode smoke test (enter, move steps up with the button, order saved, back leaves edit mode); notice still tappable while editing

## 3. Help, context

- [ ] 3.1 Ohje: one sentence on the sort button
- [ ] 3.2 At archive, edit "Simple home screen" in home-screen: replace "The order SHALL be fixed for now" with a pointer to Card order, allow the edit button in the top bar and the "Palauta oletusjärjestys" action in "no action outside the cards"; update the map in `.claude/CLAUDE.md` (`HomeOrder`, `HomeLayout`, `HomePrefs`, shared order list), product.md if it names the order, mark the roadmap item done; bump MINOR; fold the answers to Q8-Q10 and Q14 and delete their rows
