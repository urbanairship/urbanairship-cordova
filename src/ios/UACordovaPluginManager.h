/* Copyright 2018 Urban Airship and Contributors */

#import <Foundation/Foundation.h>

/**
 * Manager delegate.
 */
@protocol UACordovaPluginManagerDelegate <NSObject>

/**
 * Called to notify listeners of a new or pending event.
 */
-(void)notifyListener:(NSString *)eventType data:(NSDictionary *)data;
@end

/**
 * Manages config and event forwaring from the Urban Airship SDK.
 */
@interface UACordovaPluginManager : NSObject

/**
 * Delegate.
 */
@property (nonatomic, weak, nullable) id <UACordovaPluginManagerDelegate> delegate;

/**
 * Last received deep link.
 */
@property (nonatomic, copy) NSString *lastReceivedDeepLink;

/**
 * Flag that enables/disables auto launching the default message center.
 */
@property (nonatomic, assign) BOOL autoLaunchMessageCenter;

/**
 * Last received notification respons.
 */
@property (nonatomic, copy) NSDictionary *lastReceivedNotificationResponse;

/**
 * Checks if Airship is ready.
 */
@property (nonatomic, readonly, assign) BOOL isAirshipReady;

/**
 * Factory method.
 * @param defaultConfig The default cordova config.
 * @returns Plugin Manager instance.
 */
+ (instancetype)pluginManagerWithDefaultConfig:(NSDictionary *)defaultConfig;

/**
 * Attemps takeOff if Airship is not already flying.
 */
- (void)attemptTakeOff;

/**
 * Sets the development credentials.
 * @param appKey The appKey.
 * @param appSecret The appSecret.
 */
- (void)setDevelopmentAppKey:(NSString *)appKey appSecret:(NSString *)appSecret;

/**
 * Sets the production credentials.
 * @param appKey The appKey.
 * @param appSecret The appSecret.
 */
- (void)setProductionAppKey:(NSString *)appKey appSecret:(NSString *)appSecret;
@end
