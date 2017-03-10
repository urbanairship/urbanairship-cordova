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
sed -i '' "s/\version=\".*\">/\version=\"$VERSION\">/g" plugin.xml
