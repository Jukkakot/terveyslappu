# Design

## Context

Main screen today (`ui/MainScreen.kt`): top bar with ⋮ (text glyph), optional backup card, a
LazyColumn (period chips, chart, stats row, list rows with a delete icon), and a bottom surface with
Punnitse vaa'alla plus the whole manual form. Theme: Material 3 dynamic colours following the
system (`ui/Theme.kt`). Weigh-in dialog is a separate activity using the same theme.

## Goals / Non-Goals

**Goals:** manual form out of sight but findable; one primary action; a calm, modern overview in
both themes; theme choice.
**Non-Goals:** new data or statistics, goal weight (backlog), widget/tile look, bottom navigation
(one screen is enough).

## Decisions

1. **Manual entry = dialog from the menu**, reusing today's form state in `MainViewModel` (same
   validation, pickers). A dialog, not a new screen: one short task, keyboard friendly.
2. **Theme preference** in SharedPreferences (`appearance`), read by `TerveysTheme` through a small
   flow so a change applies at once; `WeighInActivity` reads the same value. Dynamic colours kept
   (ui rule: platform base theme first; the phone's palette is the project's theme).
3. **Patterns borrowed (look not copied):** Withings Health Mate / Google Fit style overview: a large
   latest number with a small change line on top, period chips over a full-width line chart, compact
   stat tiles, then a dense dated list. Material 3 surface tones for depth, no shadows except the
   bottom action.
4. **Menu with icons, grouped by dividers:** Lisää paino käsin; Tuo historia, Vie painot; backup
   items; Teema; Jaa loki; version. Material Symbols drawables (no icon library dependency).
5. **Mockups before code:** 2-3 alternatives at 360×780 in light and dark with made-up data, as an
   HTML page; the user picks, then the spec scenarios are finalised.

## Risks / Trade-offs

- Users of the manual form lose one tap → accepted by the user (rarely used).
- Theme override in a Compose-only app is simple; the system splash/window background still follows
  the system for a moment on start (acceptable).

## Open Questions

- Which mockup alternative (decided with pictures before task 2).
- Where the theme choice sits: a submenu in ⋮ or a small dialog with three radio buttons.
