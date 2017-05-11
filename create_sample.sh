#!/bin/bash -ex
ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

CORDOVA_PATH=$1

if [ -z "$1" ]
  then
    echo "No test path supplied"
    exit
fi

# Set up script to create a sample for iOS and android testing.
# Follow the steps below.
# 1. Add the UA credentials to the `config_sample.xml` file in the test directory and save.
# 2. Run the script with the command `./create_sample.sh `
# 3. Build the platform you want to test (see comments below).

# keep cordova up to date
npm install cordova -g

# create cordova directory
mkdir $CORDOVA_PATH
cd $CORDOVA_PATH

# create the project
PROJECT_NAME="Test"
PROJECT_NAME_LC=`echo ${PROJECT_NAME} | tr  '[A-Z]' '[a-z]'`
PROJECT_NAME_UC=`echo ${PROJECT_NAME_LC:0:1} | tr  '[a-z]' '[A-Z]'`${PROJECT_NAME_LC:1}
cordova create $PROJECT_NAME_LC com.urbanairship.sample $PROJECT_NAME_UC

cd $PROJECT_NAME_LC

PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# add the plugin
cordova plugin add $ROOT_DIR

# copy config and example files
cp $ROOT_DIR/config_sample.xml config.xml
cp $ROOT_DIR/Example/index.html www/index.html
cp $ROOT_DIR/Example/css/* www/css
cp $ROOT_DIR/Example/js/* www/js

# add the device plugin
cordova plugin add cordova-plugin-device

# set up iOS
cordova platform add ios

IOS_PLATFORM_DIR="$PROJECT_DIR/platforms/ios/"
cd $IOS_PLATFORM_DIR
pod update

# copy build.json
if [[ -f $ROOT_DIR/build.json ]]; then
    cp $ROOT_DIR/build.json $IOS_PLATFORM_DIR/build.json
else
    cp $ROOT_DIR/build_sample.json $IOS_PLATFORM_DIR/build.json
fi

# Build with command `cordova build ios --buildConfig=platforms/ios/build.json` in project directory
# After successful build, connect iOS device to test
# Test with command `cordova run ios --device --buildConfig=platforms/ios/build.json`
# Open workspace in Xcode with 'open' command, e.g. `open platforms/ios/Test.xcworkspace`

# set up android
cd $PROJECT_DIR
cordova platform add android

# Build with command `cordova build android` in project directory
# After successful build, connect android device to test
# Test with command `cordova run android`
