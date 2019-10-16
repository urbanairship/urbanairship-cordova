/* Copyright Urban Airship and Contributors */

#import "UACordovaPushEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventNotificationOpened;

/**
 * Notification opened event.
 */
@interface UACordovaNotificationOpenedEvent : UACordovaPushEvent

/**
 * Notification opened event with notification response.
 *
 * @param content The notification response.
*/
+ (instancetype)eventWithNotificationResponse:(UANotificationResponse *)response;

@end

NS_ASSUME_NONNULL_END
