# Tasks

## 1. Mockups

- [x] 1.1 Three home-screen alternatives plus Historia and Asetukset, light and dark, made-up data
  (canvas "Terveyslappu etusivu"). User chose B, manual entry only on Historia (2026-11-09).

## 2. Screens and navigation

- [ ] 2.1 Screen state Home/Historia/Asetukset in `MainViewModel`, back returns Home; Historia gets
  today's chips, chart, stats (as tiles) and list; the ⋮ menu goes. Verify: MainScreenTest smoke
  (card opens Historia, gear opens Asetukset, back returns).
- [ ] 2.2 Home per mockup B: latest card with change and 30-day sparkline, three latest rows, empty
  state, backup notice only on failure/stale, Punnitse vaa'alla. Verify: unit test for the home
  summary (latest, change, 30-day points, empty); smoke test for the card.
- [ ] 2.3 Manual entry dialog from Historia's + button; closes on save, stays with an error on
  failure; form gone from other screens. Verify: smoke (dialog opens, impossible weight disables
  Tallenna).
- [ ] 2.4 Asetukset: Ulkoasu, Painot (Tuo historia, Vie painot), Varmuuskopio (status and actions
  from the old card and menu), Tuki (Jaa loki, version). Verify: existing import/export/backup tests
  updated to open Asetukset.

## 3. Theme

- [ ] 3.1 Theme preference (Järjestelmä/Vaalea/Tumma, default system) applied at once to all
  screens and the weigh-in dialog, remembered. Verify: unit test for the preference mapping, one
  smoke test that picking Tumma changes the theme.

## 4. Look

- [ ] 4.1 Run the ui design pass in both themes at 360 px and 200 % text on all three screens;
  spacing/type from the M3 scale; icons as vector drawables. Verify: one screenshot per screen and
  theme compared with the mockups.

## 5. Context

- [ ] 5.1 product.md: theme decision (phone palette, system default), simple home with Historia and
  Asetukset, manual entry hidden on Historia; `.claude/CLAUDE.md` map and log events (menu → Asetukset);
  roadmap item; appVersion MINOR bump. Verify: `./gradlew test lint assembleDebug` passes.
