#!/bin/bash
# logcat.sh — stream TableNew logs from connected device
# Usage:
#   bash logcat.sh          # filter to TableNew tag only
#   bash logcat.sh full     # all tags (unfiltered)
#   bash logcat.sh crash    # crashes only (AndroidRuntime)

ADB="/c/Users/prana/AppData/Local/Android/Sdk/platform-tools/adb.exe"
DEVICE="10.0.0.122:5555"  # OnePlus 7 wireless — update if IP changes

# Prefer wireless; fall back to any single device
if "$ADB" devices | grep -q "^${DEVICE}"; then
    ADB_FLAGS="-s $DEVICE"
elif [ $("$ADB" devices | grep -c "device$") -eq 1 ]; then
    ADB_FLAGS=""
else
    echo "Multiple devices found. Set DEVICE= in logcat.sh or disconnect USB."
    "$ADB" devices
    exit 1
fi

MODE="${1:-default}"

case "$MODE" in
    full)
        echo "[logcat] full output — Ctrl+C to stop"
        "$ADB" $ADB_FLAGS logcat -v time
        ;;
    crash)
        echo "[logcat] crashes only — Ctrl+C to stop"
        "$ADB" $ADB_FLAGS logcat -v time "*:S" AndroidRuntime:E TableNew:E
        ;;
    *)
        echo "[logcat] TableNew only — Ctrl+C to stop"
        "$ADB" $ADB_FLAGS logcat -v time "*:S" TableNew:V AndroidRuntime:E
        ;;
esac
