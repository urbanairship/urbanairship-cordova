Update Cordova Instructions:
* In the root directory run:
        `node scripts/update_cordova.js <plugin version> -a <android SDK version> -i <iOS SDK version>`

* For help run `node scripts/update_cordova.js --help`


Android limitations:
- We need to stay compatible with Java 1.6. Try to not use `<>` and Strings in a switch statement.
