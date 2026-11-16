Rule: the treat tone is `MaterialTheme.colorScheme.tertiary` at full strength (the Energia bars' `ToneRef.TERTIARY`; never the bars' lighter alpha tone). Only the treat text changes colour; its separators, the other parts of the line, size and weight stay as they are. No contentDescription is added. Empty and gated states are unchanged (the line is not shown there).

## 1. Treat text colour

- [x] 1.1 Summary line, two call sites: the Ruoka header (`daySumText`, `ui/FoodScreen.kt:240`, titleMedium, onSurface) and the add sheet (`daySummaryText`, drawn at `ui/FoodSheet.kt:359–366`, onSurfaceVariant). Pull out a non-composable builder from `daySummaryParts` (`ui/FoodScreen.kt:138–153`), e.g. `summaryLine(parts…, treatColor: Color): AnnotatedString`, that puts one `SpanStyle(color = treatColor)` over the treat part's text only (" · " outside the span); no treat → no span. Both functions return the `AnnotatedString`; both call sites keep their present base colour.
- [x] 1.2 Home food card (`ui/HomeScreen.kt:170–176`, shared by every card size): the `food_day_treats` Text in `colors.tertiary` instead of `onSurfaceVariant`.
- [x] 1.3 Energia list day row (`ui/EnergyPage.kt:122`): `tertiary` instead of `onSurfaceVariant`.
- [x] 1.4 Energia statistics (`ui/EnergyPage.kt:98`, tile text at `:108`): the Herkut value in `tertiary` only when `averageTreat > 0`; "0 kcal/pv" and "–" keep the present onSurface. Label unchanged.
- [x] 1.5 Energia group rows (`ui/HistoryScreen.kt:426–431`, `group_avg_treats`): add `extraColor: Color = Color.Unspecified` to `DayGroupRowItem` (`ui/ListGroupRows.kt:62`), falling back to `onSurfaceVariant` inside (`:83`); only the Energia call site passes `tertiary`, so Askeleet and Kulutus ("aktiivinen ka") stay muted.
- Not changed: the Historia marker card's "herkut" row and the chart legends (their tertiary dot/swatch already links them), the herkku sign chip, own charts, widgets.

## 2. Finish

- [x] 2.1 JVM test of the builder from 1.1: the plain text is unchanged and exactly one span with the given colour covers the treat substring; no treat → no span. Existing `foodSum`/`sheetSummary` tag tests stay green. Run `./gradlew test lint assembleDebug`.
- [x] 2.2 Bump `appVersion` to 1.49.
