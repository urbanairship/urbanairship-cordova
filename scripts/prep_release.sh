#!/bin/bash
set -e

# Unified release preparation script for Cordova Airship
# Combines: update_version.sh, update_proxy_version.sh, update_changelog.sh
# Usage: ./prep_release.sh [--dry-run]
# Env vars: PLUGIN_VERSION, PROXY_VERSION, IOS_VERSION, ANDROID_VERSION, GEMINI_API_KEY

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Parse arguments
DRY_RUN=false
if [ "$1" = "--dry-run" ]; then
    DRY_RUN=true
    echo "🔍 DRY RUN MODE - No changes will be made"
    echo ""
fi

# Required env vars
if [ -z "$PLUGIN_VERSION" ]; then
    echo "Error: PLUGIN_VERSION environment variable required"
    exit 1
fi

# Optional env vars
PROXY_VERSION="${PROXY_VERSION:-}"
IOS_VERSION="${IOS_VERSION:-}"
ANDROID_VERSION="${ANDROID_VERSION:-}"
GEMINI_API_KEY="${GEMINI_API_KEY:-}"

echo "Cordova Airship Release Preparation"
echo "===================================="
echo "Plugin version: $PLUGIN_VERSION"
echo "Proxy version:  ${PROXY_VERSION:-not specified}"
echo "iOS SDK:        ${IOS_VERSION:-not specified}"
echo "Android SDK:    ${ANDROID_VERSION:-not specified}"
echo ""

# Function to polish changelog with Gemini (graceful degradation)
polish_changelog() {
    local changelog_text="$1"

    if [ -z "$GEMINI_API_KEY" ]; then
        echo "$changelog_text"
        return 0
    fi

    local prompt="Polish this Cordova Airship changelog entry. Keep it concise and professional.

Changelog:
$changelog_text

Return ONLY the polished changelog text."

    local response
    response=$(curl -s -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$GEMINI_API_KEY" \
        -H "Content-Type: application/json" \
        -d "{
            \"contents\": [{
                \"parts\": [{\"text\": \"$(echo "$prompt" | sed 's/"/\\"/g' | tr '\n' ' ')\"}]
            }],
            \"generationConfig\": {
                \"temperature\": 0.3,
                \"maxOutputTokens\": 1024
            }
        }" 2>&1)

    if echo "$response" | grep -q "error"; then
        echo "$changelog_text"
        return 0
    fi

    local polished
    polished=$(echo "$response" | grep -o '"text"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/"text"[[:space:]]*:[[:space:]]*"//' | sed 's/"$//' | head -1)

    if [ -n "$polished" ]; then
        echo "$polished"
    else
        echo "$changelog_text"
    fi
}

# File paths
CORE_PACKAGE_PATH="$REPO_ROOT/cordova-airship"
HMS_PACKAGE_PATH="$REPO_ROOT/cordova-airship-hms"
ANDROID_VERSION_PATH="$REPO_ROOT/cordova-airship/src/android/AirshipCordovaVersion.kt"
IOS_VERSION_PATH="$REPO_ROOT/cordova-airship/src/ios/AirshipCordovaVersion.swift"
HMS_PLUGIN_XML_PATH="$REPO_ROOT/cordova-airship-hms/plugin.xml"
CORE_PLUGIN_XML_PATH="$REPO_ROOT/cordova-airship/plugin.xml"

# Step 1: Update plugin version
echo "[1/4] Updating plugin version..."
if [ "$DRY_RUN" = "false" ]; then
    sed -i '' "s/var version = \"[-0-9.a-zA-Z]*\"/var version = \"$PLUGIN_VERSION\"/" "$ANDROID_VERSION_PATH"
    sed -i '' "s/static let version = \"[-0-9.a-zA-Z]*\"/static let version = \"$PLUGIN_VERSION\"/" "$IOS_VERSION_PATH"
    sed -i '' "s/<plugin id=\"@ua\/cordova-airship\" version=\"[0-9.]*\"/<plugin id=\"@ua\/cordova-airship\" version=\"$PLUGIN_VERSION\"/" "$CORE_PLUGIN_XML_PATH"
    sed -i '' "s/<plugin id=\"@ua\/cordova-airship-hms\" version=\"[0-9.]*\"/<plugin id=\"@ua\/cordova-airship-hms\" version=\"$PLUGIN_VERSION\"/" "$HMS_PLUGIN_XML_PATH"
    sed -i '' "s/<dependency id=\"@ua\/cordova-airship\" version=\"[0-9.]*\"\/>/<dependency id=\"@ua\/cordova-airship\" version=\"$PLUGIN_VERSION\"\/>/" "$HMS_PLUGIN_XML_PATH"
    npm --prefix "$CORE_PACKAGE_PATH" version "$PLUGIN_VERSION" --no-git-tag-version
    npm --prefix "$HMS_PACKAGE_PATH" version "$PLUGIN_VERSION" --no-git-tag-version
    echo "  ✓ Updated version files and package.json"
else
    echo "  Would update:"
    echo "    - $ANDROID_VERSION_PATH"
    echo "    - $IOS_VERSION_PATH"
    echo "    - $CORE_PLUGIN_XML_PATH"
    echo "    - $HMS_PLUGIN_XML_PATH"
    echo "    - $CORE_PACKAGE_PATH/package.json"
    echo "    - $HMS_PACKAGE_PATH/package.json"
fi

# Step 2: Update proxy version (if specified)
if [ -n "$PROXY_VERSION" ]; then
    echo "[2/4] Updating proxy version..."
    if [ "$DRY_RUN" = "false" ]; then
        sed -i '' -E "s/(pod name=\"AirshipFrameworkProxy\" spec=\")[^\"]*\"/\1$PROXY_VERSION\"/" "$CORE_PLUGIN_XML_PATH"
        sed -i '' -E "s/(api \"com.urbanairship.android:airship-framework-proxy:)[^\"]*\"/\1$PROXY_VERSION\"/" "$REPO_ROOT/cordova-airship/src/android/build-extras.gradle"
        sed -i '' -E "s/(implementation \"com.urbanairship.android:airship-framework-proxy-hms:)[^\"]*\"/\1$PROXY_VERSION\"/" "$REPO_ROOT/cordova-airship-hms/src/android/build-extras.gradle"
        echo "  ✓ Updated plugin.xml and build-extras.gradle"
    else
        echo "  Would update proxy version to $PROXY_VERSION"
    fi
else
    echo "[2/4] Skipping proxy version update (not specified)"
fi

# Step 3: Generate changelog
echo "[3/4] Generating changelog..."

RELEASE_DATE=$(date +"%B %-d, %Y")

# Determine release type
if [[ $PLUGIN_VERSION =~ \.0\.0$ ]]; then
    RELEASE_TYPE="Major"
elif [[ $PLUGIN_VERSION =~ \.0$ ]]; then
    RELEASE_TYPE="Minor"
else
    RELEASE_TYPE="Patch"
fi

# Build changelog entry
NEW_ENTRY="## Version $PLUGIN_VERSION - $RELEASE_DATE\n\n"

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

# Polish with Gemini if available
if [ "$DRY_RUN" = "false" ]; then
    POLISHED_ENTRY=$(polish_changelog "$(echo -e "$NEW_ENTRY")")

    # Update CHANGELOG.md
    TEMP_FILE=$(mktemp)
    echo "# Cordova Plugin Changelog" > "$TEMP_FILE"
    echo -e "\n$POLISHED_ENTRY" >> "$TEMP_FILE"
    tail -n +2 "$REPO_ROOT/CHANGELOG.md" >> "$TEMP_FILE"
    mv "$TEMP_FILE" "$REPO_ROOT/CHANGELOG.md"
    echo "  ✓ Updated CHANGELOG.md"
else
    echo "  Would add to CHANGELOG.md:"
    echo -e "$NEW_ENTRY" | sed 's/^/    /'
fi

# Step 4: Validation
echo "[4/4] Validation..."
if [ "$DRY_RUN" = "false" ]; then
    # Verify changes
    if grep -q "var version = \"$PLUGIN_VERSION\"" "$ANDROID_VERSION_PATH"; then
        echo "  ✓ Android version verified"
    else
        echo "  ✗ Android version mismatch"
        exit 1
    fi

    if grep -q "static let version = \"$PLUGIN_VERSION\"" "$IOS_VERSION_PATH"; then
        echo "  ✓ iOS version verified"
    else
        echo "  ✗ iOS version mismatch"
        exit 1
    fi

    if [ -n "$PROXY_VERSION" ]; then
        if grep -q "pod name=\"AirshipFrameworkProxy\" spec=\"$PROXY_VERSION\"" "$CORE_PLUGIN_XML_PATH"; then
            echo "  ✓ Proxy version verified"
        else
            echo "  ✗ Proxy version mismatch"
            exit 1
        fi
    fi

    echo ""
    echo "✅ Release preparation complete!"
    echo ""
    echo "Next steps:"
    echo "1. Review changes: git diff"
    echo "2. Commit changes"
    echo "3. Create PR"
else
    echo "  Dry-run complete - no changes made"
fi
