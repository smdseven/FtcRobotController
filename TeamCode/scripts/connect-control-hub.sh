#!/bin/bash
# Connect this Mac to the REV Control Hub over Wi-Fi ADB.
# Join the hub's Wi-Fi network first (the name usually ends in -RC).

HUB_ADDRESS="192.168.43.1:5555"

if command -v adb >/dev/null 2>&1; then
  ADB="adb"
elif [[ -x "${HOME}/Library/Android/sdk/platform-tools/adb" ]]; then
  ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
else
  echo "adb was not found." >&2
  echo "Install Android Studio, then confirm this file exists:" >&2
  echo "  ${HOME}/Library/Android/sdk/platform-tools/adb" >&2
  exit 1
fi

echo "Connecting to the Control Hub at ${HUB_ADDRESS}"
"${ADB}" connect "${HUB_ADDRESS}"
echo
echo "Connected devices:"
"${ADB}" devices
