## 1. Steps rules (pure, `steps/`)

- [x] 1.1 `StepsDay` and `StepsSeries.of`: bars (daily, weekly only over 90 days, so 3 kk stays daily, with the average per day with steps without today), 7-day average line (at least 3 days in the window, daily mode only, today left out), statistics (average per day with steps without today, days with steps / period days with today counted as a day with steps), newest-first list; gaps for missing days
- [x] 1.2 `HomeSteps.of`: today's steps, bars of the last 7, 30 or 90 days, first and last day, average without today, "no data today" flag
- [x] 1.3 Unit tests for 1.1 and 1.2 with made-up numbers (gap week, weekly bars, window rule, today excluded, empty input)

## 2. Health Connect

- [x] 2.1 `READ_STEPS` in the manifest; `HealthConnectSteps` with `granted()`, `daily(from, to)` (one-day period aggregate, `LocalDateTime` bounds, no origin filter) and `firstDay()` (one ascending record, readable records only); read start = max(period start, first readable day); request bounds = the phone's current local dates; permission check mirrors food (client `granted()`, own launcher asking only `READ_STEPS`); history notice when the history permission is not granted and the period starts more than 30 days back (reuse `HistoryAccess`; nothing when unsupported)
- [x] 2.1b Check on the device whether the aggregate call throws or clamps past the readable history limit and log it; fallback: chunked reads from the first readable day, or catch the failure and show the readable part
  - Done as (apply): reads go in chunks of at most 366 days; a failed read whose start is the first readable day is retried once from the next day; `steps.read ok ... fallback=none|later-start back=` (and `steps.read failed cause= retry=true` before a retry) tells on the device which happened.
- [x] 2.2 `StepsViewModel`: `StepsAccess` states, refresh on resume and on entering the home screen or the tab (re-read the whole shown range, no live updates), failed read: message only when nothing was read yet, else last numbers with a quiet "ei päivitetty" line, daily list cache sliced per period, permission request and result; singleton wiring next to the food one
- [ ] 2.3 Log events `steps.read ok|failed`, `hc.permission.steps request|granted|denied`, `home.period card=steps` (counts and timings, never step counts); add them to the event list in `.claude/CLAUDE.md`
- [x] 2.4 Tests: access state mapping (needs, denied, granted, unavailable) as a pure rule; a read failure leaves `loadFailed` and a log line; a manifest/lint test that `WRITE_STEPS` is absent

## 3. Front page

- [ ] 3.1 `stepsDays` in `PeriodPrefs` and `MainUiState`, `onStepsPeriod` action (7 by default, separate from weight and food)
- [ ] 3.2 Steps card in the right half of the row: title, today's number, bars, first/last day, average, period menu, "anna lupa", "ei saatavilla" (no tap action) and "ei tietoa" / "ei vielä keskiarvoa" states; texts "Askeleet tänään", "6 420" (thin space), "keskim. 7 800 askelta/pv" (half width "keskim. 7 800/pv"); tap anywhere but the period label opens Askeleet (in DENIED the tab shows the reason with ask-again and Health Connect settings buttons via `ACTION_MANAGE_HEALTH_PERMISSIONS`; in NEEDS a tap asks)
- [ ] 3.3 Food card at half width: period label and average on one row (average may shorten), + Annos on its own row under it; both cards fill the row height (taller sets it), the steps card's free space goes under its bars
- [ ] 3.4 Smoke tests: the steps card shows today's steps and opens Askeleet; the food card and + Annos are present beside it; no-permission text; one Robolectric layout smoke test at about 360 dp width (both cards and + Annos visible)

## 4. Historia: Askeleet tab

- [ ] 4.1 Fourth tab and pager page after Energia (swipe and tab state, `historyOnSteps` for opening from the card); reason view with the grant button
- [ ] 4.2 Askeleet page: bars, average line, weight line under on the same axis, clipped to the steps axis, empty when no weights (reuse Energia's); Kaikki from the first step day or today; one line about connecting Garmin Connect to Health Connect when the period has no step days, point details on tap, statistics, day list; the history notice for periods beyond the readable range
- [ ] 4.3 Smoke tests: tab shows bars and the day list; swipe from Energia reaches it; no-permission reason

## 5. Help, version, context

- [ ] 5.1 Ohje section on steps (source, read only, how to rank the watch first in Health Connect's priority, Garmin sync, the phone's own count starts only from the grant, permission from the card); help smoke test still green
- [ ] 5.2 `./gradlew test lint assembleDebug`
- [ ] 5.3 Bump MINOR in `gradle.properties` (`appVersion`)
- [ ] 5.4 Update context files: product.md (steps as built, decisions: Health Connect merged total, no data file, no goal), roadmap.md (steps done, combined chart stays open), `.claude/CLAUDE.md` map (`steps/` package, events), questions.md (steps questions answered, remove them); mark the roadmap items done
