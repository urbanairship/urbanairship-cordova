#!/bin/bash
set -e
set -x

SCRIPT_DIRECTORY="$(dirname "$0")"
ROOT_PATH=$SCRIPT_DIRECTORY/../

# First argument is always the version
VERSION=$1
shift

# Process remaining arguments as named parameters
while [[ $# -gt 0 ]]; do
  case $1 in
    --ios)
      IOS_VERSION="$2"
      shift 2
      ;;
    --android)
      ANDROID_VERSION="$2"
      shift 2
      ;;
    *)
      echo "Unknown parameter: $1"
      exit 1
      ;;
  esac
done

if [ -z "$VERSION" ]; then
    echo "Error: Version is required"
    exit 1
fi

RELEASE_DATE=$(date +"%B %-d, %Y")

# Determine release type based on version
if [[ $VERSION =~ \.0\.0$ ]]; then
    RELEASE_TYPE="Major"
elif [[ $VERSION =~ \.0$ ]]; then
    RELEASE_TYPE="Minor"
else
    RELEASE_TYPE="Patch"
fi

# Create changelog entry
NEW_ENTRY="## Version $VERSION - $RELEASE_DATE\n\n"

if [ -n "$IOS_VERSION" ] || [ -n "$ANDROID_VERSION" ]; then
    NEW_ENTRY+="$RELEASE_TYPE release that updates"

    if [ -n "$ANDROID_VERSION" ]; then
        NEW_ENTRY+=" the Android SDK to $ANDROID_VERSION"
    fi

    if [ -n "$IOS_VERSION" ] && [ -n "$ANDROID_VERSION" ]; then
        NEW_ENTRY+=" and"
    fi

    if [ -n "$IOS_VERSION" ]; then
        NEW_ENTRY+=" the iOS SDK to $IOS_VERSION"
    fi

    NEW_ENTRY+="\n\n### Changes\n"

    if [ -n "$ANDROID_VERSION" ]; then
        NEW_ENTRY+="- Updated Android SDK to [$ANDROID_VERSION](https://github.com/urbanairship/android-library/releases/tag/$ANDROID_VERSION)"
    fi

    if [ -n "$IOS_VERSION" ]; then
        NEW_ENTRY+="\n"
        NEW_ENTRY+="- Updated iOS SDK to [$IOS_VERSION](https://github.com/urbanairship/ios-library/releases/tag/$IOS_VERSION)"
    fi
else
    NEW_ENTRY+="$RELEASE_TYPE release."
fi

NEW_ENTRY+="\n"

# Create temporary file with new content
TEMP_FILE=$(mktemp)

# Add the header line
echo "# Cordova Plugin Changelog" > "$TEMP_FILE"
echo -e "\n$NEW_ENTRY" >> "$TEMP_FILE"

# Append the rest of the existing changelog (skipping the header)
tail -n +2 "$ROOT_PATH/CHANGELOG.md" >> "$TEMP_FILE"

# Replace original file with new content
mv "$TEMP_FILE" "$ROOT_PATH/CHANGELOG.md"
