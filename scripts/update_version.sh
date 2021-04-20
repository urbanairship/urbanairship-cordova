#!/bin/bash -ex
VERSION=$1

if [ -z "$1" ]
  then
    echo "No version number supplied"
    exit
fi

BASE_MODULE=urbanairship-cordova
SUBMODULES=(
  urbanairship-accengage-cordova
  urbanairship-hms-cordova
)

for MODULE in ${BASE_MODULE} ${SUBMODULES[@]}
do
  # Updates the module version in package.json
  sed -i '' "s/\version\": \".*\",/\version\": \"$VERSION\",/g" ${MODULE}/package.json

  # Updates the module version in plugin.xml
  sed -E -i '' "s/\version=\"[0-9]+\.[0-9]+\.[0-9]+.*\"/\version=\"$VERSION\"/g" ${MODULE}/plugin.xml
  sed -E -i '' "s/\android:value=\"[0-9]+\.[0-9]+\.[0-9]+.*\"/\android:value=\"$VERSION\"/g" ${MODULE}/plugin.xml
  sed -E -i '' "s/\<string>[0-9]+\.[0-9]+\.[0-9]+.*<\/string>/\<string>$VERSION<\/string>/g" ${MODULE}/plugin.xml
done

for MODULE in ${SUBMODULES[@]}
do
  # Updates the dependency version in plugin.xml
  sed -E -i '' "s/\<dependency id=\"${BASE_MODULE}\" version=\".*[0-9]+\.[0-9]+\.[0-9]+.*\"/<dependency id=\"${BASE_MODULE}\" version=\"$VERSION\"/g" ${MODULE}/plugin.xml
done
