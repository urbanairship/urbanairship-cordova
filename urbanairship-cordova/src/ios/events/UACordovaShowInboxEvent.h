/* Copyright Urban Airship and Contributors */

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventShowInbox;

/**
 * Show inbox event.
 */
@interface UACordovaShowInboxEvent : NSObject<UACordovaEvent>

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
 * Show default inbox event
 *
 * @return The default inbox event.
 */
+ (instancetype)event;

/**
 * Show inbox event with message id.
 *
 * @return The inbox event with message identifier data.
 */
+ (instancetype)eventWithMessageID:(NSString *)identifier;

@end

NS_ASSUME_NONNULL_END
