# Proposal

## Why

The 2026-11-17 audit found the app tidy but without character: every surface the same pale
container, every number the same weight, every chart one flat theme colour, and colours that change
with the wallpaper. The user chose direction **AC** (`openspec/design/visual-identity/`): A's
borderless 24 dp cards, tinted icon tiles and one colour per quantity on C's graphite canvas with
C's glowing weight line and today highlight. The phone's own colours (Material You) stay as a
choice in Asetukset.

## What Changes

- **New colour choice** in Asetukset → Ulkoasu: "Värit": **Terveyslappu** (the new brand palette,
  default) or **Puhelimen värit** (Material You, as today). It combines with the existing
  Järjestelmä / Vaalea / Tumma choice.
- **Terveyslappu palette**: a mint brand colour; dark theme on graphite (background `#0E1013`,
  cards `#16191E`), light theme on a calm near-white grey. One fixed colour per quantity in both
  palettes: paino (and BMI) mint, Syöty amber, Askeleet blue, Kulutus coral; Herkku a lighter amber.
- **Cards**: 24 dp corners, no outline, a round tinted tile with the quantity's icon before the
  card title; titles shortened to the quantity's name ("Syöty", "Askeleet", "Kulutus") so they never
  truncate, with "tänään" moved under the number; big numbers larger and semibold with tabular
  figures and a smaller muted unit; the period label is a light outlined chip; changes and their arrows in neutral text tones (never
  mint or green). The weight card keeps the period's change under the chart (no pill by the number).
- **Charts** (Historia, front page cards, own charts): data in the quantity's colour; the weight line
  thicker with a soft glow and a gradient fill under it; bars with rounded tops; today's bar and the
  latest weight point highlighted; gridlines fainter.
- **Historia header**: the tabs first, then the periods as one segmented row that fits 360 dp; the
  statistics as one big tinted "hero" figure (Paino and BMI: Muutos; Syöty, Askeleet, Kulutus:
  Keskiarvo) and the remaining figures in one compact row.
- **Update:** existing users get the Terveyslappu palette after the update (their Teema choice stays).
- **Row actions**: the red trash (and pencil) buttons at the end of list rows are replaced by one
  ⋮ button that opens a menu (Muokkaa, Poista in red). **BREAKING** for the look of every list
  (Historia, Ruoka, the add sheet's day list, Omat ruuat, the AI preview); delete still happens at
  once with Kumoa.
- **Screenshot harness** for the developer: a Robolectric screenshot test of the main screens in
  both themes and both palettes, with made-up data, run only on request (not in `./gradlew test`
  or CI).

Not in this change: launcher icon and widget look, onboarding, edit-mode polish, motion (count-up,
check-mark morph); these stay in the audit's list.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `app-appearance`: theme choice gains the Värit choice; new requirements for the brand palette,
  quantity colours, card look, number style and chart look; delete controls and list rows move to
  the ⋮ menu.
- `home-screen`: card titles and "tänään" placement, weight card's change pill, period label look.
- `weight-chart`: tab and period header order and look, statistics layout (hero + row), own charts'
  Oletus colour = the quantity colour, Kulutus's colour coral.
- `food-log`: item row buttons become the ⋮ menu (sheet, Ruoka, Omat ruuat).
- `ui-terms` is unchanged (new words go to the glossary in product.md).

## Impact

- App screens only: theme (`ui/Theme.kt`, `ui/Appearance.kt`), Asetukset → Ulkoasu, front page cards
  (`ui/HomeScreen.kt`, `ui/CardChart.kt`, `ui/ChartCard.kt`), Historia (`ui/HistoryScreen.kt`,
  `ui/PeriodPanel.kt`, `ui/HistoryChartView.kt`, `ui/ChartPalette.kt`, `ui/BurnPage.kt`,
  `ui/StepsPage.kt`, `ui/EnergyPage.kt`), rows (`ui/FoodScreen.kt`, `ui/FoodSheet.kt`,
  `ui/ProductsPage.kt`, `ui/AiImportPreview.kt`, `ui/ListGroupRows.kt`).
- No change to Health Connect, the scale, the widgets, backups or the data file.
- New dependencies: MaterialKolor (the brand colour scheme from one seed) and Roborazzi (test only,
  screenshots).
- Build/CI: the screenshot test is excluded from the normal test task; CI is unchanged.
