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
| Android | background work on Samsung |

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

## Android

- **Background work is unreliable on Samsung** (battery optimisation stops scheduled jobs); prefer
  doing work when the app is opened or right after the change that needs it.
