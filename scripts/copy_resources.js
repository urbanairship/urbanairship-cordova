#!/usr/bin/env node

var fs = require('fs');
var path = require('path');

module.exports = function (context) {    
    var cordova_util = context.requireCordovaModule('cordova-lib/src/cordova/util');
    var projectRoot = cordova_util.isCordova(process.cwd());
    var projectXml = cordova_util.projectConfig(projectRoot);
    var ConfigParser = context.requireCordovaModule ('cordova-lib/src/ConfigParser/ConfigParser');
    var projectConfig = new ConfigParser(projectXml);
    var projName = projectConfig.name();
    console.log(projName);

    var filestocopy = {
    ///////////////////////////
    //          iOS
    ///////////////////////////
        ios : [
            {
                "plugins/com.urbanairship.cordova/src/ios/Airship/UI/Default/Common/Resources": "platforms/ios/" + projName
            }
        ]
    };

    function copyResources() {
        // no need to configure below
        var platforms = fs.readdirSync('platforms');

        for(var i in platforms) {
            var platform = platforms[i];

            if (filestocopy[platform] == undefined) {
                continue;
            }

            filestocopy[platform].forEach(function(obj) {
                Object.keys(obj).forEach(function(srcfile) {
                    var destfile = obj[srcfile];

                    console.log('Copying ' + srcfile + ' to ' + destfile);

                    function copyFileSync( source, target ) {

                        var targetFile = target;

                        //if target is a directory a new file with the same name will be created
                        if ( fs.existsSync( target ) ) {
                            if ( fs.lstatSync( target ).isDirectory() ) {
                                targetFile = path.join( target, path.basename( source ) );
                            }
                        }

                        fs.createReadStream( source ).pipe( fs.createWriteStream( targetFile ) );
                    }

                    function copyFolderRecursiveSync( source, target ) {
                        var files = [];

                        //check if folder needs to be created or integrated
                        var targetFolder = path.join( target, path.basename( source ) );
                        console.log(targetFolder);
                        if ( !fs.existsSync( targetFolder ) ) {
                            fs.mkdirSync( targetFolder );
                        }

                        //copy
                        if ( fs.lstatSync( source ).isDirectory() ) {
                            files = fs.readdirSync( source );
                            files.forEach( function ( file ) {
                                var curSource = path.join( source, file );
                                if ( fs.lstatSync( curSource ).isDirectory() ) {
                                    copyFolderRecursiveSync( curSource, targetFolder );
                                } else {
                                    copyFileSync( curSource, targetFolder );
                                }
                            } );
                        }
                    }            

                    copyFolderRecursiveSync(srcfile, destfile);
                });
            });
        }
    };

    copyResources();
};

