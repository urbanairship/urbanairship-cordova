# Airship Cordova

## Update Plugin and SDK versions instructions

* Plugin version:
  * In the root directory run: `scripts/update_version.sh <plugin version>`
* iOS SDK version
  * update plugin.xml with new iOS version, i.e. 6.4.0
* Android SDK version
  * update src/android/build-extras.gradle to point to the latest android versions (our SDK & external libraries)

## Development

Basic setup:

1) Install everything

```
npm run bootstrap
```

2) Create a sample app

```
npm run create-sample
```


When working with the plugin, you most likely will use the sample app. After making the changes you need you will copy the files back to the plugin. 