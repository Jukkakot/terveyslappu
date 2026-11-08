#!/usr/bin/env bash
# UserPromptSubmit: every request in this project goes through OpenSpec.
cat <<'TXT'
OpenSpec flow (project rule): before answering, read the specs of the parts concerned (openspec/specs/<capability>/spec.md), code only where they do not answer. Route the request through the matching skill even when the user does not name it: ideas, questions, feedback -> openspec-explore; a new change -> openspec-propose; revising a planned change -> openspec-update-change; implementing -> openspec-apply-change; finishing -> openspec-archive-change. Small talk and questions about the workflow itself need none. Never commit real health data (NFR-1).
TXT
