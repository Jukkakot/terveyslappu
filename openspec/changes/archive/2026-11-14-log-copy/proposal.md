## Why
Jaa loki sends whole files; pasting the log into a chat for debugging needs a short text. The
scale's repeated packets make the log long.

## What Changes
- Asetukset Tuki: new row "Kopioi loki" after "Jaa loki" copies a compact text of the last 24 h to
  the clipboard (snackbar "Loki kopioitu").
- Compaction: repeated `scale.packet` lines collapse, size capped at 40 000 characters.
- New log event `log.copy`.

## Capabilities
### Modified Capabilities
- `app-log`: adds the copy requirement.

## Impact
Settings screen, a pure compaction function in `log/`, `MainActivity`.
