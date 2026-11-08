# Proposal

## Why

The weigh-in widget shows four lines (icon, Punnitse, weight, day). Resized to one cell they would
be clipped, which breaks the "state legible" rule.

## What Changes

- The widget picks its layout by size: one cell high shows the icon and the latest weight only;
  taller shows icon, Punnitse, weight and day as now. Default size is one by two cells.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- (none) Layout only; the specified content stays at the default size (`skip_specs`).

## Impact

- Widget layouts and provider info only.
