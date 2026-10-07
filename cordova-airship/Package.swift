// swift-tools-version:6.0

// Copyright Airship and Contributors

import PackageDescription

let package = Package(
    name: "@ua/cordova-airship",
    platforms: [.iOS(.v16)],
    products: [
        .library(
            name: "@ua/cordova-airship",
            targets: ["AirshipCordova"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/apache/cordova-ios.git", branch: "master"),
        .package(url: "https://github.com/urbanairship/airship-mobile-framework-proxy.git", from: "16.0.1"),
        .package(url: "https://github.com/urbanairship/ios-library.git", from: "21.0.2"),
    ],
    targets: [
        .target(
            name: "AirshipCordova",
            dependencies: [
                .product(name: "Cordova", package: "cordova-ios"),
                .product(name: "AirshipFrameworkProxy", package: "airship-mobile-framework-proxy"),
                .product(name: "AirshipCore", package: "ios-library"),
            ],
            path: "src/ios",
            swiftSettings: [.swiftLanguageMode(.v5)]
        )
    ]
)
