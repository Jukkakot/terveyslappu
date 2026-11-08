#!/usr/bin/env bash
# Cloud sessions start from a fresh container: install the OpenSpec CLI and the Android SDK
# (command-line tools, then the platform and build tools the project compiles against), and point
# Gradle at it through local.properties, so `./gradlew test lint assembleDebug` runs here.
if [ "${CLAUDE_CODE_REMOTE:-}" = "true" ]; then
  command -v openspec >/dev/null || npm install -g @fission-ai/openspec >/dev/null 2>&1 || true

  SDK="$HOME/android-sdk"
  if [ ! -x "$SDK/cmdline-tools/latest/bin/android" ]; then
    zip=$(curl -s https://dl.google.com/android/repository/repository2-3.xml \
      | grep -o 'commandlinetools-linux-[0-9]*_latest.zip' | head -1)
    if [ -n "$zip" ]; then
      mkdir -p "$SDK/cmdline-tools" && tmp=$(mktemp -d) \
        && curl -s -o "$tmp/clt.zip" "https://dl.google.com/android/repository/$zip" \
        && unzip -q "$tmp/clt.zip" -d "$tmp" && rm -rf "$SDK/cmdline-tools/latest" \
        && mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
      rm -rf "$tmp"
    fi
  fi
  if [ -x "$SDK/cmdline-tools/latest/bin/android" ] && [ ! -d "$SDK/platforms/android-37.0" ]; then
    "$SDK/cmdline-tools/latest/bin/android" --sdk="$SDK" sdk install \
      "platforms;android-37.0" "build-tools;36.0.0" >&2 || echo "Android SDK install failed; run it by hand" >&2
  fi
  [ -f "$CLAUDE_PROJECT_DIR/local.properties" ] || echo "sdk.dir=$SDK" > "$CLAUDE_PROJECT_DIR/local.properties"
fi
exit 0
