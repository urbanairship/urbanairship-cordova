Update Cordova Instructions:
* Plugin version:
  * In the root directory run: `scripts/update_version.sh <plugin version>`
* iOS SDK version
  * update plugin.xml with new iOS version, i.e. 6.4.0
* Android SDK version
  * update src/android/build-extras.gradle to point to the latest android versions (our SDK & external libraries)

Android limitations:
- We need to stay compatible with Java 1.6. Try to not use `<>` and Strings in a switch statement.
