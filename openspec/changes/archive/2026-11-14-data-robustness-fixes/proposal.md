## Why

An audit found four ways the app can silently lose or misshow data: the CSV drops the time-zone
offset (the DST repeated hour and trips across zones come back wrong), the automatic backup
shrinks without the history permission, charts keep an old "now" after a reload, and a pending
export/backup is forgotten when the activity is recreated.

## What Changes

- CSV time column carries the UTC offset (`07:12:00+03:00`); the reader accepts times with or
  without an offset (old files are read in the phone's zone).
- Backup: without the history permission the status says "recent only"; a write that would cut
  the row count by more than half versus the last count is refused (status failed, reason shown,
  log `backup.run refused reason=shrink`). A manual "Varmuuskopioi nyt" is not guarded.
- `MainUiState.loadedAt` is set on every load; Historia and Home key their `now` on it.
- The pending export/backup action after the history-permission screen is kept in the ViewModel.
- No version bump (fixes raise BUILD only).

## Capabilities

### Modified Capabilities
- `data-file`: CSV time column with offset.
- `weight-backup`: recent-only note, shrink guard.
