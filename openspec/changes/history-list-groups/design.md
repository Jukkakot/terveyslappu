# Design

## Context

`ui/HistoryScreen` shows the period's weights as a flat `LazyColumn` of `WeightRowItem`s keyed by
entry id; `chart/changesFromPrevious` gives each row's change. Delete and undo go through
`MainActions`.

## Goals / Non-Goals

**Goals:** a pure, tested tree builder; the UI only flattens the open part of the tree into lazy
rows. **Non-Goals:** averages per group, remembering open groups, grouping on the home screen.

## Decisions

- **Pure tree in `chart/ListGroups`**: input the period's entries and the zone; output nodes
  `Group(level, key, label parts, first, last, count, children)`, `Weight(entry, change)` and
  `Gap(fromDay, toDay)`. Levels YEAR, MONTH, WEEK (ISO Monday start, key = month + week so weeks
  split at month borders), DAY. A group with one child is replaced by that child (applied bottom
  up, so a lone weight rises as far as it can). Gap rows go between neighbours whose last/first
  weights are more than 30 days apart, at the level where they are siblings.
- **Per-weight change stops at gaps**: `changesFromPrevious` gets the 30-day limit (shared
  constant with `ChartData.MAX_GAP_DAYS`).
- **Flattening in the UI**: open set = group keys in `rememberSaveable(period)`; default = the path
  to the newest weight. Rows indent by level (small step) so the hierarchy reads without lines.
  Keys `y2025`, `m2025-11`, `w2025-11-45`, `d2025-11-04`, entry ids for weights, `gap-<from>`.
- **Labels in the UI** from the node's dates (Finnish month names via the existing formats), so
  the pure part holds no copy.
- **Alternative considered:** a level picker (Päivä/Viikko/Kuukausi/Vuosi) as in some weight apps;
  rejected: one more control, and the tree with auto-collapse gives both overview and detail.

## Risks / Trade-offs

- [Deep trees mean many taps to an old weight] → only levels with more than one child exist; the
  chart's point details still reach any weight directly.
- [Deleting the last weight of a group] → the tree rebuilds from the new list; the group disappears
  and undo brings it back.
