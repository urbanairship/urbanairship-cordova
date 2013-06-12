#!/bin/bash

VERSION="1.0.7"

mkdir -p archive
mkdir -p build/android
mkdir -p build/ios

git archive master | tar -xC archive/

#Copy the README, CHANGELOG and COPYING
cp archive/README.md build/README
cp archive/COPYING build
cp archive/CHANGELOG build

#Copy the sample apps
cp -r archive/android-sample build/android/sample-app
cp -r archive/ios-sample build/ios/sample-app

#Copy android files to android dir
cp archive/android-sample/assets/www/lib/PushNotification.js build/android/
cp archive/android-sample/src/com/urbanairship/phonegap/plugins/PushNotificationPlugin.java build/android/
cp archive/android-sample/src/com/urbanairship/phonegap/sample/IntentReceiver.java build/android/

#Copy IOS files to ios dir.
cp archive/ios-sample/www/lib/PushNotification.js build/ios/
cp -r archive/ios-sample/UAPhonegapSample/Plugins/PushNotificationPlugin build/ios/

#Build release
BUILD_DIR="urbanairship-phonegap-$VERSION"
ln -s build $BUILD_DIR
zip -r $BUILD_DIR.zip $BUILD_DIR
rm $BUILD_DIR

#Remove build dir.
rm -rf build
rm -rf archive
