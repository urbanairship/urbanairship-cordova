const fs = require('fs');
const path = require('path');

const ANDROID_PROJECT_ROOT = 'platforms/android';
const ROOT_GRADLE_FILEPATH = ANDROID_PROJECT_ROOT + '/build.gradle';

const gradleDependencyTemplate = "classpath '{artifactDef}'";
const gradleRepositoryTemplate = "maven { url '{repositoryDef}' }";

const Android = {};

Android.addDependencyToRootGradle = function(artifactDef, repositoryDef) {
	const gradleDependency = gradleDependencyTemplate.replace("{artifactDef}", artifactDef);
	const gradleRepository = gradleRepositoryTemplate.replace("{repositoryDef}", repositoryDef);

    let rootGradle = fs.readFileSync(path.resolve(ROOT_GRADLE_FILEPATH)).toString();
    
    if (!rootGradle.match(gradleDependency)) {
    	rootGradle = rootGradle.replace("dependencies {", "dependencies {\n"+gradleDependency);
    	fs.writeFileSync(path.resolve(ROOT_GRADLE_FILEPATH), rootGradle);
    	console.log("UALib - Added dependency to root gradle: " + artifactDef);
		console.log("UALib: " + rootGradle);
	}

	if (!rootGradle.match(gradleRepository)) {
    	rootGradle = rootGradle.replace(/repositories {/g, "repositories {\n"+gradleRepository);
    	fs.writeFileSync(path.resolve(ROOT_GRADLE_FILEPATH), rootGradle);
    	console.log("UALib - Added maven repository to root gradle: " + repositoryDef);
		console.log("UALib: " + rootGradle);
	}
};

module.exports = Android;