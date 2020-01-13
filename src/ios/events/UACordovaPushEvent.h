/* Copyright Urban Airship and Contributors */

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#else
@import Airship;
#endif

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventPushReceived;

/**
 * Push event.
 */
@interface UACordovaPushEvent : NSObject<UACordovaEvent>

/**
 * The event type.
 *
 * @return The event type.
 */
@property (nonatomic, strong, nullable) NSString *type;

/**
 * The event data.
 *
 * @return The event data.
 */
@property (nonatomic, strong, nullable) NSDictionary *data;

/**
 * Push event with notification content.
 *
 * @param content The notification content.
 */
+ (instancetype)eventWithNotificationContent:(UANotificationContent *)content;

/**
 * Helper method for producing sanitized push payloads from notification content.
 *
 * @param notificationContent The notification content.
 * @return A push payload dictionary.
 */
+ (NSDictionary *)pushEventDataFromNotificationContent:(UANotificationContent *)notificationContent;

@end

NS_ASSUME_NONNULL_END
