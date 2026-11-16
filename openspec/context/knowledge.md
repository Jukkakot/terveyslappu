# Knowledge

General knowledge this project relies on: principles and facts about the domain that do not
change with our code. Knowledge that holds in any project (charts, data files …) lives in the
global bank `~/.claude/knowledge.md` (claude-config); read its index too.
Our own choices live in `product.md`, facts about the code in the code. Read the section a task touches before researching it again; add what a new search taught, with
the source. No configuration, versions or other things that go stale.

**Never read this whole file.** Find the topic in the index, then read only that section
(`grep -n '^## '` gives the line numbers). Keep the index in step when adding a section.

## Index

| Section | What is there |
|---|---|
| Body weight data | daily fluctuation size and causes, trend vs single readings, rate per week |
| Derived body values | BMI and its limits, waist-to-height, BMR (Mifflin-St Jeor), body fat from impedance, Health Connect record types |
| Android | background work on Samsung, Health Connect from the background, widget updates, chart libraries |
| Motivation and feedback | self-monitoring, negative feedback, visible progress, trend smoothing, streaks, flat average reference |
| Steps in Health Connect | aggregate dedupe by app priority, gap filling, one-app filter, daily buckets, on-device steps (SPN), history limit, Garmin gaps, total calories estimate fill, Garmin kcal split |

Charts and Data files: see the global bank `~/.claude/knowledge.md`.

## Body weight data

- **Daily weight is noisy.** A healthy adult's weight moves about 0.5–2 kg from day to day, mostly
  water and glycogen (each gram of glycogen holds ~3 g of water), salt and gut contents, not fat.
  ±1 kg between consecutive days is common, ±0.5 kg very common. Fat changes show over weeks.
  ([NHS Grampian](https://nhsgrampian.org/service-hub/eating-disorder-mcn/patients--carers/anorexia---further-information/weight-fluctuation),
  [Everlab](https://www.everlab.com.au/post/how-much-does-weight-fluctuate))
- **Trend over single readings.** Weekly averages or a smoothed line (e.g. a 7-day moving average)
  show the real direction better than any single weigh-in; weight apps commonly draw one over the
  daily dots. A reading should be compared at the same time of day (morning, after the toilet,
  before food) to cut the noise.
- **Rate of change** is best shown per week (kg/vko), the unit diet advice uses.

## Derived body values

- **BMI** = kg / m². Cheap and standard, but blind to body composition: read it with caution over
  65 (a little extra weight may protect; muscle loss lowers BMI while fat rises) and with high
  muscle mass. NICE advises **waist-to-height ratio** next to BMI for BMI under 35: 0.4–0.49
  healthy, 0.5–0.59 increased, ≥0.6 high risk, same for both sexes ("waist under half of height";
  needs a waist measurement).
  ([NICE CG189 draft](https://nice.org.uk/guidance/cg189/update/cg189-update-1/documents/draft-guideline),
  [Pulse](https://pulsetoday.co.uk/news/clinical-areas/obesity-and-nutrition/nice-urges-patients-to-seek-hcp-advice-if-waist-measures-more-than-half-of-height/),
  [News-Medical](https://www.news-medical.net/news/20251119/BMI-is-failing-to-detect-dangerous-obesity-as-people-age.aspx))
- **Basal metabolic rate:** Mifflin-St Jeor (weight, height, age, sex) is the most accurate common
  equation, within ~10 % for most non-athlete adults but off by more for about one in five (one
  in three with obesity); it ignores body composition. Harris-Benedict reads higher.
  ([Harris–Benedict, Wikipedia](https://www.wikipedia.com/wiki/Harris-Benedict_equation),
  [PMC5867535](https://pmc.ncbi.nlm.nih.gov/articles/PMC5867535))
- **Body fat from a consumer scale's impedance:** weight is accurate (±0.3 kg against DXA) but
  body composition has large individual errors; the equations are proprietary and fit group
  averages; hydration swings the reading by several points. Good for a group, weak for one person.
  ([PubMed 20054195](https://pubmed.ncbi.nlm.nih.gov/20054195/),
  [PMC6452160](https://pmc.ncbi.nlm.nih.gov/articles/PMC6452160),
  [Acibadem](https://acibademinternational.com/blog/smart-scales-and-bioimpedance-how-accurate-is-your-body-fat-reading/))
- **Health Connect** has records for body fat %, basal metabolic rate, lean body mass (and
  height, weight), each with its own permission; none for BMI.
  ([records package](https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/package-summary))

## Android

- **Background work is unreliable on Samsung** (battery optimisation stops scheduled jobs); prefer
  doing work when the app is opened or right after the change that needs it.
- **Health Connect from the background** ([read data](https://developer.android.com/guide/health-and-fitness/health-connect/develop/read-data),
  [rate limiting](https://developer.android.com/health-and-fitness/health-connect/rate-limiting)):
  reads are foreground-only unless the user grants `READ_HEALTH_DATA_IN_BACKGROUND`; inserts and
  deletes are not listed as foreground-only, so a BroadcastReceiver may write. Background quotas
  are stricter and unpublished; a quota error is an exception like any failed write. From Android
  14 the Health Connect permissions are platform permissions, so `checkSelfPermission` answers
  without a Health Connect call.
- **Widget updates**: `AppWidgetManager.updateAppWidget` works from any receiver;
  `updatePeriodMillis` wakes the phone and is at least 30 min, so the widget is redrawn at the
  app's own alarms and events instead. Bluetooth on/off and power-saving changes are not
  implicit-broadcast exceptions, and battery restriction has no broadcast at all, so a widget
  showing them lags until the next own redraw.
- **Samsung "Optimoitu" battery setting stops screen-off BLE scans** (own test 2026-11-14, S24,
  Android 17): a filtered PendingIntent scan delivered packets only when the screen came on while
  the app was "Optimoitu"; after "Rajoittamaton" packets arrived with the screen off and locked.
  Android's "Restricted" is not the only blocker: warn unless the app ignores battery optimisation.

- **Chart libraries for Compose (2026-11):** Vico (Apache 2.0, Compose-first, maintained; line,
  column, combo layers, markers, zoom/scroll), MPAndroidChart 4 (rewritten in Kotlin 2026 with a
  Compose module, JitPack only, markers, zoom, 30k points), KoalaPlot (Compose Multiplatform, 0.x
  API, zoom/pan, stacked bars), ComposeCharts by ehsannarmani (0.2.x, animated, simple).
  ([Vico](https://github.com/patrykandpatrick/vico), [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart),
  [KoalaPlot](https://github.com/KoalaPlot/koalaplot-core), [ComposeCharts](https://github.com/ehsannarmani/ComposeCharts))


## Motivation and feedback

- **Logging itself is the active part.** In a review of 39 trials, more self-monitoring went with
  more weight loss in over 70 % of reported links; goal setting and tailored feedback strengthen
  it. Adherence falls over time (about 70 % in week one to 20 % after 70 weeks), so the effort of
  logging matters more than chart polish.
  ([PMC7269678](https://pmc.ncbi.nlm.nih.gov/articles/PMC7269678), [Spark protocol](https://cdn.clinicaltrials.gov/large-docs/65/NCT05249465/Prot_000.pdf))
- **Bad news weighs more than good.** Days with a weight gain bring more guilt, shame and lower
  confidence; daily weighing with a trend line raised mood swings in one trial (another found no
  harm). Setbacks hit 2-3 times harder than equal wins. Difficulty using negative feedback is a
  proposed reason people stop tracking. Hence neutral framing: no red for a gain, no judgement.
  ([PMC4846305](https://pmc.ncbi.nlm.nih.gov/articles/PMC4846305), [Springer 2024](https://link.springer.com/content/pdf/10.1007/s10865-024-00476-4.pdf))
- **Visible progress motivates** (progress principle: small steps in meaningful work drive daily
  motivation; goal-gradient: effort rises as a goal nears). Sources are summaries, not primary.
  ([Ness Labs](https://nesslabs.com/?p=12440), [Learnnovators](https://learnnovators.com/blog/the-goal-gradient-effect-why-visible-progress-sustains-motivation/))
- **Smoothed weight takes the sting out of plateaus** (Happy Scale, Libra: a moving average over
  the daily weights; users report less discouragement; it lags after big changes). Anecdotal.
  ([Happy Scale support](https://happyscale.com/support))
- **Streaks and gamification:** evidence mixed, effects on steps small; no solid proof either way.
  ([Sahha](https://sahha.ai/blog/gamification-behavioral-nudges-health-apps/), [Frontiers 2023](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2023.1286463/pdf))
- **Flat average as a reference:** step apps draw the period's daily average as one line, so a
  bar above it reads as an above-average day; Apple's Trends compare a recent 90-day average to
  the 365-day one instead of drawing a moving line.
  ([Vantagefit](https://www.vantagefit.io/en/help/health-tracking/what-are-trends/), [leancrew](https://leancrew.com/all-this/2024/11/apple-health-trends/))

## Steps in Health Connect

- **Read steps with `aggregate`, not raw records.** Steps are cumulative; the aggregate API removes
  duplicates when several apps write the same steps (Garmin Connect and Samsung Health) and keeps
  the app with the highest priority in the user's Health Connect priority list (only the user can
  change it). Raw records summed by hand double count. `aggregateGroupByPeriod` with a one-day
  period and `LocalDateTime` bounds (an `Instant` range throws) gives daily totals; the bucket list
  is sparse, a day without data is missing.
  ([aggregate data](https://developer.android.com/health-and-fitness/guides/health-connect/develop/aggregate-data),
  [read data](https://developer.android.com/health-and-fitness/guides/health-connect/develop/read-data))
- **On-device steps:** from Android 14, once any app holds the steps read permission, Health Connect
  counts the phone's own steps (batched about once a minute) as one more source in the priority
  dedupe. From the June 2026 update their origin is a device-specific synthetic package name instead
  of `android`; an aggregate without an origin filter includes them unchanged. (read data page)
- **The merge fills gaps across apps.** The no-filter aggregate takes, for each stretch of time, the
  highest-priority app that has data there, so the phone's steps fill the hours a watch left empty:
  the day's total can exceed every single app's own count. One app's own daily total =
  `aggregateGroupByPeriod` with `dataOriginFilter` = that app's packages. (aggregate data page;
  seen in this app with Garmin Connect + the phone, 2026-11-15)
- **`READ_STEPS` alone reads the origins.** Every `StepsRecord` carries `metadata.dataOrigin`, and
  aggregates accept `dataOriginFilter`; no extra permission is needed. Whether
  `AggregationResult.dataOrigins` lists origins the dedupe dropped is not documented, so the origin
  list also needs a records read. (read data page)
- **Phone steps = `android` + the synthetic package name (SPN).** Filter on both to get the phone's
  own steps across the June 2026 change. The SPN is device- and app-scoped and must not be
  hard-coded: framework `HealthConnectManager.getCurrentDeviceDataSource()` (API 34 + SDK extension
  22; not in the Jetpack client) gives it; the documented shape `com.android.healthconnect.phone.…`
  is only a fallback. (read data page,
  [HealthConnectManager](https://developer.android.com/reference/android/health/connect/HealthConnectManager))
- **Reads reach 30 days before the app's first permission grant**; older data needs
  `READ_HEALTH_DATA_HISTORY`, otherwise the read throws. Reads are foreground-only without the
  background-read permission.
- **Garmin Connect writes steps to Health Connect** when the user enables the link in Garmin
  Connect (connected partners), per data type; a Garmin forum thread reports hourly blocks missing
  intermittently, days undercounted by 10-20 %. Garmin's own support page was not verified.
  ([Android Central](https://www.androidcentral.com/wearables/garmin/heres-everything-garmin-will-and-wont-share-with-google-health-connect),
  [Garmin forum](https://forums.garmin.com/apps-software/mobile-apps-web/f/garmin-connect-mobile-andriod/442637/android-health-connect---missing-steps-intermittently-during-sync-no-reconciliation))
- **The total calories aggregate is partly an estimate.** `TotalCaloriesBurnedRecord.ENERGY_TOTAL`
  is deduped by priority like steps, and every interval of the requested range with no total record
  is filled with that interval's active calories plus basal calories: from `BasalMetabolicRate`
  records, else from height and weight records, else defaults (73 kg, 1,7 m, age 30). The fill
  reads those tables without the request's `dataOriginFilter`, so an origin filter does not stop
  it, and a day with no record at all can still get a total. Only `ENERGY_TOTAL` is derived;
  `ACTIVE_CALORIES_TOTAL` sums records only. To show only written data, read the raw records
  first and keep only days on which the origin wrote one. The public docs say only that total
  includes active and BMR.
  ([TotalCaloriesBurnedRecord](https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/TotalCaloriesBurnedRecord),
  AOSP `packages/modules/HealthFitness` `TotalCaloriesBurnedRecordHelper.deriveAggregate`,
  `DeriveTotalCaloriesBurnedHelper`, `DeriveBasalCaloriesBurnedHelper`, read 2026-11-15)
- **Garmin's export splits each day's burn.** The account export's UDS daily summaries hold
  `activeKilocalories`, `bmrKilocalories` and `totalKilocalories` (total = active + bmr on every
  day); days before the watch hold BMR only. What Garmin Connect writes to Health Connect for
  calories (whole-day totals, or active only for workouts) is not known yet; `burn.read` logs the
  split / total-only / active-only day counts. (a real export, 2026-11-15)
