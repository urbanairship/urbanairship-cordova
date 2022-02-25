/* Copyright Urban Airship and Contributors */

#import <Foundation/Foundation.h>

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#else
@import AirshipKit;
#endif

NS_ASSUME_NONNULL_BEGIN

/**
 * Manager delegate.
 */
@protocol UACordovaPluginManagerDelegate <NSObject>

/**
 * Called to notify listeners of a new or pending event.
 *
 * @param eventType The event type string.
 * @param data The json payload dictionary.
 *
 * @return `YES` if a listener was notified, `NO` otherwise.
 */
-(BOOL)notifyListener:(NSString *)eventType data:(NSDictionary *)data;
@end

/**
 * Manages config and event forwarding from the Urban Airship SDK.
 */
@interface UACordovaPluginManager : NSObject

/**
 * Delegate.
 */
@property (nonatomic, weak, nullable) id <UACordovaPluginManagerDelegate> delegate;

/**
 * Last received deep link.
 */
@property (nonatomic, copy, nullable) NSString *lastReceivedDeepLink;

/**
 * Flag that enables/disables auto launching the default message center.
 */
@property (nonatomic, assign) BOOL autoLaunchMessageCenter;

/**
 * Last received notification response.
 */
@property (nonatomic, copy, nullable) NSDictionary *lastReceivedNotificationResponse;

/**
 * Checks if Airship is ready.
 */
@property (nonatomic, readonly, assign) BOOL isAirshipReady;

/**
 * Factory method.
 * @param defaultConfig The default cordova config.
 * @return Plugin Manager instance.
 */
+ (instancetype)pluginManagerWithDefaultConfig:(NSDictionary *)defaultConfig;

/**
 * Attempts takeOff if Airship is not already flying.
 */
- (void)attemptTakeOff;

/**
 * Attempts takeOff if Airship is not already flying with launch options.
 */
- (void)attemptTakeOffWithLaunchOptions:(nullable NSDictionary *)launchOptions;

/**
 * Sets the development credentials.
 * @param appKey UA app key.
 * @param appSecret UA app secret.
 */
- (void)setDevelopmentAppKey:(NSString *)appKey appSecret:(NSString *)appSecret;

/**
 * Sets the production credentials.
 * @param appKey The appKey.
 * @param appSecret The appSecret.
 */
- (void)setProductionAppKey:(NSString *)appKey appSecret:(NSString *)appSecret;

/**
 * Sets the cloud site.
 * @param site The site, either "US" or "EU".
 */
- (void)setCloudSite:(NSString *)site;

/**
 * Sets the presentation options.
 * @param options The presentation options.
 */
- (void)setPresentationOptions:(NSUInteger)options;

- (void)setPreferenceCenter:(NSString *)preferenceCenterID useCustomUI:(BOOL)useCustomUI;
@end

NS_ASSUME_NONNULL_END
