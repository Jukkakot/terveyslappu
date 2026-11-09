# Open questions

Rolling IDs (Q1, Q2 …, never reused). The user answers by ID; a row is deleted once its answer
is folded into a change or a context file.

| ID | Question | Recommendation |
|---|---|---|
| Q1 | Does Samsung Health store Health Connect weights in its own data (personal-data CSV export) and so in Samsung Cloud? | Shown in Samsung Health already; check its personal-data CSV export for a terveyslappu weight |
| Q2 | Which format is the OKOK export in? | User 2026-11-09: OKOK International exports .xls, not CSV. Content unknown (true Excel, xlsx renamed, or an HTML/tab table named .xls); the import picker does not offer Excel files yet. Share the file in a session (not committed, NFR-1) to decide |
| Q3 | Keep the backup file (weight-backup) as the phone-move answer, or rely on Samsung Cloud / Health Connect's own export later? | Keep it: it is visible and needs nothing else; revisit once Q1 is answered |
| Q5 | Scale weigh-in: should a stable weight the scale is still showing from a moment ago be ignored (the user must step on after tapping)? | Decide from the first weigh-in logs (`scale.packet` lines) |
