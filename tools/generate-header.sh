#!/bin/bash -ex

ROOT=`dirname "${0}"`/..
AIRSHIP=$ROOT/src/ios/Airship

HEADERS=`find $AIRSHIP -name '*.h'`;

for file in $HEADERS
do 
    echo "<header-file src=\"${file#$ROOT/}\" />"
done