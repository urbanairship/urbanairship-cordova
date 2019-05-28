/* Copyright Urban Airship and Contributors */

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventNotificationOpened;

/**
 * Notification opened event.
 */
@interface UACordovaNotificationOpenedEvent : NSObject<UACordovaEvent>

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
 * Open event with event data.
 *
 * @param data The event data.
 */
+ (instancetype)eventWithData:(NSDictionary *)data;

@end

NS_ASSUME_NONNULL_END
