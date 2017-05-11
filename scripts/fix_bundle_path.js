#!/usr/bin/env node

var fs = require('fs')
var readline = require('readline')
var path = require('path')

module.exports = function (context) {  
    if (context.opts.platforms.indexOf('ios') === -1) {
        return
    }
    const changeBlockHeader = "# BEGIN URBANAIRSHIP CHANGES FOR CORDOVA BUILD"
    const changeBlockFooter = "# END URBANAIRSHIP CHANGES FOR CORDOVA BUILD"
    const beginningOfInstallResourceBlock = "if [[ \"$CONFIGURATION\" == \"Debug\" ]]; then"
    const endOfInstallResourcesBlock = "fi"

    const defaultResourceBundlePath = "$PODS_CONFIGURATION_BUILD_DIR/UrbanAirship-iOS-SDK"
    const newResourceBundlePath = "$RESOURCE_BUNDLE_DIR"

    const codeToInsert = `
# look in two different directories for the resource bundle
# Xcode build puts the resource bundle here
RESOURCE_BUNDLE_DIR="` + defaultResourceBundlePath + `"
TRIED_RESOURCE_BUNDLE_DIRS="` + newResourceBundlePath + `"
if [[ ! -d "` + newResourceBundlePath + `/AirshipResources.bundle" ]]; then
  # Cordova build puts the resource bundle here
  RESOURCE_BUNDLE_DIR="$CONFIGURATION_BUILD_DIR"
  TRIED_RESOURCE_BUNDLE_DIRS="$TRIED_RESOURCE_BUNDLE_DIRS or ` + newResourceBundlePath + `"
fi
if [[ ! -d "` + newResourceBundlePath + `/AirshipResources.bundle" ]]; then
  echo "Warning: Could not find AirshipResources.bundle in either $TRIED_RESOURCE_BUNDLE_DIRS"
fi
`
    const installResourceCommand = "  install_resource \""+defaultResourceBundlePath+"/AirshipResources.bundle\""
 
    var rootPath = context.opts.projectRoot
    var configXmlPath = path.join(rootPath, 'config.xml')
    var configParser = getConfigParser(context, configXmlPath)
    var appName = configParser.name() 
 
    var podsResourcesScriptPath = 'platforms/ios/Pods/Target Support Files/Pods-' + appName + '/Pods-' + appName + '-resources.sh'

    var temporaryFileName = podsResourcesScriptPath + ".tmp"

    var fileAlreadyFixed = false
    var inConfigBlock = false
    var foundConfigBlock = false
    var numberOfInstallCommandsReplaced = 0
    var finishedModifications = false
 
    // copy to a temporary file
    var newFile = fs.openSync(temporaryFileName,'w')

    // reading the pods resources file")
    var lines = fs.readFileSync(podsResourcesScriptPath, 'utf-8')
        .split('\n')
    if (! lines) {
        console.log("ERROR: Unable to read " + podsResourcesScriptPath)
    }

    for (lineNumber in lines) {
        line = lines[lineNumber]
        if (!finishedModifications) {
            if (line.lastIndexOf(changeBlockHeader, 0) === 0) {
                fileAlreadyFixed = true
                break
            }
            if (line.lastIndexOf(beginningOfInstallResourceBlock, 0) === 0) {
                if (foundConfigBlock) {
                    console.log('Warning: Already found config block')
                } else {
                    fs.writeSync(newFile,changeBlockHeader + '\n')
                    fs.writeSync(newFile,codeToInsert + '\n')
                }
                foundConfigBlock = true
                inConfigBlock = true
            } else if (line.lastIndexOf(installResourceCommand, 0) === 0) {
                line = line.replace(defaultResourceBundlePath,newResourceBundlePath)
                numberOfInstallCommandsReplaced++
            }
        }

        fs.writeSync(newFile,line + '\n')

        if (!finishedModifications) {
            if (line.lastIndexOf(endOfInstallResourcesBlock,0) === 0) {
                if (inConfigBlock && (numberOfInstallCommandsReplaced == 2)) {
                    fs.writeSync(newFile,changeBlockFooter + '\n')
                    inConfigBlock = false
                    finishedModifications = true
                }
            }       
        }
    }

    fs.closeSync(newFile)
    if (fileAlreadyFixed) {
        fs.unlink(temporaryFileName)
    } else {
        fs.chmodSync(temporaryFileName,fs.statSync(podsResourcesScriptPath).mode)
        fs.rename(temporaryFileName,podsResourcesScriptPath, function(err) {
            if ( err ) {
                console.log('Error renaming ' + temporaryFileName + ' to ' + podsResourcesScriptPath + '\nError: ' + err)
            }
        })
    }

    function getConfigParser(context, config) {
        var semver = context.requireCordovaModule('semver')
        var ConfigParser

        if (semver.lt(context.opts.cordova.version, '5.4.0')) {
            ConfigParser = context.requireCordovaModule('cordova-lib/src/ConfigParser/ConfigParser')
        } else {
            ConfigParser = context.requireCordovaModule('cordova-common/src/ConfigParser/ConfigParser')
        }

        return new ConfigParser(config)
    }
}

