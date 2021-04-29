#!/bin/bash

set -euxo pipefail

cd `dirname "${0}"`/../
ROOT_PATH="$(pwd)"
CORDOVA_AIRSHIP_MODULE="$ROOT_PATH/urbanairship-cordova"
CORDOVA_AIRSHIP_ACCENGAGE_MODULE="$ROOT_PATH/urbanairship-accengage-cordova"
CORDOVA_AIRSHIP_HMS_MODULE="$ROOT_PATH/urbanairship-hms-cordova"
cd -

source "$ROOT_PATH/scripts/config.sh"

CORDOVA_PATH=$1

if [ -z "$1" ]
  then
    echo "No test path supplied"
    exit
fi

# Set up script to create a sample for iOS and android testing.
# Follow the steps below.
# 1. Add the UA credentials to the `config_sample.xml` file and save.
# 2. Run the script with the command `./scripts/create_sample.sh `
# 3. Build the platform you want to test (see comments below).

# create cordova directory
mkdir -p $CORDOVA_PATH
cd $CORDOVA_PATH

# create the test project
rm -rf test
npm install cordova@$CORDOVA_VERSION
npx cordova create test com.urbanairship.sample Test
cd test
npm install cordova@$CORDOVA_VERSION

# add the plugins
npx cordova plugin add $CORDOVA_AIRSHIP_MODULE
npx cordova plugin add $CORDOVA_AIRSHIP_ACCENGAGE_MODULE
npx cordova plugin add $CORDOVA_AIRSHIP_HMS_MODULE

# copy config and example files
cp "$ROOT_PATH/config_sample.xml" config.xml
cp "$ROOT_PATH/Example/index.html" www/index.html
cp "$ROOT_PATH/Example/css/"* www/css
cp "$ROOT_PATH/Example/js/"* www/js

# copy mock google-services.json
cp "$ROOT_PATH/scripts/mock-google-services.json"  google-services.json


# add required plugins
npx cordova plugin add cordova-plugin-device

# set up iOS
npx cordova platform add ios@$IOS_CORDOVA_VERSION

# Build with command `cordova build ios --emulator` in project directory
# After successful build, connect iOS device to test
# Test with command `cordova run ios --device --developmentTeam=XXXXXXXXXX`
#   Please refer to https://cordova.apache.org/docs/en/latest/guide/platforms/ios/#signing-an-app for more information about code signing.

# Open workspace in Xcode with 'open' command, e.g. `open platforms/ios/Test.xcworkspace`

# set up android
npx cordova platform add android@$ANDROID_CORDOVA_VERSION

# Build with command `cordova build android` in project directory
# After successful build, connect android device to test
# Test with command `cordova run android`
