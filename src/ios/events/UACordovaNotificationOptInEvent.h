/* Copyright Urban Airship and Contributors */

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
 * @return The event opt-in event.
 */
+ (instancetype)eventWithData:(NSDictionary *)data;

@end

NS_ASSUME_NONNULL_END
