## ADDED Requirements

### Requirement: Copy the log
Asetukset SHALL offer "Kopioi loki" right after "Jaa loki". Tapping it SHALL put a compact text
on the clipboard and show a snackbar "Loki kopioitu" with the line count, and log
`log.copy lines= chars=`. The text SHALL start with the line
`terveyslappu v<version> commit=<commit> copied=<local time>`, then the log lines (older file
first) from the last 24 hours. Consecutive `scale.packet` lines with the same `key` and `data`
SHALL collapse into the first of them followed by ` x<count> rssi=<min>..<max>`. When the text
passes 40 000 characters the oldest lines SHALL be dropped, the newest kept, and a line
`... <n> older lines left out` added after the header. Jaa loki SHALL stay as it is.

#### Scenario: Repeated packets
- **WHEN** five consecutive `scale.packet` lines share key and data
- **THEN** the copy has the first line once, ending ` x5 rssi=<min>..<max>`

#### Scenario: Too long
- **WHEN** the compacted text is over 40 000 characters
- **THEN** only the newest lines fit and the line `... <n> older lines left out` tells how many were dropped

#### Scenario: Old lines
- **WHEN** a line is older than 24 hours
- **THEN** it is not copied
