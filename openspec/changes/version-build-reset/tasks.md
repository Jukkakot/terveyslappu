## 1. Build number

- [ ] 1.1 `app/build.gradle.kts`: BUILD counts the app commits after the last commit that changed `appVersion` (`git log -1 -G'^appVersion=' -- gradle.properties`); without one, the old count
- [ ] 1.2 `.claude/CLAUDE.md` release notes: BUILD resets with MAJOR.MINOR; the food-log commit builds as 1.24.0
