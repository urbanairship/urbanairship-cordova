#!/bin/bash
set -e

ROOT_PATH=`dirname "${0}"`/..

CORE_PACKAGE_PATH="$ROOT_PATH/cordova-airship/package.json"
HMS_PACKAGE_PATH="$ROOT_PATH/cordova-airship-hms/package.json"
ANDROID_VERISON_PATH="$ROOT_PATH/cordova-airship/src/android/AirshipCordovaVersion.kt"
IOS_VERISON_PATH="$ROOT_PATH/cordova-airship/src/ios/AirshipCordovaVersion.swift"
HMS_PLUGIN_XML_PATH="$ROOT_PATH/cordova-airship-hms/plugin.xml"

coreVersion=$(node -p "require('$CORE_PACKAGE_PATH').version")
echo "core package version: $coreVersion"

hmsVersion=$(node -p "require('$HMS_PACKAGE_PATH').version")
echo "hms package version: $hmsVersion"

androidVersion=$(grep "var version" $ANDROID_VERISON_PATH | awk -F'"' '{print $2}')
echo "android: $androidVersion"

iosVersion=$(grep "static let version" $IOS_VERISON_PATH | awk -F'"' '{print $2}')
echo "ios: $iosVersion"

hmsDependencyVersion=$(grep '<dependency id="cordova-airship"' $HMS_PLUGIN_XML_PATH | awk -F 'version="' '{print $2}' | awk -F '"' '{print $1}')
echo "hms core dependency: $hmsDependencyVersion"

if [ "$coreVersion" = "$hmsVersion" ] && [ "$coreVersion" = "$androidVersion" ] && [ "$coreVersion" = "$iosVersion" ] && [ "$coreVersion" = "$hmsDependencyVersion" ]; then
    echo "All versions are equal :)"
else
    echo "Version mismatch!"
	exit 1
fi