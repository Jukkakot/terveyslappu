# Design: steps

## Context

The app writes and reads weight and food in Health Connect; Historia has the tabs Paino, BMI
(`Quantity`) and Energia (a separate pager page after them, `EnergySeries`, `EnergyPage`); the front
page has the weight card and the food card (`HomeFood`, `FoodCard` in `ui/HomeScreen.kt`) whose row
was built for a steps card (home-overview, home-compact). Food shows the pattern to mirror: access
state (`FoodAccess`: checking, unavailable, needs, denied, granted), refresh on resume, a card with
own period (`foodDays` in `PeriodPrefs`), the permission asked from the feature itself. Steps differ
in one way: the data is not ours (read-only, written by Garmin Connect, Samsung Health and Health
Connect's own phone counter). Why: proposal.md; product.md "Food log: why" BR2. Facts about Health
Connect steps with sources: knowledge.md "Steps in Health Connect".

## Goals / Non-Goals

**Goals:** the right daily numbers without double counting; the steps card and tab look and behave
like their food twins; no new storage; a failure (no permission, no data) is explained, never
shown as zero.

**Non-Goals:** the combined chart (backlog), a step goal or target line, distance, floors, calories
burned, heart rate or workouts, writing steps, background reads or a steps widget/notification,
steps in the data file or backup, per-source breakdown (which app wrote what).

## Decisions

1. **Daily totals from `aggregateGroupByPeriod`, not raw records.** One request over the period with
   a one-day `Period` and `LocalDateTime` bounds (an `Instant` range throws) returns one total per
   day. Health Connect dedupes steps across writers by the user's priority list in that call, so
   Garmin Connect and Samsung Health never add up. Alternative (read `StepsRecord`s and sum) rejected:
   double counts and pages a lot. No `DataOrigin` filter: a filter would drop the phone's own steps
   (origin `android` before June 2026, a device-specific synthetic package name after) or Garmin,
   and the priority is the user's to set. Sources: knowledge.md.

2. **A missing day is a gap, not zero.** The bucket list is sparse (a day with no data is absent);
   it is shown as a gap and left out of averages and day counts, as Energia does for unlogged days.
   Today is shown but left out of the averages (it is not over). Alternative (zero for missing)
   rejected: a day the watch was not synced or not worn would drag the average down. Garmin is known
   to leave hourly gaps (knowledge.md), so the numbers can read low; Ohje says to check the source
   in Health Connect instead of the app guessing.

3. **Pure series in `steps/`, Android glue in `HealthConnectSteps`.** `StepsSeries.of(days, period,
   today)` mirrors `EnergySeries` (bars, weekly from 90 days using the average per day with steps,
   the 7-day average line with the same at-least-3 rule, stats, newest-first list); `HomeSteps` mirrors
   `HomeFood`. Both pure and unit-tested with made-up numbers. `HealthConnectSteps` holds the
   permission set (`READ_STEPS` only), `granted()`, `daily(from, to)` and `firstDay()`. Alternative
   (generalise `EnergySeries` for both) rejected for now: the treat share, the energy rounding and
   the Herkku tone make the shared part small; copy the shape, share only `Period` and the axis code
   that already is generic.

4. **Kaikki starts at the first day with steps.** One ascending `readRecords` of one `StepsRecord`
   finds the oldest record (cheap, one call); the daily aggregate then covers first day to today in
   one request (at most a few years of buckets). Without the history permission the start is clamped
   to what Health Connect lets the app read, and Historia shows the same history notice as for weights
   (`HistoryAccess` is reused, nothing new asks for it). Reading is foreground-only; Health Connect's
   foreground quota is ample for one or two aggregate calls per screen.

5. **State: `StepsViewModel` next to `FoodViewModel`.** `StepsAccess` (checking, unavailable, needs,
   denied, granted) and the loaded day list (null until read, `loadFailed`). It refreshes on every
   resume and on entering the home screen or the tab (steps arrive from another app, no signal exists),
   caches the daily list for the widest range asked and slices periods from it. The permission
   result updates the state like `onPermissionResult` of food. The card's period is `stepsDays` in
   `PeriodPrefs` (`HomeSummary.homeDays` accepts 7, 30, 90), separate from weight and food.

6. **Askeleet is a fourth page after Energia.** `Quantity` stays weight and BMI (it computes from
   weights and the owner profile); Energia already shows the pattern of an extra page, so Askeleet
   becomes `STEPS_PAGE = ENERGY_PAGE + 1` with its own reason view (`StepsReason`, like
   `EnergyReason`) and day rows. Swiping and tab state extend by one page. The weight line under the
   bars is the existing Energia lower chart reused with the same time axis (a small mirror of Energia,
   not the combined chart, which stays a backlog item). Bars use a single tone (the primary used for
   Energia's base food), no treat tone.

7. **Half width for both cards.** The row becomes two equal halves, the food card on the left, the
   steps card on the right, both the same height (the row's height is the taller card's). The food
   card keeps its parts; at about 170 dp the average line wraps or shortens and "+ Annos" stays a full
   outlined button under the bars. The steps card is the food card without the button, with a
   quiet period label and average. Because the card has no button its tap target is the whole card
   except the period label. Alternative (steps as a full-width third row) rejected as the default:
   three stacked cards plus the notice do not fit one phone screen (home-compact's goal); it stays
   as open question 1.

8. **Permission only from steps.** `READ_STEPS` goes in the manifest and the permission contract
   next to the nutrition ones; it is requested from the card (needs) or the tab button, never from the
   weigh-in or at start-up (mirrors food-log "Food in Health Connect"). Granting it also makes
   Health Connect count the phone's own steps from Android 14; with a watch the two are merged by the
   user's priority, and when the phone ranks above the Garmin the numbers read like the phone's.
   The card cannot tell the user which source won, so Ohje names the Health Connect setting (Sovellusten
   prioriteetti / data priority) to fix it.

9. **Not in the data file or backup.** The file holds what this app authors or keeps (weights it
   writes, foods, products). Steps are Garmin's and live in Health Connect and Garmin Connect; copying
   them into a file would make a stale second copy and a health-data export the user did not ask for
   (NFR-1, NFR-2 spirit). `data-file` states it so the question is closed. No change in the backup,
   no stored steps, nothing in app storage except the period choice.

10. **Log: counts and timings only.** `steps.read ok days= ms=`, `steps.read failed cause= ms=`,
    `hc.permission.steps request|granted|denied`, `home.period card=steps days=`, `chart.period` as
    today. Never a step count, as with food (counts and timings, never values). NFR-7 (log raw inputs
    for replay) is met by the aggregate being Health Connect's own number: there is nothing of ours
    to replay, so the raw day totals are not logged; a failing case is reproduced by the cause and
    the date range.

11. **Ohje and wording.** A short section "Askeleet": read from Health Connect, never written,
    where the numbers come from, what to do when they look wrong (priority of apps in Health Connect,
    Garmin sync). Finnish UI texts: "Askeleet tänään", "Askeleet: anna lupa", "ei tietoa",
    "askelta/pv", "päivää".

12. **Version:** MINOR bump in the last task (the user notices a new card, tab and permission).

## Risks / Trade-offs

- [Garmin undercount or no sync until the watch syncs] -> numbers can lag the watch; refreshed on
  every resume, Ohje explains; no action possible in the app.
- [Phone's own steps start counting once permission is given and may outrank Garmin] -> Ohje names
  the priority setting; if it bites in real use, a later change can show the source name per day (not
  in this change).
- [Half-width food card gets cramped] -> parts allowed to wrap, smoke test checks that the food card
  and its button are visible and tappable; open question 1 holds the fallback.
- [Time zone of travel changes a day's bucket] -> accept: days are the phone's current zone.
- [Aggregate over Kaikki is large] -> one request, buckets are small; the first load runs off the main
  thread and the tab shows a spinner like Energia.

## Open questions

1. **Layout of the front page row.** Recommended: food and steps cards side by side, half width each
   (the home-compact plan, fits one screen). Alternative: steps as a full-width third card
   (roomier, but the front page then scrolls on a 6-inch phone).
2. **Weight line under the Askeleet bars** (as on Energia). Recommended: yes, same as Energia so the
   three tabs behave alike. Alternative: bars only (simpler, leaves "side by side" to the later combined
   chart).
3. **Step goal.** Recommended: none (no goal line, no percent, no colour for "few" steps), matching the
   food log's no-pressure rule (BR5). Alternative: a goal in Asetukset shown as a line.
4. **Which steps count when the phone also counts.** Recommended: take Health Connect's merged total
   (the user's priority decides, Ohje explains how to put the watch first). Alternative: show only
   the watch's steps by filtering on Garmin Connect's package (exact for the user, breaks if the data
   comes from another app, and needs the package name to be right).
5. **Asking the permission.** Recommended: only when the user taps the steps card or tab (no
   prompt at start-up, none from a weigh-in). Alternative: ask together with the next food/weight
   permission prompt so there is one dialog.
