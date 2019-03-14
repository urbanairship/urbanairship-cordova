#!/bin/bash

cd `dirname "${0}"`/../
ROOT_PATH="$(pwd)"
cd -

CORDOVA_PATH=$1

if [ -z "$1" ]
then
echo "No test path supplied"
exit
fi

# Parse framework version line from plugin and pull out the version number
VERSION=$(xpath $ROOT_PATH/plugin.xml '/plugin/platform[@name="ios"]/source-file[@framework="true"]' 2>/dev/null |
grep -oh '[0-9]\+\.[0-9]\+\.[0-9]\+')

# Don't copy static library if it already exists
if [[ -f src/ios/Airship/libUAirship-$VERSION.a ]]; then 
  echo "libUAirship-$VERSION has already been downloaded"
  exit
fi

echo "Downloading libUAirship-$VERSION.zip from bintray..."
curl -s -LO "https://urbanairship.bintray.com/iOS/urbanairship-sdk/$VERSION/libUAirship-$VERSION.zip"
echo "Unzipping libUAirship into temp directory..."
unzip -q -d $ROOT_PATH/temp $ROOT_PATH/libUAirship-$VERSION.zip
echo "Making room for Airship directory in src/ios/..."
rm -rf $ROOT_PATH/src/ios/Airship
echo "Moving Airship directory to src/ios/..."
mv -f $ROOT_PATH/temp/Airship $ROOT_PATH/src/ios/
echo "Cleaning up..."
rm -rf $ROOT_PATH/temp
rm -rf $ROOT_PATH/libUAirship-$VERSION.zip
echo "Done"
