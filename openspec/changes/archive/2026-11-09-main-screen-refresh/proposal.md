# Proposal

## Why

The main screen shows too much information and text at once (user 2026-11-09), and the manual
weight form takes its bottom third although the user will hardly ever use it. The user wants a simple
home screen from which other places open when wanted, a modern and clean look in line with common
weight and health apps, and a usual light/dark theme choice in a settings screen.

## What Changes

- **Home screen** (mockup B, chosen 2026-11-09): one card with the latest weight, its time, the change
  from the previous weight and a small 30-day line; the three latest weights; Punnitse vaa'alla as the
  only filled action at the bottom; a settings button in the top bar. The ⋮ menu and the backup card
  leave the home screen; a notice appears only when the backup failed or is stale.
- **Historia** (opened from the card): period chips, chart, statistics and the weight list with
  delete and undo, as today; an add button in its top bar opens **Lisää paino käsin**, a dialog with
  today's form (weight, date, time, Tallenna). The form is nowhere else.
- **Asetukset** (opened from the settings button): Ulkoasu (Teema: Järjestelmä default, Vaalea,
  Tumma; colours stay the phone's own), Painot (Tuo historia, Vie painot), Varmuuskopio (state and
  actions), Tuki (Jaa loki, version).
- Consistent spacing, type scale and surface tones in both themes (mockup canvas "Terveyslappu
  etusivu").
- Nothing changes in Health Connect, the scale, the widget or the tile.

## Capabilities

### New Capabilities
- `home-screen`: the simple home screen, Historia and Asetukset screens and how they open.
- `app-appearance`: theme choice, remembered.

### Modified Capabilities
- `weight-entry`: manual entry becomes a dialog from Historia; the list lives on Historia; version in
  Asetukset.
- `weight-chart`: the chart lives on Historia.
- `weight-backup`: turn on, status and actions in Asetukset; home only shows a notice on trouble.
- `history-import`, `weight-export`, `app-log`: their entries move from the menu to Asetukset.

## Impact

App screens only: home, two new screens, a manual-entry dialog, theme preference (local). No Health
Connect, scale, widget or build/CI changes.
