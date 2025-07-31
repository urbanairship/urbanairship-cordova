#!/usr/bin/env bash
set -euxo pipefail

SCRIPT_DIRECTORY="$(cd "$(dirname "$0")" && pwd)"
ROOT_PATH="$SCRIPT_DIRECTORY/.."

PROXY_VERSION="$1"
if [ -z "$PROXY_VERSION" ]; then
    echo "No proxy version supplied"
    exit 1
fi

# Update plugin.xml
sed -i.bak -E "s/(pod name=\"AirshipFrameworkProxy\" spec=\")[^\"]*\"/\1$PROXY_VERSION\"/" "$ROOT_PATH/cordova-airship/plugin.xml"

# Update Android build-extras.gradle
sed -i.bak -E "s/(api \"com.urbanairship.android:airship-framework-proxy:)[^\"]*\"/\1$PROXY_VERSION\"/" "$ROOT_PATH/cordova-airship/src/android/build-extras.gradle"

# Update HMS build-extras.gradle
sed -i.bak -E "s/(implementation \"com.urbanairship.android:airship-framework-proxy-hms:)[^\"]*\"/\1$PROXY_VERSION\"/" "$ROOT_PATH/cordova-airship-hms/src/android/build-extras.gradle"

# Clean up backup files
find "$ROOT_PATH" -name "*.bak" -delete
