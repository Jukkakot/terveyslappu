# Knowledge

General knowledge this project relies on: principles and facts about the domain that do not
change with our code. Our own choices live in `product.md`, facts about the code in the code.
Read the section a task touches before researching it again; add what a new search taught, with
the source. No configuration, versions or other things that go stale.

**Never read this whole file.** Find the topic in the index, then read only that section
(`grep -n '^## '` gives the line numbers). Keep the index in step when adding a section.

## Index

| Section | What is there |
|---|---|
| Body weight data | daily fluctuation size and causes, trend vs single readings, rate per week |
| Charts | y-axis start and minimum span, one scale rule, time axis, dots by density, units, direction without colour |
| Data files | CSV/JSON, versioned extensible files, unique export names |
| Android | background work on Samsung |

Sections marked *(generic)* hold no project facts and are waiting to move to the global
knowledge in claude-config.

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

## Charts *(generic)*

- **Line charts may start above zero.** Unlike bars (length = value), a line encodes change, so
  the y-axis can cover just the data with some margin.
  ([Data Visualisation Guide](https://data.europa.eu/apps/data-visualisation-guide/scales-in-line-charts),
  [FlowingData](https://flowingdata.com/2024/03/06/line-chart-baselines))
- **But a too tight scale lies too.** Auto-scaling to min..max turns noise into a dramatic slope;
  give the axis a minimum span that matches the normal noise of the data, so a small change looks
  small. ([Observable](https://observablehq.com/blog/never-okay-crop-y-axis-except-when-it-is),
  [Chez Voila](https://chezvoila.com/blog/yaxis/))
- **Keep the same scale rule everywhere.** Charts of the same quantity on different screens or
  periods should use the same rule, otherwise equal changes look different.
- **Time on x spans the whole period**, also when the data covers only part of it; otherwise two
  days look like a month. Leave gaps (break the line) where there is no data for long.
- **Markers (dots) for sparse points, none on dense series.** Guides agree on the idea but give no
  number; a rule by room (enough width per point that dots never touch) works on every screen.
  ([Deckary](https://deckary.com/blog/line-chart-powerpoint),
  [The Bricks](https://thebricks.com/resources/guide-how-to-add-dots-to-a-line-graph-in-excel))
- **Units on every number and axis** (`kg`); a bare number makes the reader guess.
- **Direction not by colour alone:** arrows or signs next to changes (accessibility, colour
  blindness); gain/loss colours stay neutral when the "good" direction depends on the user.

## Data files *(generic)*

- **CSV and JSON** are what other apps import and export; CSV for spreadsheets, JSON for
  structured data (nested raw values). Recognise the format from the content, not the file name.
- **Versioned, extensible files:** a version marker and one part per data kind let a newer file
  still be read by an older reader, which skips and reports the parts it does not know.
- **Unique export names** with a timestamp to the millisecond (`name-export-yyyyMMdd-HHmmss-SSS`)
  never collide and sort by time.

## Android

- **Background work is unreliable on Samsung** (battery optimisation stops scheduled jobs); prefer
  doing work when the app is opened or right after the change that needs it.
