#!/bin/bash
set -e
set -x

ROOT_PATH=`dirname "${0}"`/..

# Get version from package.json file
packageJSONFilePath="$ROOT_PATH/package.json"
packageJSONVersionRegex='"version": ?"[0-9]+\.[0-9]+\.[0-9]+"'
packageJSONVersion=$(grep -E "$packageJSONVersionRegex" $packageJSONFilePath | cut -f4 -d \")


# Get version from plugin.xml file
pluginXMLFilePath="$ROOT_PATH/plugin.xml"
# Get plugin version
pluginXMLFileVersionRegex='version= ?"[0-9]+\.[0-9]+\.[0-9]+"'
pluginXMLFileVersion=$(grep -E "$pluginXMLFileVersionRegex" $pluginXMLFilePath | cut -f2 -d \")
# Get plugin version for android config
pluginXMLAndroidConfigVersionRegex='android:value= ?"[0-9]+\.[0-9]+\.[0-9]+"'
pluginXMLAndroidConfigVersion=$(grep -E "$pluginXMLAndroidConfigVersionRegex" $pluginXMLFilePath | cut -f2 -d \")
# Get plugin version for iOS config
pluginXMLiOSConfigVersionRegex='<string>[0-9]+\.[0-9]+\.[0-9]+'
pluginXMLiOSConfigVersion=$(grep -E "$pluginXMLiOSConfigVersionRegex" $pluginXMLFilePath | cut -f2 -d \> | cut -f1 -d \<)

if [ "$pluginXMLFileVersion" != "$packageJSONVersion" ] || [ "$pluginXMLAndroidConfigVersion" != "$packageJSONVersion" ] || [ "$pluginXMLiOSConfigVersion" != "$packageJSONVersion" ]; then
	echo "BUILD FAILED: The plugin version is not the same everywhere (please check package.json and plugin.xml files)"
	exit 1
fi