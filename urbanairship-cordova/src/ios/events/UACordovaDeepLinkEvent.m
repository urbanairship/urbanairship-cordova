/* Copyright Urban Airship and Contributors */

#import "UACordovaDeepLinkEvent.h"

NSString *const EventDeepLink = @"urbanairship.deep_link";
NSString *const DeepLinkKey = @"deepLink";

@implementation UACordovaDeepLinkEvent

+ (instancetype)eventWithDeepLink:(NSURL *)deepLink {
    return [[UACordovaDeepLinkEvent alloc] initWithDeepLink:deepLink];
}

- (instancetype)initWithDeepLink:(NSURL *)deepLink {
    self = [super init];
    if (self) {
        self.data = @{DeepLinkKey:[deepLink absoluteString]};
        self.type = EventDeepLink;
    }
    return self;
}

@end
