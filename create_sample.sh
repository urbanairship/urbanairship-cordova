#!/bin/bash -ex
ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

TEST_PATH=$1

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

# create test directory
mkdir $TEST_PATH
cd $TEST_PATH

# create the test project
cordova create test com.urbanairship.sample Test
cd test

TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

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
cd $TEST_DIR/platforms/ios/
pod update
# Build with command `cordova build ios` in test directory
# After successful build, connect iOS device to test
# Open workspace to test with command `open platforms/ios/Test.xcworkspace`

# set up android
cd $TEST_DIR
cordova platform add android
# Build with command `cordova build android` in test directory
# After successful build, connect android device to test
# Test with command `cordova run android`
