## 1. Order rule and storage (pure + prefs)

- [ ] 1.1 Add the reorderable library to the version catalog and app dependencies (check it resolves through the mirrors cloud containers use; if not, say so here and use move buttons only)
- [ ] 1.2 `HomeOrder` (parse with unknown dropped, duplicates removed, missing appended; move up/down; move to index; serialise) and `HomeLayout.rows(order)` (weight full width, adjacent food/steps paired, lone half card full width)
- [ ] 1.3 Unit tests: layout for all six permutations of the three cards, parse edge cases, moves at the ends
- [ ] 1.4 `HomePrefs` `homeOrder` string, loaded into `MainUiState`, saved on change (not in the data file or backup); `home.order set=` log event, added to the event list in `.claude/CLAUDE.md`

## 2. Front page

- [ ] 2.1 Render `HomeContent` from the order through `HomeLayout.rows`; cards keep their content and equal-height rows
- [ ] 2.2 Top-bar edit button (pencil / check, contentDescription "Järjestä kortit" / "Valmis"), local edit state, `BackHandler` leaving edit mode
- [ ] 2.3 Edit mode: reorderable column with a drag handle, move up/down buttons and custom accessibility actions on each card, cards and quick actions inert, "Palauta oletusjärjestys"
- [ ] 2.4 Smoke tests: default order layout at about 360 dp; one edit-mode test (enter, move steps up with the button, order saved, back leaves edit mode); cards inert while editing

## 3. Help, context

- [ ] 3.1 Ohje: one sentence on the edit button
- [ ] 3.2 At archive, edit the sentence "The order SHALL be fixed for now" in home-screen "Simple home screen" to point to Card order; update the map in `.claude/CLAUDE.md` (`HomeOrder`, `HomeLayout`, `HomePrefs`), product.md if it names the order, mark the roadmap item done; bump MINOR; fold the answers to Q8-Q10 and delete their rows
