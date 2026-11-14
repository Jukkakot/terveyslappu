# Proposal: settings-list

## Why

Asetukset has grown into one long scrolling page (theme, owner details, data, backup, automatic
weigh-in, support), so finding one setting means scrolling past all the others. A short list of
rows with their current state, each opening its own sub-screen, is calmer and shows at a glance
whether something needs attention. Split out of food-log (user 2026-11-14), which only adds its
own row later.

## What Changes

- Asetukset becomes a list of five rows, each with an icon, a name, one state line and a chevron:
  Omat tiedot, Automaattinen punnitus, Tiedot ja varmuuskopio, Ulkoasu, Tuki. Each row opens a
  sub-screen holding today's section unchanged.
- A section's warning (backup failing or late, weighings waiting to be saved, listening blocked)
  shows on its row in the warning tint.
- Links that open Asetukset at a section (BMI vaatii pituuden on the home screen and in Historia,
  the backup notice) open that sub-screen directly; back returns to where it was opened from.
- Ohje leaves Asetukset: a help (?) icon in the home screen's top bar, next to the gear, opens it;
  back from Ohje returns to the home screen. Tuki keeps Jaa loki, Kopioi loki and the version.
- Version 1.23.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `home-screen`: Asetukset becomes a list of sub-screens; the home top bar gains the Ohje button.
- `help-page`: Ohje opens from the home screen's top bar, back returns there.

## Impact

- App screens: Asetukset (main list and five sub-screens), home top bar, Ohje back target.
  `Screen.SETTINGS` gains a sub-screen value and an origin.
- Health Connect, scale, widget, tile, background weigh-in, data file: untouched (the sections'
  contents and behaviour stay as they are). Build/CI: no new dependency.
- food-log: its Asetukset regrouping moves here; it keeps the Ruokakirjaus row and sub-screen.
