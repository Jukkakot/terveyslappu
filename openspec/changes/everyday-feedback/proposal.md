# Proposal

## Why

The usability review of 2026-11-19 found that the everyday paths sometimes end without saying
what happened: the weigh-in dialog closes silently when nothing was saved, a dialog opened right
after an automatic save starts a new scan, the notification's Poista paino cannot be undone, Kumoa
bars ignore Android's accessibility timeout, and the add search forgets its adds as soon as the
bar fades. The user chose U1–U6 and U8 as one change (roadmap, "Usability review 2026-11-19").

## What Changes

- **Weigh-in end says why (U1):** when the 30 seconds run out without a saved weight the dialog no
  longer closes by itself. It shows why nothing was saved (vaakaa ei tunnistettu / paino ei
  vakiintunut / vaaka toisti äskeistä punnitusta, with that weight and its time) with Valmis and
  Punnitse uudelleen. While it is ignoring a repeated reading it says so instead of "Astu
  vaa'alle".
- **Dialog after an automatic save (U2):** opened within 10 minutes of an automatic weight (not
  "?"), the dialog shows that weight ("Tallennettu automaattisesti klo 7.12", change) with Poista
  and Valmis instead of scanning.
- **Undoable notification delete (U3):** Poista paino turns the notification into "Paino
  poistettu" with Kumoa for about 10 seconds; Kumoa brings the weight back as it was. The no-op OK
  goes; a swipe dismisses.
- **Accessibility timeout (U4):** Kumoa bars (4 s), the weigh-in result's auto-close (10 s) and
  the "Paino poistettu" notification (10 s) last longer when Android's accessibility timeout asks
  for it.
- **Adds stay visible in the search (U5):** rows of ruuat added in this search show ✓ (or ×2,
  ×3 …); a bottom strip "2 annosta lisätty · 465 kcal" with Valmis (same as back) shows while
  anything is added.
- **The add bar names the amount (U6):** "Rahka 200 g lisätty · Kumoa", "Pikalisäys 350 kcal
  lisätty".
- **Widget ✓ (U8):** while automatic weigh-in is on and the window's weight is saved, the Punnitse
  widget shows a ✓ instead of the dot (never the value); Asetukset's "Tänään punnittu" line starts
  with the same ✓.
- **Log:** `weighin.end reason=` for every dialog end, `weighin.recent`, `auto.action undo`,
  `auto.action delete ok ageS=`, `food.search.end added= via=`.

Touches app screens (weigh-in dialog, the add search, Asetukset status line), the weigh-in
notification, the Punnitse widget, Health Connect (re-write on Kumoa, one read when the dialog
opens) and the scale logic (what the repeat rules remember). No data file or build/CI change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: "Weigh-in dialog" (end state instead of a silent close), "Save the scale
  weight" (auto-close follows the accessibility timeout), "One-tap start" (✓ symbol), "Ignore the
  scale's lingering broadcast" and "Ignore the same weight repeated" (the dialog says it is
  ignoring); new "Open on a recent automatic weight", "Weigh-in end in the log".
- `background-weigh-in`: "Weigh-in notification", "Notification actions" (no OK, Poista paino
  with Kumoa), "Widget status" and "Status symbol in Asetukset" (✓ while weighed).
- `food-log`: "Adding from the search" (marks and the added strip), "Leaving the search" (Valmis,
  log line), "Search messages and loading" (bar names the amount).
- `app-appearance`: "Undo messages" (accessibility timeout).

## Impact

`scale/WeighIn.kt` (end reasons, ignored readings), `scale/WeighingKeys.kt` and
`scale/LastWeighIn.kt` (remember the time and whether saved or deleted), `ui/WeighInClose.kt`,
`ui/WeighInDialog.kt`, `ui/WeighInViewModel.kt`, `WeighInActivity` (recent automatic weight,
end log), `weight/HealthConnectWeights` (recent automatic weights read), `auto/AutoWeights.kt`,
`auto/AutoAndroid.kt` (notification actions, Kumoa, restore), `auto/AutoText.kt`,
`listen/WidgetState.kt`, `WeighInWidget.kt` and a ✓ drawable, `ui/ListenSection.kt` /
`listen/ListenStatus.kt` (Asetukset symbol), `ui/AppSnackbar.kt` and its callers,
`ui/FoodSearch.kt`, `ui/FoodViewModel.kt`, `ui/FoodAddDialog.kt` / `FoodAddActivity.kt` (Valmis),
`res/values/strings.xml` (texts, help), `ui/MainViewModel.kt` (Historia delete remembers as
deleted). `.claude/CLAUDE.md` log lines, `openspec/context/product.md` (weigh-in decisions),
`roadmap.md`, `gradle.properties` (1.64).
