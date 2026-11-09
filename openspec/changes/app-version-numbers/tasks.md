# Tasks

## 1. Version

- [ ] 1.1 `appVersion=1.0` in `gradle.properties`; build number = commits outside `openspec/`,
  `.claude/` and `*.md` (the paths CI ignores); version name `<appVersion>.<build>`; version code
  stays the total commit count; `GIT_SHA` as a BuildConfig field. Verify:
  `./gradlew -q :app:printVersionName` prints `1.0.<n>`.
- [ ] 1.2 Menu shows `v<version>`; `app.start` logs `version=` and `commit=`; CI release title
  `Terveyslappu v<version> (<sha>)`. Verify: `./gradlew test lint assembleDebug` passes
  (the menu smoke test still finds the version item).

## 2. Context

- [ ] 2.1 `.claude/CLAUDE.md` (version line and the bump rule) and `openspec/context/product.md`
  (decision). Verify: files read correctly.
