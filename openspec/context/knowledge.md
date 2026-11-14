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
| Android | background work on Samsung, Health Connect from the background, widget updates |
| Steps in Health Connect | aggregate dedupe by app priority, daily buckets, on-device steps, history limit, Garmin gaps |

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
- **Reads reach 30 days before the app's first permission grant**; older data needs
  `READ_HEALTH_DATA_HISTORY`, otherwise the read throws. Reads are foreground-only without the
  background-read permission.
- **Garmin Connect writes steps to Health Connect** when the user enables the link in Garmin
  Connect (connected partners), per data type; a Garmin forum thread reports hourly blocks missing
  intermittently, days undercounted by 10-20 %. Garmin's own support page was not verified.
  ([Android Central](https://www.androidcentral.com/wearables/garmin/heres-everything-garmin-will-and-wont-share-with-google-health-connect),
  [Garmin forum](https://forums.garmin.com/apps-software/mobile-apps-web/f/garmin-connect-mobile-andriod/442637/android-health-connect---missing-steps-intermittently-during-sync-no-reconciliation))
