#!/bin/bash
# logcat.sh — stream TableNew logs from connected device
# Usage:
#   bash logcat.sh          # filter to TableNew tag only
#   bash logcat.sh full     # all tags (unfiltered)
#   bash logcat.sh crash    # crashes only (AndroidRuntime)

ADB="/c/Users/prana/AppData/Local/Android/Sdk/platform-tools/adb.exe"

# Ensure device is connected
if ! "$ADB" devices | grep -q "device$"; then
    echo "No device connected. Connect USB or run:"
    echo "  $ADB connect <phone-ip>:5555"
    exit 1
fi

MODE="${1:-default}"

case "$MODE" in
    full)
        echo "[logcat] full output — Ctrl+C to stop"
        "$ADB" logcat -v time
        ;;
    crash)
        echo "[logcat] crashes only — Ctrl+C to stop"
        "$ADB" logcat -v time "*:S" AndroidRuntime:E TableNew:E
        ;;
    *)
        echo "[logcat] TableNew only — Ctrl+C to stop"
        "$ADB" logcat -v time "*:S" TableNew:V AndroidRuntime:E
        ;;
esac
