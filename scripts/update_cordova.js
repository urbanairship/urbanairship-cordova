#!/usr/bin/env node

var co = require('co'),
    prompt = require('co-prompt'),
    program = require('commander'),
    fs = require('fs'),
    admZip = require('adm-zip'),
    request = require('superagent'),
    semver = require('semver');

const pluginXMLVersionRegex = /version=\"[0-9]+\.[0-9]+\.[0-9]+\"/g;
const pluginJSONVersionRegex = /"version": ?"[0-9]+\.[0-9]+\.[0-9]+"/g;
const pluginHeadersRegex = /.*<header-file src="src\/ios\/Airship\/Headers\/.*\.h\" ?\/> ?\n/g;
const pluginiOSLibRegex = /<source-file framework="true" src="src\/ios\/Airship\/libUAirship-[0-9]+\.[0-9]+\.[0-9]+.a"\/>/g;
const buildExtrasRegex = /urbanairship-fcm:[0-9]+\.[0-9]+\.[0-9]+'/g;

const pluginXMLFilePath = `${__dirname}/../plugin.xml`
const packageJSONFilePath = `${__dirname}/../package.json`
const buildExtrasFilePath = `${__dirname}/../src/android/build-extras.gradle`

 program
    .arguments('<Plugin version>')
    .option('-a, --android <Android version>', 'The UA Android SDK version to update to.')
    .option('-i, --ios <iOS version>', 'The UA iOS SDK version to update to.')
    .action(function(pluginVersion) {
            // This section will only be entered if arguments are included
            console.log('\nUpdating plugin version to: %s\n', pluginVersion)
            replacePluginVersion(pluginVersion, pluginXMLFilePath)
            replacePluginVersionJSON(pluginVersion, packageJSONFilePath)

            // Use pluginValue to gate the interactive process
            pluginValue = pluginVersion

            if (program.android) {
                console.log('\nUpdating Android SDK version to: %s\n', program.android)
                replaceAndroidLibVersion(program.android, buildExtrasFilePath)
            }
    
            if (program.ios) {
                console.log('\nUpdating iOS SDK version to: %s\n', program.ios)
                replaceiOSLibVersion(program.ios, pluginXMLFilePath)
                replaceHeaders(program.ios, pluginXMLFilePath)
            }
    })
    .parse(process.argv);
    
    // Use pluginValue to gate the interactive process
    if (typeof pluginValue === 'undefined') {
        co(function *() {              
            var pluginVersion = yield prompt('\nWhat is the new version of the plugin?');
            pluginVersion = semver.valid(pluginVersion)

            if (pluginVersion === null || pluginVersion === 'undefined' || pluginVersion === "") {
                console.error('\nValid plugin version is required, exiting.');
                process.exit(1);
            }
            console.log('\nUpdating plugin version to: %s', pluginVersion)
            replacePluginVersion(pluginVersion, pluginXMLFilePath)
            replacePluginVersionJSON(pluginVersion, packageJSONFilePath)

            var androidVerson = yield prompt('\nWhat Android SDK version should this version use? (leave blank if no update is required): ');
            androidVerson = semver.valid(androidVerson)

            console.log(androidVerson)
            if (androidVerson === null || androidVerson === 'undefined' || androidVerson === "") {
                console.log('\nSkipping Android SDK update, no valid version provided')
            } else {
                console.log('\nUpdating Android SDK version to: %s', androidVerson)
                replaceAndroidLibVersion(androidVerson, buildExtrasFilePath)
            }
            
            var iOSVersion = yield prompt('\nWhat iOS Plugin version should this version use? (leave blank if no update is required): ');
            iOSVersion = semver.valid(iOSVersion)

            if (iOSVersion === null || iOSVersion === 'undefined' || iOSVersion === "") {
                console.log('\nSkipping iOS SDK update, no valid version provided')
            } else {
                console.log('\nUpdating iOS SDK version to: %s', iOSVersion)
                replaceHeaders(iOSVersion, pluginXMLFilePath)
                replaceiOSLibVersion(iOSVersion, pluginXMLFilePath)
            }
        });
    }

function replacePluginVersion(version, filePath) {
    const replacementString = `version="${version}"`
    regexReplace(pluginXMLVersionRegex, replacementString, filePath)
}

function replacePluginVersionJSON(version, filePath) {
    const replacementString = `"version": "${version}"`
    regexReplace(pluginJSONVersionRegex, replacementString, filePath)
}

function replaceAndroidLibVersion(version, filePath) {
    const replacementString = `urbanairship-fcm:${version}'`
    regexReplace(buildExtrasRegex, replacementString, filePath)
}

function replaceiOSLibVersion(version, filePath) {
    const replacementString = `<source-file framework="true" src="src/ios/Airship/libUAirship-${version}.a"/>`
    regexReplace(pluginiOSLibRegex, replacementString, filePath)
}

// Generates a xml-compatible string of iOS headers
function replaceHeaders(version, filePath) {
    const zipOutputPath = `${__dirname}/libUAirship-${version}`
    const headerPath = `${zipOutputPath}/Airship/Headers`

    var zipPathURL = `https://urbanairship.bintray.com/iOS/urbanairship-sdk/${version}/libUAirship-${version}.zip`

    const zipFile = `${zipOutputPath}.zip`;
    
    console.log('zip url', zipPathURL);
    console.log('extract entry to directory', zipOutputPath);

    request
      .get(zipPathURL)
      .on('error', function(error) {
        console.log(error);
      })
      .pipe(fs.createWriteStream(zipFile))
      .on('finish', function() {
        console.log('finished dowloading');
        var zip = new admZip(zipFile);
        console.log('started unzip');

        zip.extractAllTo(zipOutputPath, true);
        console.log('finished unzip');

        var headerList = fs.readdirSync(headerPath)

        var headerString = '';
        headerList.forEach(function(header) {
            headerString = (`${headerString}        <header-file src="src/ios/Airship/Headers/${header}" />\n`)
        });

        var data = fs.readFileSync(filePath).toString()
        var matches = data.match(pluginHeadersRegex)
        var result = data

        if (matches != null) {
            for (i = 0; i < matches.length; i++) {
                if (i < (matches.length - 1)) {
                    // Delete matches
                    result = result.replace(matches[i], "");
                } else {
                    // Replace the final match with the header string
                    result = result.replace(matches[i], headerString);
                }
            }
        } else {
            console.log("No header matches found - check the header regex")
        }  

        fs.writeFileSync(filePath, result)

        console.log('Cleaning up...');
        
        // Clean up zip and temp folder
        fs.unlinkSync(zipFile);
        deleteFolderRecursive(zipOutputPath)
        console.log('iOS header update complete');

        console.log('\nCordova update complete');
        process.exit(1);
      });
}

// Helper for replacing single regex matches with a replacement string
function regexReplace(regex, replacementString, filePath) {
    var data = fs.readFileSync(filePath).toString()
    var result = data.replace(regex, replacementString);
    fs.writeFileSync(filePath, result)
}

// Helper for deleting a non-empty folder recursively
var deleteFolderRecursive = function(path) {
    if (fs.existsSync(path)) {
      fs.readdirSync(path).forEach(function(file, index){
        var curPath = path + "/" + file;
        if (fs.lstatSync(curPath).isDirectory()) { // recurse
          deleteFolderRecursive(curPath);
        } else { // delete file
          fs.unlinkSync(curPath);
        }
      });
      fs.rmdirSync(path);
    }
  };