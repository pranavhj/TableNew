#!/bin/bash
# deploy.sh — download latest APK from GitHub Actions and install on phone
# Usage: bash deploy.sh [--wait]
#   --wait   block until the current in-progress run finishes before downloading

ADB="/c/Users/prana/AppData/Local/Android/Sdk/platform-tools/adb.exe"
GH="/c/Program Files/GitHub CLI/gh.exe"
DEVICE="10.0.0.122:5555"
REPO="pranavhj/TableNew"
TMP_DIR="/tmp/tablenew-deploy"

# --- Optional: wait for in-progress run to finish ---
if [[ "$1" == "--wait" ]]; then
    echo "Waiting for latest run to complete..."
    "$GH" run watch --repo "$REPO" 2>/dev/null || true
fi

# --- Find latest successful run ---
echo "Looking for latest successful build..."
RUN_ID=$("$GH" run list --repo "$REPO" --status success --limit 1 --json databaseId -q '.[0].databaseId' 2>/dev/null)
if [[ -z "$RUN_ID" ]]; then
    echo "No successful runs found. Check: $GH run list --repo $REPO"
    exit 1
fi
echo "Run ID: $RUN_ID"

# --- Download and extract APK ---
rm -rf "$TMP_DIR" && mkdir -p "$TMP_DIR"
"$GH" run download "$RUN_ID" --repo "$REPO" -n app-debug -D "$TMP_DIR"

APK="$TMP_DIR/app-debug.apk"
if [[ ! -f "$APK" ]]; then
    echo "APK not found. Contents of $TMP_DIR:"
    ls "$TMP_DIR"
    exit 1
fi

# --- Install on device ---
if ! "$ADB" devices | grep -q "^${DEVICE}"; then
    echo "Device $DEVICE not found. Run: $ADB connect $DEVICE"
    exit 1
fi

echo "Installing on $DEVICE..."
"$ADB" -s "$DEVICE" install -r "$APK" && echo "Done." || {
    echo "Install failed — trying fresh install..."
    "$ADB" -s "$DEVICE" uninstall com.example.tablenew
    "$ADB" -s "$DEVICE" install "$APK" && echo "Done."
}

rm -rf "$TMP_DIR"
