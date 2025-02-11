#!/bin/bash

#
# run_ci_tasks.sh [OPTIONS] [PATH TO SAMPLE APP]
# where OPTIONS are:
#  -a to run Android CI tasks.
#  -i to run iOS CI tasks.
#  Defaults to -a -i.
#

set -euxo pipefail

SCRIPT_DIRECTORY=`dirname "$0"`
SCRIPT_NAME=`basename "$0"`

# get platforms to build
ANDROID=false
IOS=false

# Parse arguments
OPTS=`getopt haid $*`

if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi
eval set -- "$OPTS"

if [ "$1" == "--" ]; then
  # set the default options
  eval set -- "-a" "-i" $@
fi

while true; do
  case "${1:-}" in
    -h  ) echo -ne "\n${SCRIPT_NAME} [OPTIONS] [PATH TO SAMPLE APP]\nwhere OPTIONS are:\n  -a to run Android CI tasks.\n  -i to run iOS CI tasks.\n  Defaults to -a -i. \n"; exit 0;;
    -a  ) ANDROID=true;;
    -i  ) IOS=true;;
    --  ) ;;
    *   ) break ;;
  esac
  shift
done

# ckeck for version
${SCRIPT_DIRECTORY}/check_plugin_version.sh

SAMPLE_APP_PATH=${1:-}

if [ "$ANDROID" = "true" ] || [ "$IOS" = "true" ]; then
  # create the sample for building
  if [ -z "$SAMPLE_APP_PATH" ]; then
    SAMPLE_APP_PATH=$(mktemp -d /tmp/cordova-sample-app-XXXXX)
  fi

  # if sample app doesn't already exist, create it
  if [[ ! -d $SAMPLE_APP_PATH/test ]]; then
    ${SCRIPT_DIRECTORY}/create_sample.sh $SAMPLE_APP_PATH
  fi

  cd $SAMPLE_APP_PATH/test
fi

if [ "$ANDROID" = "true" ]; then
  # Build android
  npx cordova build android -- --gradleArg="-PuaSkipApplyGoogleServicesPlugin=true" 2>&1 | tee -a /tmp/CORDOVA-$$.out

  # check for failures
  if grep "BUILD FAILED" /tmp/CORDOVA-$$.out; then
    # Set build status to failed
    echo "ANDROID BUILD FAILED"
    exit 1
  else
    echo "ANDROID BUILD SUCCEEDED"
  fi
fi

if [ "$IOS" = "true" ]; then
  npx cordova run ios --list
  
  # Build ios
  npx cordova build ios --target="iPhone-16-Pro, 18.2" --emulator 2>&1 | tee -a /tmp/CORDOVA-$$.out

  # check for failures
  if grep "BUILD FAILED" /tmp/CORDOVA-$$.out; then
    # Set build status to failed
    echo "iOS BUILD FAILED"
    exit 1
  fi

  if grep "Failed to install 'com.urbanairship.cordova'" /tmp/CORDOVA-$$.out; then
      # Set build status to failed
      echo "iOS BUILD FAILED"
      exit 1
  fi

  echo "iOS BUILD SUCCEEDED"

fi

echo "CI TASKS SUCCEEDED"
