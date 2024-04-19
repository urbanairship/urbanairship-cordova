#!/bin/bash -ex
VERSION=$1

ROOT_PATH=`dirname "${0}"`/..
CORE_PACKAGE_PATH="$ROOT_PATH/cordova-airship"
HMS_PACKAGE_PATH="$ROOT_PATH/cordova-airship-hms"
ANDROID_VERISON_PATH="$ROOT_PATH/cordova-airship/src/android/AirshipCordovaVersion.kt"
IOS_VERISON_PATH="$ROOT_PATH/cordova-airship/src/ios/AirshipCordovaVersion.swift"
HMS_PLUGIN_XML_PATH="$ROOT_PATH/cordova-airship-hms/plugin.xml"
CORE_PLUGIN_XML_PATH="$ROOT_PATH/cordova-airship/plugin.xml"


if [ -z "$1" ]
  then
    echo "No version number supplied"
    exit
fi


sed -i '' "s/var version = \"[-0-9.a-zA-Z]*\"/var version = \"$VERSION\"/" $ANDROID_VERISON_PATH
sed -i '' "s/static let version = \"[-0-9.a-zA-Z]*\"/static let version = \"$VERSION\"/" $IOS_VERISON_PATH
sed -i '' "s/<plugin id=\"@ua\/cordova-airship\" version=\"[0-9.]*\"/<plugin id=\"@ua\/cordova-airship\" version=\"$VERSION\"/" $CORE_PLUGIN_XML_PATH
sed -i '' "s/<plugin id=\"@ua\/cordova-airship-hms\" version=\"[0-9.]*\"/<plugin id=\"@ua\/cordova-airship-hms\" version=\"$VERSION\"/" $HMS_PLUGIN_XML_PATH
sed -i '' "s/<dependency id=\"@ua\/cordova-airship\" version=\"[0-9.]*\"\/>/<dependency id=\"@ua\/cordova-airship\" version=\"$VERSION\"\/>/" $HMS_PLUGIN_XML_PATH
npm --prefix $CORE_PACKAGE_PATH version $VERSION
npm --prefix $HMS_PACKAGE_PATH version $VERSION