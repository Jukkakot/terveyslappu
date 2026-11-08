# Open questions

Rolling IDs (Q1, Q2 …, never reused). The user answers by ID; a row is deleted once its answer
is folded into a change or a context file.

| ID | Question | Recommendation |
|---|---|---|
| Q1 | Does Samsung Health store Health Connect weights in its own data (personal-data CSV export) and so in Samsung Cloud? | Shown in Samsung Health already; check its personal-data CSV export for a terveyslappu weight |
| Q2 | Which format is the OKOK export in? | Try menu ⋮ → Tuo historia; if it finds nothing, share the log (it records the header) or the file in a session (not committed, NFR-1). xlsx is not supported yet |
| Q3 | Keep the backup file (weight-backup) as the phone-move answer, or rely on Samsung Cloud / Health Connect's own export later? | Keep it: it is visible and needs nothing else; revisit once Q1 is answered |
| Q4 | Body composition from the scale's impedance (fat %, muscle …): wanted? Needs height, birth year and sex stored in the app | Only if Samsung Health's weight view feels thin; weight alone is the goal |
| Q5 | Scale weigh-in: should a stable weight the scale is still showing from a moment ago be ignored (the user must step on after tapping)? | Decide from the first weigh-in logs (`scale.packet` lines) |
| Q6 | Weigh-in without any tap (background scan, saves when you step on): wanted, if the scale allows it? | Yes if the first logs show a stable name or id to filter on; it costs a little battery |
