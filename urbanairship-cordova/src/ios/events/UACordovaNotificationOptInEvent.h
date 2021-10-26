/* Copyright Urban Airship and Contributors */

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#else
@import AirshipKit;
#endif

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventNotificationOptInStatus;

/**
 * Notification opt-in status event.
 */
@interface UACordovaNotificationOptInEvent : NSObject<UACordovaEvent>

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
 * The opt-in event constructor.
 *
 * @param authorizedSettings The authorized notification settings
 * @return The event opt-in event.
 */
+ (instancetype)eventWithAuthorizedSettings:(UAAuthorizedNotificationSettings)authorizedSettings;

@end

NS_ASSUME_NONNULL_END
