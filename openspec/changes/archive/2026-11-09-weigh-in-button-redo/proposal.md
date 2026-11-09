# Proposal

## Why

The home-screen widget and the Quick Settings tile show the previous weight before the user has
stepped on the scale, which the user does not want to see. After a weigh-in there is also no way
to throw away a bad reading (wrong person, a bag in hand) and weigh again; that needs the main
screen today.

## What Changes

- The widget becomes a plain "Punnitse" button (default size 1x1) that only starts the weigh-in
  and shows no weight. The existing widget changes in place; no second widget.
- The Quick Settings tile loses its latest-weight subtitle.
- The app no longer keeps a latest-weight cache for the widget and tile (nothing reads it).
- After saving, the weigh-in dialog shows the result for about 10 seconds instead of 3; a tap on
  the card keeps it open. It offers "Punnitse uudelleen" and "Valmis". The change from the
  previous weight stays.
- "Punnitse uudelleen" deletes the just-saved weight at once, without asking, and scans again.
  Until a new weight is saved the dialog offers "Kumoa", which writes the weight back and returns
  to the result. Closing without a new weight leaves it deleted.

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: saved result stays longer with redo and done buttons; new redo-with-undo
  requirement; the latest-weight-on-widget-and-tile requirement is removed.

## Impact

Touches the widget, the Quick Settings tile, the weigh-in dialog and Health Connect (deleting and
writing back the just-saved scale weight). Main screen only loses the cache update. No build/CI
change. A widget already on the home screen keeps its size; the user can shrink it to 1x1.
