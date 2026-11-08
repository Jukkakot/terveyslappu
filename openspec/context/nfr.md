# Non-functional requirements

Every change follows these. IDs are stable; never reuse a removed one.

- **NFR-1 No real health data in the repo (MUST, user 2026-11-08).** The repo is public. Real
  weights, step counts, logs, Health Connect or Samsung Health exports, OKOK exports and
  screenshots with real values never go into commits, issues, test data or docs. Tests use
  made-up values. Files the user shares for debugging stay in the session's scratchpad.
  `.gitignore` blocks the usual export file types as a safety net.
- **NFR-2 No own backend or server database.** Health data lives in Health Connect on the phone
  and in the user's own vendor clouds. A local cache on the phone is allowed. A cloud copy, if
  ever needed, is a file in the user's own cloud storage, not a server we run.
- **NFR-3 Shareable log.** Every build writes a log file and the app can share it through the
  Android share sheet (Quick Share to the cloud session). The log holds app events and errors;
  weight values may appear in it, so it is shared only by the user and never committed (NFR-1).
- **NFR-4 Installable updates.** Each release is signed with the same key, so Obtainium installs
  it over the previous one and Health Connect permissions stay.
- **NFR-5 Finnish UI, light and dark theme.** Texts in Finnish (string resources, so English can
  be added later); follows the system theme.
- **NFR-6 Budget 0 EUR.** No paid services or APIs.
- **NFR-7 Replayable inputs (lesson from rubikki-solveri, user 2026-11-08).** Whatever the app
  reads from the outside world (scale BLE packets, later others) is written to the log raw, so a
  failure the user shares can be replayed exactly as a JVM test. Rubikki tuned its scanner by
  guesswork through dozens of fixes until it recorded the scan input; do it from the start.
  Tests built from shared logs use made-up or object weights, never the user's own (NFR-1).
- **NFR-8 Timings in the log.** Steps whose speed is the point of the app (tap to saved weight,
  scan to stable reading, Health Connect calls) log their duration in ms, so speed is measured,
  not guessed.
- **NFR-9 Every failure leaves a trace.** No silently swallowed errors: a failing share, intent,
  permission or Health Connect call logs its cause, and the user sees a short message. (Rubikki's
  log share failed silently in one browser and the log could not be sent.)
