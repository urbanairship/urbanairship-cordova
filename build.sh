#Do this from a fresh checkout so there are no generated files.

VERSION="1.0.1"

mkdir -p build/android
mkdir -p build/ios

#Copy the README and COPYING
cp README.md build/README
cp COPYING build

#Copy the sample apps
cp -r android-sample build/android/sample-app
cp -r ios-sample build/ios/sample-app

#Copy android files to android dir
cp android-sample/assets/www/lib/PushNotification.js build/android/
cp android-sample/src/com/urbanairship/phonegap/plugins/PushNotificationPlugin.java build/android/
cp android-sample/src/com/urbanairship/phonegap/sample/IntentReceiver.java build/android/

#Copy IOS files to ios dir.
cp ios-sample/www/lib/PushNotification.js build/ios/
cp -r ios-sample/UAPhonegapSample/Plugins/PushNotificationPlugin build/ios/

#Build release
BUILD_DIR="urbanairship-phonegap-$VERSION"
ln -s build $BUILD_DIR
zip -r $BUILD_DIR.zip $BUILD_DIR
rm $BUILD_DIR

#Remove build dir.
rm -rf build
