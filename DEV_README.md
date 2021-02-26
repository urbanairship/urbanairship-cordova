# Airship Cordova

## Update Plugin and SDK versions instructions

* Plugin version:
  * In the root directory run: `scripts/update_version.sh <plugin version>`
* iOS SDK version
  * update plugin.xml with new iOS version, i.e. 6.4.0
* Android SDK version
  * update src/android/build-extras.gradle to point to the latest android versions (our SDK & external libraries)

## Development

The example is set up to reference the modules using a yarn workspace.

1) Install yarn and watchman, if necessary

```
brew install yarn
```

```
brew install watchman
```

2) Install modules

Execute the following command in the root directory

```
yarn install
```

3) Create a sample app

Execute the following command in the root directory.
That will create a sample in the parent directory with our cordova modules

```
yarn create-sample
```

4) Build and Run

Execute the following commands in the root directory to build and run the sample app for a given OS

```
yarn build-ios
yarn build-android
```

```
yarn run-ios
yarn run-android
```

## Android limitations

- We need to stay compatible with Java 1.6. Try to not use `<>` and Strings in a switch statement.