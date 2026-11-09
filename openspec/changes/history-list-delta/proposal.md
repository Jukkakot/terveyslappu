# Proposal

## Why

The Historia list shows only values, so the ups and downs between weigh-ins have to be worked out in
the head. A change next to each weight makes the trend readable at a glance (user 2026-11-09).

## What Changes

- Each row of the Historia weight list shows the change from the weight before it in time (e.g.
  `+0,3` / `−0,5` / `±0,0` kg), in the same format as the home card's change.
- Gains and losses get two different colours from the phone's own palette, neither red nor green, so
  neither direction reads as good or bad; the sign is always shown too, so colour is never the only
  cue.
- The oldest row in the chosen period has no change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-entry`: the "Recent weights from Health Connect" list rows gain the change from the
  previous weight.

## Impact

App screens: Historia list rows only. No Health Connect, scale, widget or CI change. Pure delta
computation reusing the existing change formatting.
