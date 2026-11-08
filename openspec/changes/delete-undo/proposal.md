# Proposal

## Why

Deleting a weight asks for confirmation in a dialog. The user's UI rule is undo instead of
confirmation: one long-press should do the thing, and a mistake should be one tap to take back.

## What Changes

- Long-pressing an own weight deletes it at once; the message "Paino poistettu" offers Kumoa for
  about ten seconds.
- Kumoa writes the same weight back at the same time, keeping whether it came from the scale.
- Weights of other apps still cannot be deleted here.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `weight-entry`: "Delete own entry" replaces the confirmation with undo.

## Impact

- Parts: main screen (no delete dialog, snackbar action), Health Connect (re-insert on undo).
- The automatic backup runs after the delete and again after an undo, as for any change.
