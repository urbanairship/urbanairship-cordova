#!/bin/bash -ex

# Set up script for iOS and android testing.
# Follow the steps below.
# 1. Add the UA credentials to the `config_sample.xml` file in the test directory and save.
# 2. Run the script with the command `./test_sample.sh `
# 3. Build the platform you want to test (see comments below).

# keep cordova up to date
npm install cordova -g

# remove any previous test
rm -rf test

# create the test project
cordova create test com.urbanairship.sample Test

cd test

# add the plugin
cordova plugin add ../../

# copy config and example files
cp ../config_sample.xml config.xml
cp ../../Example/index.html www/index.html
cp ../../Example/css/* www/css
cp ../../Example/js/* www/js

# add the device plugin
cordova plugin add cordova-plugin-device

# set up iOS
cordova platform add ios
cd platforms/ios/
pod update
cd ../..
# Build with command `cordova build ios` in test directory
# After successful build, connect iOS device to test
# Open workspace to test with command `open platforms/ios/Test.xcworkspace`

# set up android
cordova platform add android
# Build with command `cordova build android` in test directory
# After successful build, connect android device to test
# Test with command `cordova run android`
