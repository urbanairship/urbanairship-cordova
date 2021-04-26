const fs = require('fs');
const path = require('path');

const ANDROID_PROJECT_ROOT = 'platforms/android';
const ROOT_GRADLE_FILEPATH = ANDROID_PROJECT_ROOT + '/build.gradle';
const APP_GRADLE_FILEPATH = ANDROID_PROJECT_ROOT + '/app/build.gradle';

const gradleDependencyTemplate = "classpath '{artifactDef}'";
const gradleRepositoryTemplate = "maven { url '{repositoryDef}' }";
const applyPluginTemplate = "apply plugin: '{pluginDef}'";

const Android = {};

Android.addDependencyToRootGradle = function(artifactDef, repositoryDef) {
	const gradleDependency = gradleDependencyTemplate.replace("{artifactDef}", artifactDef);
	const gradleRepository = gradleRepositoryTemplate.replace("{repositoryDef}", repositoryDef);

    let rootGradle = fs.readFileSync(path.resolve(ROOT_GRADLE_FILEPATH)).toString();
    
    if (!rootGradle.match(gradleDependency)) {
    	rootGradle = rootGradle.replace("dependencies {", "dependencies {\n"+gradleDependency);
    	fs.writeFileSync(path.resolve(ROOT_GRADLE_FILEPATH), rootGradle);
    	console.log("UALib - Added dependency to root gradle: " + artifactDef);
	}

	if (!rootGradle.match(gradleRepository)) {
    	rootGradle = rootGradle.replace(/repositories {/g, "repositories {\n"+gradleRepository);
    	fs.writeFileSync(path.resolve(ROOT_GRADLE_FILEPATH), rootGradle);
    	console.log("UALib - Added maven repository to root gradle: " + repositoryDef);
	}
};

Android.applyPluginToAppGradle = function(pluginDef) {
    const applyPlugin = applyPluginTemplate.replace("{pluginDef}", pluginDef);

    let appGradle = fs.readFileSync(path.resolve(APP_GRADLE_FILEPATH)).toString();
    
    if (appGradle.match(applyPlugin)) return;
    appGradle += "\n"+applyPlugin;
    fs.writeFileSync(path.resolve(APP_GRADLE_FILEPATH), appGradle);
    console.log("UALib - Applied plugin to app gradle: " + pluginDef);
};

module.exports = Android;