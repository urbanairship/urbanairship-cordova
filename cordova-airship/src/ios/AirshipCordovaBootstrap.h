/* Copyright Airship and Contributors */

@import Foundation;


/**
 * Handles takeOff for the Airship SDK.
 */
@interface AirshipCordovaBootstrap: NSObject

typedef void (^UALaunchBlock)(NSDictionary * _Nullable);

@property (class, nonatomic, copy, nullable) UALaunchBlock onLaunch;
@end
