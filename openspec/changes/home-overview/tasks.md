## 1. Front page cards

- [ ] 1.1 `HomeScreen`: drop `RecentList`; derived values inside the weight card; food card with "Syöty tänään" (total · herkkuja) and the outlined Lisää annos inside it; automatic weigh-in card (state line from `SettingsSummary.listen`, opens its Asetukset page) only while switched on
- [ ] 1.2 Smoke tests: no weight list on the front page; the food card opens Ruoka; the automatic weigh-in card shows only when on
- [ ] 1.3 Map line in `.claude/CLAUDE.md` if needed; `appVersion=1.25`; `./gradlew test lint assembleDebug` green
