/* Copyright Urban Airship and Contributors */

#import <Foundation/Foundation.h>
#import "AirshipLib.h"

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
@property (nonatomic, copy) NSString *lastReceivedDeepLink;

/**
 * Flag that enables/disables auto launching the default message center.
 */
@property (nonatomic, assign) BOOL autoLaunchMessageCenter;

/**
 * Last received notification response.
 */
@property (nonatomic, copy) NSDictionary *lastReceivedNotificationResponse;

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
 * Sets the presentation options.
 * @param options The presentation options.
 */
- (void)setPresentationOptions:(NSUInteger)options;

/**
 * Generates a push event dictionary from a notification content object.
 *
 * @param notificationContent The notification content.
 * @return A push event dictionary.
 */
- (NSDictionary *)pushEventFromNotification:(UANotificationContent *)notificationContent;

@end
