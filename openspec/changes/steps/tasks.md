## 1. Steps rules (pure, `steps/`)

- [ ] 1.1 `StepsDay` and `StepsSeries.of`: bars (daily, weekly from 90 days with the average per day with steps), 7-day average line (at least 3 days in the window, daily mode only, today left out), statistics (average per day with steps without today, days with steps / period days), newest-first list; gaps for missing days
- [ ] 1.2 `HomeSteps.of`: today's steps, bars of the last 7, 30 or 90 days, first and last day, average without today, "no data today" flag
- [ ] 1.3 Unit tests for 1.1 and 1.2 with made-up numbers (gap week, weekly bars, window rule, today excluded, empty input)

## 2. Health Connect

- [ ] 2.1 `READ_STEPS` in the manifest; `HealthConnectSteps` with `granted()`, `daily(from, to)` (one-day period aggregate, `LocalDateTime` bounds, no origin filter) and `firstDay()` (one ascending record); clamp the start when the history permission is missing (reuse `HistoryAccess`)
- [ ] 2.2 `StepsViewModel`: `StepsAccess` states, refresh on resume and on entering the home screen or the tab, daily list cache sliced per period, permission request and result; singleton wiring next to the food one
- [ ] 2.3 Log events `steps.read ok|failed`, `hc.permission.steps request|granted|denied`, `home.period card=steps` (counts and timings, never step counts); add them to the event list in `.claude/CLAUDE.md`
- [ ] 2.4 Tests: access state mapping (needs, denied, granted, unavailable) as a pure rule; a read failure leaves `loadFailed` and a log line

## 3. Front page

- [ ] 3.1 `stepsDays` in `PeriodPrefs` and `MainUiState`, `onStepsPeriod` action (7 by default, separate from weight and food)
- [ ] 3.2 Steps card in the right half of the row: title, today's number, bars, first/last day, average, period menu, "anna lupa" and "ei tietoa" states; tap opens Historia on Askeleet
- [ ] 3.3 Food card at half width: both cards equal height, parts wrap, + Annos stays visible and tappable
- [ ] 3.4 Smoke tests: the steps card shows today's steps and opens Askeleet; the food card and + Annos are present beside it; no-permission text

## 4. Historia: Askeleet tab

- [ ] 4.1 Fourth tab and pager page after Energia (swipe and tab state, `historyOnSteps` for opening from the card); reason view with the grant button
- [ ] 4.2 Askeleet page: bars, average line, weight line under on the same axis (reuse Energia's), point details on tap, statistics, day list; the history notice for periods beyond the readable range
- [ ] 4.3 Smoke tests: tab shows bars and the day list; swipe from Energia reaches it; no-permission reason

## 5. Help, version, context

- [ ] 5.1 Ohje section on steps (source, read only, app priority and Garmin sync, permission from the card); help smoke test still green
- [ ] 5.2 `./gradlew test lint assembleDebug`
- [ ] 5.3 Bump MINOR in `gradle.properties` (`appVersion`)
- [ ] 5.4 Update context files: product.md (steps as built, decisions: Health Connect merged total, no data file, no goal), roadmap.md (steps done, combined chart stays open), `.claude/CLAUDE.md` map (`steps/` package, events), questions.md if any open question remains; mark the roadmap items done
