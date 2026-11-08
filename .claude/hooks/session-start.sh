#!/usr/bin/env bash
# Cloud sessions start from a fresh container: install the OpenSpec CLI.
# The Android SDK setup is added by the poc-manual-weight change.
if [ "${CLAUDE_CODE_REMOTE:-}" = "true" ]; then
  command -v openspec >/dev/null || npm install -g @fission-ai/openspec >/dev/null 2>&1 || true
fi
exit 0
