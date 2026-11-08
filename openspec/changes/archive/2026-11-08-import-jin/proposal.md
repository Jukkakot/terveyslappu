# Proposal

## Why

The OKOK app (Chinese origin) can show weight in jin (斤, half a kilogram). If the user's export
uses jin, the import would store double weights or reject them as out of range.

## What Changes

- The history import converts a weight column whose header says jin or 斤 (but not 公斤, which is
  kg) to kilograms by halving it.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `history-import`: "Read the table" gains the jin unit.

## Impact

- Parts: history file parser only.
