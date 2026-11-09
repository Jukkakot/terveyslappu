# Tasks

## 1. Mockups

- [ ] 1.1 2-3 clearly different main-screen alternatives (layout/density/visual approach) at
  360×780, light and dark, made-up data incl. empty state and 15+ rows; menu open state; manual
  entry dialog. User picks one; fold the choice into specs/design. Verify: user's choice recorded.

## 2. Manual entry and menu

- [ ] 2.1 Manual form into a dialog opened by menu Lisää paino käsin; bottom bar keeps only
  Punnitse vaa'alla; dialog closes on save, stays with an error on failure. Verify: MainScreenTest
  smoke (menu opens dialog, impossible weight disables Tallenna, main screen has no form).
- [ ] 2.2 Menu grouped with icons (proper ⋮ icon instead of the text glyph). Verify: existing menu
  tests pass.

## 3. Theme

- [ ] 3.1 Theme preference (Järjestelmä/Vaalea/Tumma, default system) applied at once to the main
  screen and the weigh-in dialog, remembered. Verify: unit test for the preference mapping, one
  smoke test that picking Tumma changes the theme.

## 4. Look

- [ ] 4.1 Implement the chosen alternative: latest-weight summary, chips, chart, stat tiles, list
  rows; spacing/type from the M3 scale; run the ui design pass in both themes at 360 px and 200 %
  text. Verify: smoke test for the summary (latest, change, empty state); one screenshot per theme
  compared with the mockup.

## 5. Context

- [ ] 5.1 product.md: theme decision (phone palette, system default) and the hidden manual entry;
  roadmap item; appVersion MINOR bump. Verify: `./gradlew test lint assembleDebug` passes.
