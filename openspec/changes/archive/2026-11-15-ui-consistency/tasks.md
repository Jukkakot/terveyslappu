Start only after `fixed-tabs-single-data` is merged to `main`.

## 1. Delete and Kumoa

- [x] 1.1 Historia weight rows: remove the long-press delete; trash button tinted `colorScheme.error` (the "?" row's trash too)
- [x] 1.2 Item editor (`FoodSheet.FoodEditor`) "Poista", product editor (`ProductsPage`) "Poista tuote": error colour, on the left of the dialog's buttons, Peruuta then Tallenna/Lisää on the right
- [x] 1.3 Ruokakirjaus product removal: "<name> poistettu · Kumoa" bar; Kumoa restores via `ProductStore.restore` (as the sheet's trash does)
- [x] 1.4 Own chart delete: drop the confirm dialog; delete at once with "Kaavio poistettu · Kumoa"; Kumoa restores the chart (same id, name, series, layout, period) and its front page card at its old place and size (`ChartStore`, `HomePrefs`)
- [x] 1.5 AI preview: trash in error colour; removing a row shows "<name> poistettu · Kumoa" (in the preview's own snackbar/line); Kumoa puts it back at the same index
- [x] 1.6 Clipboard card ✕: "Ohitettu · Kumoa"; Kumoa clears the skip mark so the offer shows again (bar outside the sheet, sheet line inside it)
- [x] 1.7 Settings: `SettingsScreen` copy-log message and `ListenSection` messages through `showTimed`
- [x] 1.8 Unit tests for the undo logic (chart restore incl. home layout position, preview row restore, skip undo)

## 2. Buttons and rows

- [x] 2.1 Ruoka bottom "Lisää annos": filled `Button`; Uusi kaavio tab's button: filled `Button`
- [x] 2.2 Rows: min height 56 dp and 16 dp start/end content edge on Historia weights, Energia, Askeleet, Ruoka, Ruokakirjaus, AI preview; sheet suggestion rows aligned to the same edge
- [x] 2.3 Strings for the new messages; Ohje: no long-press delete mention if any; `help_charts_body` unchanged
- [x] 2.4 Update UI tests that used long-press delete or the chart confirm dialog; one smoke test that chart delete shows Kumoa

## 3. Finish

- [x] 3.1 Bump `appVersion` to the next MINOR; `./gradlew test lint assembleDebug`
