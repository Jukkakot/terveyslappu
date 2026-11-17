## 1. Lines

- [ ] 1.1 In `chart/CustomChartSpec`, for Syöty, Kulutus and Askeleet lines (Allekkain and Päällekkäin, card and tab), leave today out of the line's points and add today, when it has a value, as a separate one-point line series in the same tone and side (a one-point line draws its dot); no legend entry or average of its own; Paino unchanged
- [ ] 1.2 Tests in `CustomChartSpecTest`: partial day (line ends at yesterday, today a separate one-point series), only today, weight line still joins today, bars unchanged
- [ ] 1.3 Bump `appVersion` to 1.58; run `./gradlew test lint assembleDebug`
