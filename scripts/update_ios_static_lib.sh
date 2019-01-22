#!/bin/bash

# Parse framework version line from plugin and pull out the version number
VERSION=$(xpath plugin.xml '/plugin/platform[@name="ios"]/source-file[@framework="true"]' 2>/dev/null |
grep -oh '[0-9]\+\.[0-9]\+\.[0-9]\+')

# Don't copy static library if it already exists
if [[ -f src/ios/Airship/libUAirship-$VERSION.a ]]; then 
  echo "libUAirship-$VERSION has already been downloaded"
  exit
fi

echo "Downloading libUAirship-$VERSION.zip from bintray..."
curl -s -LO "https://urbanairship.bintray.com/iOS/urbanairship-sdk/$VERSION/libUAirship-$VERSION.zip"
echo "Unzipping libUAirship into temp directory..."
unzip -q -d temp libUAirship-$VERSION.zip
echo "Making room for Airship directory in src/ios/..."
rm -rf src/ios/Airship
echo "Moving Airship directory to src/ios/..."
mv -f temp/Airship src/ios/
echo "Cleaning up..."
rm -rf temp
rm -rf libUAirship-$VERSION.zip
echo "Done"
