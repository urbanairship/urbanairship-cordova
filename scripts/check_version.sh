#!/bin/bash
set -e
set -x

ROOT_PATH=`dirname "${0}"`/..

VERSION=$(node -p "require('$ROOT_PATH/package.json').version")

if [ $1 = $VERSION ]; then
 exit 0
else
 exit 1
fi