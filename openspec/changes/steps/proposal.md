# Proposal: steps

## Why

The user wears a Garmin watch whose steps reach Health Connect. Eating (food-log) and weight are
already in the app; steps complete the picture the food log was started for (product.md "Food log:
why", BR2). The front page was built so a steps card takes the right half of the food card's row
(home-overview, home-compact), and every feature gets the same parts on the front page and in its
details (parity rule).

## What Changes

- **Read-only steps** from Health Connect: the app never writes steps. Daily totals come from
  Health Connect's own aggregate, which already merges Garmin Connect, Samsung Health and the
  phone's own counter by the user's data-source priority (no summing of raw records).
- **Front page:** a steps card "Askeleet tänään" in the right half of the food card's row (the food
  card becomes the left half): today's steps large, small daily bars of the card's own period
  (7, 30 or 90 days, remembered, own menu), first and last day, the period's average. A tap opens
  Historia on Askeleet. No quick action (steps have nothing to add).
- **Historia:** a fourth tab **Askeleet** after Energia: daily bars (weekly from 90 days up), the
  7-day average line, the weight line under it on the same time axis, statistics (average per day
  with data, days with data) and the list of days, newest first.
- **Permission:** the steps read permission is asked only from the steps card or the Askeleet tab,
  never from a weigh-in or at start-up; without it both show the reason and a grant button, and the
  rest of the app works as before. Older than 30 days uses the history permission the app already
  asks for weights.
- **Data file and backup do not carry steps** (decision: the data is Garmin's, it lives in Health
  Connect and Garmin Connect).
- Ohje gets a section on steps (including where the numbers come from and how to fix a wrong
  source). Log events with counts and timings only. Version MINOR bump.
- Not here: the combined chart (backlog "combined chart"), a step goal or target line, distance,
  floors, heart rate or workouts, writing steps, a steps widget or notification.

## Capabilities

### New Capabilities
- `steps`: reading steps from Health Connect (aggregate daily totals, no writing), the permission
  and its states, history older than 30 days, steps stay out of the data file, log events.

### Modified Capabilities
- `home-screen`: the row holds the food card and the steps card side by side; the steps card.
- `weight-chart`: Historia's tabs gain Askeleet with its chart, statistics and list.
- `data-file`: explicit statement that steps are not a data kind.
- `help-page`: an Ohje section on steps.

## Impact

- App screens: front page row (food card narrower, steps card), Historia fourth tab, Ohje text.
- Health Connect: `READ_STEPS` only (no write); asking it also makes Health Connect start counting
  the phone's own steps on Android 14+.
- No new storage, no change to the data file, the backup or the other features.
