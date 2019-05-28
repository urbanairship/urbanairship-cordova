/* Copyright Urban Airship and Contributors */

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventInboxUpdated;

/**
 * Inbox update event.
 */
@interface UACordovaInboxUpdatedEvent : NSObject<UACordovaEvent>

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
 * The default event.
 *
 * @return The default event.
 */
+ (instancetype)event;


@end

NS_ASSUME_NONNULL_END
