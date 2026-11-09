# Proposal

## Why

The manual weight form takes the bottom third of the main screen although the user will hardly
ever use it (user 2026-11-09): weighing happens with the scale. The screen also looks like a plain
list of default widgets; the user wants it modern, clean and pleasant, in line with how common
weight and health apps present the same data, and a usual light/dark theme choice.

## What Changes

- The manual form leaves the main screen. Menu ⋮ gets **Lisää paino käsin**, which opens a dialog
  with the same fields and rules as today (weight, date, time, Tallenna).
- Bottom of the main screen: only the primary action **Punnitse vaa'alla** (one filled button,
  thumb reach).
- Menu ⋮ gets **Teema**: Järjestelmä (default), Vaalea, Tumma. Colours stay the phone's own
  (Material You); the choice is remembered.
- A design pass on the main screen (mockups first, 2-3 alternatives): a summary of the latest
  weight and its change on top, then period chips, chart, stats and the list, with consistent
  spacing, type scale and surface tones in both themes. Menu items get icons and are grouped
  (daily, data, backup, about).
- Nothing changes in Health Connect, the scale, the widget or the tile.

## Capabilities

### New Capabilities
- `app-appearance`: theme choice (system, light, dark), remembered.

### Modified Capabilities
- `weight-entry`: manual entry moves into a dialog opened from the menu.
- `weight-chart`: a latest-weight summary at the top of the main screen.

## Impact

App screens only: main screen, menu, a new manual-entry dialog, theme setting (local preference).
No Health Connect, scale, widget or build/CI changes.
