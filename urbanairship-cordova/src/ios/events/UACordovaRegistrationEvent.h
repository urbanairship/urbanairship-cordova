/* Copyright Urban Airship and Contributors */

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventRegistration;

/**
 * Registration event.
 */
@interface UACordovaRegistrationEvent : NSObject<UACordovaEvent>

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
 * The registration succeeded event constructor.
 *
 * @param channelID The channel ID.
 * @param deviceToken The device token.
 * @return registration event.
 */
+ (instancetype)registrationSucceededEventWithChannelID:channelID deviceToken:(NSString *)deviceToken;

/**
 * The registration failed event constructor.
 *
 * @return registration event.
*/
+ (instancetype)registrationFailedEvent;

@end

NS_ASSUME_NONNULL_END
