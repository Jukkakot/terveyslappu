# Design

## Context

- The theme is `ui/Theme.kt` → `TerveysTheme`: Material 3 with `dynamicLight/DarkColorScheme` only;
  `ThemeMode` (Järjestelmä/Vaalea/Tumma) in `ui/Appearance.kt`, prefs file `appearance`, key
  `theme`. Several places pick tones by `surface.luminance()` (`checkColors`, `paletteColour`).
- Chart colours: Historia tabs and cards take `primary`/`tertiary` from the scheme; own charts use
  `ui/ChartPalette` (eight fixed colours with light/dark shades, `SeriesColour`, Kulutus = ORANSSI)
  and `ChartPaletteTest` checks 3:1 against fixed surfaces.
- Charts are MPAndroidChart (`ui/HistoryChartView.kt`: `bind` configures a `HistoryCombinedChart`
  per `PanelSpec`, card mode via `PanelStyle.card`). Bars are `BarDataSet`s (stacked for treats and
  lepo/aktiivinen); lines `LineDataSet`s; `BandYAxisRenderer` already subclasses a renderer.
- Historia's header: `PeriodChips` (`ui/PeriodPanel.kt`) above the tab row (`ui/HistoryScreen.kt`);
  statistics in `StatsTiles` and the Syöty/Askeleet/Kulutus pages' own tiles.
- Rows with pencil/trash: `HistoryScreen` (weight rows, questioned rows), `FoodScreen`, `FoodSheet`
  (day list, typing rows), `ProductsPage`, `AiImportPreview`.
- Test fixtures for screens exist (`HomeFixtures`, `ChartFixtures`, `FakeFoodRecords`); the audit's
  screenshot harness was not kept.
- Mockups: `openspec/design/visual-identity/AC-*.html` (tokens in the CSS); light AC was not drawn,
  `A-*-light.html` is the light reference. **The specs win over the mockups** wherever they differ
  (texts such as "keskiarvo 944 /pv", the weight card's change pill, mint today bars, "Kuukaudessa").
- Change colours today: `ui/ChangeLabel.kt` `changeColor()` gives a loss `primary` and a gain
  `tertiary`; with a mint primary that would read "loss = green", so it becomes neutral.
- `RowActions` already exists in `ui/FoodScreen.kt` (different signature): replaced by the shared
  `RowMenu` below.

## Goals / Non-Goals

**Goals:**
- One place for every colour decision: the scheme (brand or dynamic) plus a small `QuantityColours`
  set that the whole UI and all charts read.
- The AC look on the front page, Historia and rows with the least custom drawing, on top of Material 3
  and MPAndroidChart.
- A screenshot harness the developer (and the implementer) can run to compare with the mockups.

**Non-Goals:**
- Launcher icon, widgets, onboarding, motion, edit-mode polish, Ohje rewrite.
- New statistics or values; only their layout changes.
- Fonts other than the system default.

## Decisions

1. **Brand scheme from a seed with MaterialKolor** (`com.materialkolor:material-kolor`, Apache-2.0,
   maintained, Compose-native port of material-color-utilities): `dynamicColorScheme(seedColor =
   #2E7D6B, isDark, style = TonalSpot)` gives a full, contrast-checked M3 scheme for both themes.
   Then override the dark neutrals with the AC graphite (`background`/`surface` `#0E1013`,
   `surfaceContainerLow`/cards `#16191E`, `surfaceContainer`/raised `#1E2229`,
   `surfaceContainerHigh` `#272C34`) and the dark `primary` with the mockup mint `#4DF0B8`
   (`onPrimary` dark). Light: the seed's light scheme with `background` `#F4F6F5`, cards (`surfaceContainerLow`)
`#FFFFFF`, `surfaceContainer` `#EEF1F0`, `surfaceContainerHigh` `#E3E8E6`. Puhelimen värit: the
dynamic scheme as today, cards on `surfaceContainer` (one step above `background`). *Alternative:* hand-written 30 × 2 tokens; rejected (libraries first, contrast not
   checked).
2. **`ColourChoice` next to `ThemeMode`**: `enum ColourChoice { BRAND, PHONE }`, same prefs file,
   key `colours`, default BRAND. `TerveysTheme` picks brand or dynamic scheme. Ulkoasu sub-screen
   gets a second chip group "Värit" under "Teema"; `SettingsSummary` row reads "<teema> ·
   <värit>".
3. **`QuantityColours`** (new, `ui/QuantityColours.kt`): a pure object with light/dark shades for
   paino, syöty, herkku, askeleet, kulutus, lepo, plus a `@Composable quantityColours()` that picks
   the shade by the theme's darkness (one helper `isDarkTheme()` replacing the scattered
   `luminance()` checks). Starting values from the mockups, tuned to ≥ 3:1 by the test:
   - dark: paino `#4DF0B8`, syöty `#F5C16A`, herkku `#F6E3BF`-ish lighter amber, askeleet
     `#9CC3FA`, kulutus `#FF9A85`, lepo lighter coral;
   - light: paino `#1F6B5A`, syöty `#96590A`, herkku `#D48A12`-ish, askeleet `#3D6FC0`, kulutus
     `#B5442E`, lepo lighter coral.
   The test checks each full shade ≥ 3:1 against the brand card surfaces (`#16191E`/`#FFFFFF`) and
the reference surfaces in `ChartPaletteTest` (`LIGHT_SURFACE`/`DARK_SURFACE`), the 60 % shades of
main parts ≥ 2:1; the final herkku and lepo shades are whatever passes "told apart" (≥ 1.5:1
against their main part). Own charts' Oletus maps to these;
   `ChartPalette.BURN` goes away in favour of kulutus.
4. **Chart look in `HistoryChartView.bind`** (one place, so Historia, cards and own charts follow):
   - Glow: an extra `LineDataSet` with the same entries under the paino/BMI line, width 6 dp, colour
  at 25 % alpha, no circles, not highlightable, not in the legend; dark theme only. Glow and the
  ring set go into their own list after `lines`, so the indices `seriesSets`/`where` use for
  picking stay as they are (today's code counts `averages + trends + lines`); add a test that a tap
  still picks the weight.
   - Fill: `setDrawFilled(true)` + `fillDrawable` = vertical `GradientDrawable` from the colour at
  28 % (dark) / 18 % (light) to transparent. The drawable's bounds are the content rect, so the
  fade runs from the plot's top, not from the line: accepted. Paino only (no BMI fill over the
  bands), not on trend or average lines.
   - Latest point ring: the existing circle settings on the last entry via a one-entry data set
     (radius 5 dp, hole in the card colour).
   - Rounded bars: `RoundedBarChartRenderer` subclassing the fork's `BarChartRenderer` (the project
  uses a Kotlin fork with `Entry<…>` generics: check the real signatures first), overriding
  `drawDataSet` to draw rounded-top `Path` rects (radius `min(4 dp, width/2)`); in a stacked entry
  only the topmost **non-zero** part is rounded. `createRenderers` reruns on every `setData`, so
  set a `CombinedChartRenderer` subclass as `chart.renderer` before data, whose
  `createRenderers` puts the rounded renderer in place of the bar one. The highlight overlay may
  stay square.
   - Today emphasis: past daily bars at 60 % alpha, today's entry in full colour, via per-part
  colours (`setColors(list)`). The colour index is cumulative over drawn parts (buffer position /
  4), and entries have different part counts (`p.parts ?: listOf(p.y)`: a Kulutus total-only or
  active-only day has one part), so build the list by walking the entries' parts in order. A
  pure builder function with a unit test covers mixed days.
   - Gridlines: `gridColor` = onSurface at 8 %; x gridlines off (already).
5. **Historia header**: tab row first (`ScrollableTabRow`, `edgePadding` 8 dp, tab min width 0 and
   12 dp horizontal padding so the five fixed tabs fit 360 dp); under it `PeriodChips` becomes a
   `SingleChoiceSegmentedButtonRow`-like custom row (M3's segmented buttons have a check icon and
   outline that do not fit six at 360 dp), drawn as one rounded `surfaceContainer` track with six
   equal text segments; the chosen one has a `surfaceContainerHigh` pill and `primary` text.
6. **Statistics**: one `HeroStat` composable (tinted card: quantity colour at 14 % dark / 10 % light
   over the card) + one `StatRow` (equal-weight columns with `VerticalDivider`, `FlowRow` wrap at
   > 4). Each tab maps its existing values to hero + row; no value logic changes.
7. **Row ⋮ menu**: one shared `RowMenu(name, onEdit?, onDelete?, extra)` composable (replacing
   `FoodScreen`'s `RowActions`) =
   `IconButton(MoreVert)` + `DropdownMenu`; Poista item with `error` colour text and icon. Replaces
   every pencil/trash pair. A row without actions reserves no space; values align to the row end.
8. **Big numbers**: a `TextStyle` extension in `ui/Format.kt`/theme: `displayMedium` size on cards
   (`displaySmall` on small cards), `FontWeight.SemiBold`, `fontFeatureSettings = "tnum"`,
   `letterSpacing` −1 sp; units `titleMedium` regular in `onSurfaceVariant`. Auto-shrink by
   `TextAutoSize` (Compose 1.8+ `BasicText(autoSize = …)`) when the number with unit does not fit.
9. **Screenshot harness with Roborazzi** (`io.github.takahirom.roborazzi`, Apache-2.0, maintained,
   Robolectric-native): test class `ScreenshotTour` under `app/src/test/.../screens/`, tagged so
   the normal `test` task excludes it: `tasks.withType<Test>().configureEach { if
   (!providers.gradleProperty("screenshots").isPresent) exclude("**/screens/**") }`. Apply the Roborazzi plugin only for its record task and check that it adds no verify task to
   `check`/`lint` and that `./gradlew test lint assembleDebug` runs no screenshot. Run:
   `./gradlew testDebugUnitTest -Pscreenshots -Proborazzi.test.record=true --tests '*ScreenshotTour*'`;
   images land in `app/build/outputs/roborazzi/` (never committed: made-up data, but build output).
   It renders, at 360 × 780 dp: front page, Historia Paino/Syöty/Askeleet/Kulutus, Ruoka, Asetukset →
   Ulkoasu, in Tumma and Vaalea × Terveyslappu and Puhelimen värit (Robolectric's dynamic scheme is
   the AOSP default seed, which is enough to see the switch), with the existing fixtures' made-up data.
   The audit's known gaps (bottom sheet window, text-field dialogs) stay out.

## Risks / Trade-offs

- **Per-entry colours on stacked bars** must follow MPAndroidChart's colour indexing exactly or the
  treat/lepo parts swap tones. → A unit test on the colour list builder.
- **Glow as a second data set** could be picked by taps or the crosshair. → Mark it
  non-highlightable and exclude it from picking (the picker already filters by data set role).
- **Graphite in dark vs. Samsung's own dark grey**: the app will look different from other apps; it
  is the point of the change, and Puhelimen värit stays as the way back.
- **Every updating user's look changes** (default Terveyslappu): intended; Puhelimen värit restores
  the old colours.
- **⋮ adds a tap to delete** in the add sheet and Ruoka, where deletes are common. Accepted for one
  pattern everywhere (the user can still open the editor with a row tap and delete there).
- **MaterialKolor version** must be one built against the project's Compose BOM; pick the newest
  that compiles with it.
- **Five tabs at 360 dp with 130 % text**: may not fit; then the row scrolls (still the first row)
  and the test only asserts Paino…Askeleet visible.
