# Proposal

## Why

The Historia list shows only values, so the ups and downs between weigh-ins have to be worked out in
the head, and most change numbers are bare signed text. A change next to each weight and a small
arrow next to every change make the direction readable at a glance (user 2026-11-09).

## What Changes

- Each row of the Historia weight list shows the change from the weight before it in time (e.g.
  `+0,3` / `−0,5` / `±0,0` kg), in the same format as the home card's change.
- Gains and losses in the list get two different colours from the phone's own palette, neither red
  nor green, so neither direction reads as good or bad; the sign is always shown too.
- The oldest row in the chosen period has no change.
- Every change number gets a small arrow up (gain) or down (loss), none at zero: list rows, the
  period stats' Muutos and Tahti tiles, and the home card's 30-day change (the card's latest change
  already has one). One shared look for all of them.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-entry`: the "Recent weights from Health Connect" list rows gain the change from the
  previous weight, with arrow and neutral colours.
- `weight-chart`: "Period statistics" change and change per week show an arrow.
- `home-screen`: "Simple home screen" names the 30-day change and gives both card changes an arrow.

## Impact

App screens: home card, Historia stats and list rows. No Health Connect, scale, widget or CI change.
Pure delta computation and a shared change label reusing the existing formatting and arrow icons.
