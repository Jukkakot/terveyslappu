# Design

## Context

Main screen today (`ui/MainScreen.kt`): top bar with ⋮ (text glyph), optional backup card, a
LazyColumn (period chips, chart, stats row, list rows with a delete icon), and a bottom surface with
Punnitse vaa'alla plus the whole manual form. Theme: Material 3 dynamic colours following the system
(`ui/Theme.kt`). The weigh-in dialog is a separate activity using the same theme.

## Goals / Non-Goals

**Goals:** a simple home screen; everything else one tap away on Historia or Asetukset; theme choice;
a calm, consistent look in both themes.
**Non-Goals:** new data or statistics, goal weight (backlog), widget/tile look, bottom navigation
(mockup C rejected 2026-11-09).

## Decisions

1. **Three screens in `MainActivity`, no navigation library:** a `Screen` state (Home, Historia,
   Asetukset) in `MainViewModel`, `BackHandler` returns to Home. Three fixed screens do not need
   Navigation Compose; state survives rotation through the view model.
2. **Home = mockup B** (canvas https://claude.ai/artifact/8Xy4472ukqBcVeMko9k3L7, `KorttiB`): card
   (latest, time, change chip with an arrow icon, 30-day sparkline drawn with a Compose `Canvas`, no
   axes), three latest rows, filled Punnitse vaa'alla at the bottom, gear button top right. The
   sparkline uses the last 30 days of own weights whatever period Historia has.
3. **Historia** keeps today's chips, Vico chart, stats and list; stats shown as compact tiles; add
   button (+, "Lisää paino käsin") in the top bar opens the manual-entry dialog, reusing today's form
   state and validation in `MainViewModel`.
4. **Asetukset** is a plain scrolling list of grouped sections (mockup `Asetukset`); the theme is a
   three-way segmented button. Backup actions move here from the menu and the card.
5. **Theme preference** in SharedPreferences (`appearance`), exposed as a flow read by `TerveysTheme`
   so a change applies at once; `WeighInActivity` reads the same value. Dynamic colours kept (ui rule:
   platform base theme first; the phone's palette is the project's theme).
6. **Icons:** Material Symbols vector drawables in `res/drawable` (no icon library dependency).

## Risks / Trade-offs

- Import, export and log sharing take one more tap (via Asetukset) → rare actions, accepted.
- The system splash still follows the system theme for a moment on start (acceptable).
