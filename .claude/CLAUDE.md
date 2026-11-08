# Terveyslappu – working instructions

Android app for effortless weight logging into Health Connect. Product decisions:
`openspec/context/product.md`; requirements: `openspec/context/nfr.md`; plan:
`openspec/context/roadmap.md`; open questions: `openspec/context/questions.md`.

## Never commit health data

The repo is public (NFR-1). Logs, exports, screenshots and values the user shares stay in the
session scratchpad. Tests use made-up weights. Check `git diff --cached` before every commit.

## Session start

Read `openspec/context/roadmap.md` and run `openspec list`, then tell the user in two or three
lines where the project stands and the natural next step. Project memory is `.claude/memory/`
(index `MEMORY.md`); write new project memories there.

## Specs first

Read the specs of the parts concerned (`openspec/specs/<capability>/spec.md`) before proposing or
reading code. Every request goes through the matching OpenSpec skill (explore, propose, apply,
archive); a `UserPromptSubmit` hook repeats this.

## Working agreements

- Commit and push to `main` yourself (public GitHub repo `Jukkakot/terveyslappu`).
- The user develops from the phone through cloud sessions and installs builds with Obtainium.
  There is no device in the cloud: compile and unit-test here, the user tests on the phone and
  shares the app log (Quick Share link → `quickshare` skill) when something fails.
- Summaries: what the user will notice and every decision taken on their behalf; no "how to
  check" lists.

## Build, release, logs

Filled in by `poc-manual-weight` (Gradle commands, CI workflows, signing secrets, log location).
