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

Decided with the user (2026-11-14): food and steps side by side at half width, one screen; no step
goal; Health Connect's merged daily total (no origin filter); weight line under the bars; the
permission asked only on a tap of the card or tab.

Untestable scenarios are covered by design: "two sources, one total" by using only the aggregate
with no origin filter; "never written" by a manifest/lint test that `WRITE_STEPS` is absent; "fits
without scrolling" by one Robolectric layout smoke test at about 360 dp width.

1. **Daily totals from `aggregateGroupByPeriod`, not raw records.** One request over the period with
   a one-day `Period` and `LocalDateTime` bounds (an `Instant` range throws) returns one total per
   day. Health Connect dedupes steps across writers by the user's priority list in that call, so
   Garmin Connect and Samsung Health never add up. Alternative (read `StepsRecord`s and sum) rejected:
   double counts and pages a lot. No `DataOrigin` filter: a filter would drop the phone's own steps
   (origin `android` before June 2026, a device-specific synthetic package name after) or Garmin,
   and the priority is the user's to set. Sources: knowledge.md.

2. **A missing day is a gap, not zero.** The bucket list is sparse (a day with no data is absent);
   it is shown as a gap and left out of averages and day counts, as Energia does for unlogged days.
   Today is shown but left out of the averages, the 7-day line and the weekly bar averages (it is
   not over), yet counted as a day with steps in "x / y päivää" (as Energia). A day is the local
   day as Health Connect's daily aggregate reports it; the request bounds are the phone's current
   local dates (travel across zones may shift a bucket, accepted). Alternative (zero for missing)
   rejected: a day the watch was not synced or not worn would drag the average down. Garmin is known
   to leave hourly gaps (knowledge.md), so the numbers can read low; Ohje says to check the source
   in Health Connect instead of the app guessing.

3. **Pure series in `steps/`, Android glue in `HealthConnectSteps`.** `StepsSeries.of(days, period,
   today)` mirrors `EnergySeries` (bars, weekly only when the period is over 90 days, so 3 kk and the
   card's 90 pv stay daily, using the average per day with steps,
   the 7-day average line with the same at-least-3 rule, stats, newest-first list); `HomeSteps` mirrors
   `HomeFood`. Both pure and unit-tested with made-up numbers. `HealthConnectSteps` holds the
   permission set (`READ_STEPS` only), `granted()`, `daily(from, to)` and `firstDay()`. Alternative
   (generalise `EnergySeries` for both) rejected for now: the treat share, the energy rounding and
   the Herkku tone make the shared part small; copy the shape, share only `Period` and the axis code
   that already is generic.

4. **Kaikki starts at the first day with steps, or today.** An ascending `readRecords` (one
   `StepsRecord`) returns only readable records, so it gives the first readable steps day. The daily
   read start is `max(period start, that day)`; one aggregate request then covers start to today.
   History notice: shown when `HistoryAccess` says the history permission is not granted and the
   period starts more than 30 days back (as for weights); nothing when history reading is
   unsupported. Nothing new asks for the permission. Whether the aggregate call throws or clamps
   past the readable limit is not known without a device: a task checks it on the device and logs
   the result, with a fallback (read in chunks starting at the first readable day, or catch the
   failure and show the readable part). Reading is foreground-only; Health Connect's foreground
   quota is ample for one or two aggregate calls per screen.

5. **State: `StepsViewModel` next to `FoodViewModel`.** `StepsAccess` (checking, unavailable, needs,
   denied, granted) and the loaded day list (null until read, `loadFailed`). It refreshes on every
   resume and on entering the home screen or the tab (steps arrive from another app, no signal exists),
   re-reading the whole shown range each time (late watch data); numbers do not update live while
   the screen stays open. It caches the daily list for the widest range asked and slices periods
   from it. A failed read replaces the numbers with a message only when nothing has been read yet;
   otherwise the last numbers stay with a quiet "ei päivitetty" line. The permission
   check mirrors food: the Health Connect client's `granted()` and an own launcher call asking only
   `READ_STEPS`. In NEEDS a tap asks; in DENIED a tap opens Askeleet with the reason and two buttons
   (ask again; open Health Connect's settings with `ACTION_MANAGE_HEALTH_PERMISSIONS`, already used
   in `MainActivity`). The in-memory `permissionAsked` resets after a restart, so DENIED reads as
   NEEDS again; accepted. Health Connect unavailable: card "Askeleet: ei saatavilla" without tap
   action, the tab the same text without a button. The permission
   result updates the state like `onPermissionResult` of food. The card's period is `stepsDays` in
   `PeriodPrefs` (`HomeSummary.homeDays` accepts 7, 30, 90), separate from weight and food.

6. **Askeleet is a fourth page after Energia.** `Quantity` stays weight and BMI (it computes from
   weights and the owner profile); Energia already shows the pattern of an extra page, so Askeleet
   becomes `STEPS_PAGE = ENERGY_PAGE + 1` with its own reason view (`StepsReason`, like
   `EnergyReason`) and day rows. Swiping and tab state extend by one page. The weight line under the
   bars is the existing Energia lower chart reused with the same time axis, clipped to the steps
   axis; with no weights in range the lower area stays empty as in Energia (a small mirror of Energia,
   not the combined chart, which stays a backlog item). Bars use a single tone (the primary used for
   Energia's base food), no treat tone.

7. **Half width for both cards.** The row becomes two equal halves, the food card on the left, the
   steps card on the right, both the same height (the row's height is the taller card's). The food
   card keeps its parts; at about 170 dp the average line wraps or shortens and "+ Annos" stays a full
   outlined button under the bars. The steps card is the food card without the button, with a
   quiet period label and average. Both cards fill the row height (the taller sets it); the
   steps card's free space goes under its bars so the bottoms line up. In the food card at half width
   the period label and the average share one row (the average may shorten to "7 800/pv"), with
   "+ Annos" on its own row under it. Because the card has no button its tap target is the whole card
   except the period label (parity with food, whose taps are split because it has a quick action).
   Alternative (steps as a full-width third row) rejected: three stacked cards plus the notice do not
   fit one phone screen (home-compact's goal). Texts: title "Askeleet tänään", number with a thin
   space ("6 420"), "keskim. 7 800 askelta/pv" (half width: "keskim. 7 800/pv"). No data at all: title
   kept, "ei tietoa" in the number slot, "ei vielä keskiarvoa"; the tab with no step days shows one
   line about connecting Garmin Connect to Health Connect instead of an empty chart.

8. **Permission only from steps.** `READ_STEPS` goes in the manifest and the permission contract
   next to the nutrition ones; it is requested from the card (needs) or the tab button, never from the
   weigh-in or at start-up (mirrors food-log "Food in Health Connect"). Granting it also makes
   Health Connect count the phone's own steps from Android 14; with a watch the two are merged by the
   user's priority (decided: Health Connect's merged total), and when the phone ranks above the Garmin
   the numbers read like the phone's; the phone's own count starts only from the grant. The card
   cannot tell the user which source won, so Ohje explains how to rank the watch first in Health
   Connect's priority.

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
- [Half-width food card gets cramped] -> label and average share a row, + Annos on its own row; one
  Robolectric layout smoke test at about 360 dp width checks both cards and the button.
- [Time zone of travel changes a day's bucket] -> accept: request bounds are the phone's current local dates.
- [Aggregate over Kaikki is large] -> one request, buckets are small; the first load runs off the main
  thread and the tab shows a spinner like Energia.
