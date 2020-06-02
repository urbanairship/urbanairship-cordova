#!/bin/bash -ex
VERSION=$1

if [ -z "$1" ]
  then
    echo "No version number supplied"
    exit
fi

# Updates the urbanairship-cordova version in package.json
sed -i '' "s/\version\": \".*\",/\version\": \"$VERSION\",/g" package.json

# Updates the urbanairship-cordova version in plugin.xml
sed -E -i '' "s/\version=\"[0-9]+\.[0-9]+\.[0-9]+.*\"/\version=\"$VERSION\"/g" plugin.xml
sed -E -i '' "s/\android:value=\"[0-9]+\.[0-9]+\.[0-9]+.*\"/\android:value=\"$VERSION\"/g" plugin.xml
sed -E -i '' "s/\<string>[0-9]+\.[0-9]+\.[0-9]+.*<\/string>/\<string>$VERSION<\/string>/g" plugin.xml

# Update packages
npm install