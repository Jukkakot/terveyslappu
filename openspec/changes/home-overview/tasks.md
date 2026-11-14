## 1. Front page cards

- [ ] 1.1 Pure `chart/HomeFood` (today's sum, daily bars of 7/30/90 days ending today, average per logged day without today) with unit tests; the food card's period in `PeriodPrefs` (`home.period` log gets `card=weight|food`)
- [ ] 1.2 `HomeScreen`: drop `RecentList`; derived values inside the weight card; food card per the spec (bars in Energia's tones, own period menu, Lisää annos + Tänään inside, tap opens Historia on Energia, permission line); automatic weigh-in card (state line from `SettingsSummary.listen`, opens its Asetukset page) only while on
- [ ] 1.3 Smoke tests: no weight list on the front page; the food card shows bars and opens Energia; the automatic weigh-in card shows only when on
- [ ] 1.4 `.claude/CLAUDE.md` map (`HomeFood`); `appVersion=1.25`; `./gradlew test lint assembleDebug` green
