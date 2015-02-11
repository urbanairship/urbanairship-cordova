#!/usr/bin/env node

//
// This hook removes files the appropriate platform specific location

var filestoremove = {
///////////////////////////
//          ANDROID
///////////////////////////
    android: [
        "platforms/android/custom_rules.xml"
    ]
};

var fs = require('fs');
var path = require('path');

// no need to configure below
var platforms = fs.readdirSync('platforms');

for(var i in platforms) {
    var platform = platforms[i];

    if (filestoremove[platform] == undefined) {
        continue;
    }

    filestoremove[platform].forEach(function(srcfile) {
        if (fs.existsSync(srcfile)) {
            fs.unlinkSync(srcfile)
            console.log('Removing ' + srcfile);
        }
    });
};
