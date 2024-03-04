#!/bin/bash
set -e
set -x

ROOT_PATH=`dirname "${0}"`/..

version=$(node -p "require('$ROOT_PATH/cordova-airship/package.json').version")

if [ $1 = $version ]; then
 exit 0
else
 exit 1
fi