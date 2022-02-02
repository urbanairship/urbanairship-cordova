#!/usr/bin/env node

'use strict';

var fs = require('fs');
var path = require("path");

module.exports = function(context) {

	if (context.opts.platforms.indexOf('android') < 0) {
        return;
    }

	if (fs.statSync(path.resolve("platforms/android")).isDirectory()) {
		var androidHelper = require("./lib/android");
		androidHelper.addDependencyToRootGradle("com.huawei.agconnect:agcp:1.6.0.300", "https://developer.huawei.com/repo/");
		androidHelper.applyPluginToAppGradle("com.huawei.agconnect");
	}

};