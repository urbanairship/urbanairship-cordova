/* Copyright Urban Airship and Contributors */

#import "UACordovaEvent.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const EventDeepLink;

/**
 * Deep link event when a new deep link is received.
 */
@interface UACordovaDeepLinkEvent : NSObject<UACordovaEvent>

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
 * Deep link event when a new deep link is received.
 *
 * @param deepLink The deep link url.
 */
+ (instancetype)eventWithDeepLink:(NSURL *)deepLink;

@end

NS_ASSUME_NONNULL_END
