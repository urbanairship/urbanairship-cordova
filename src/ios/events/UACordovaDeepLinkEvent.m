/* Copyright Urban Airship and Contributors */

#import "UACordovaDeepLinkEvent.h"

NSString *const EventDeepLink = @"urbanairship.deep_link";
NSString *const DeepLinkKey = @"deepLink";

@implementation UACordovaDeepLinkEvent

+ (instancetype)event {
    return [[UACordovaDeepLinkEvent alloc] init];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.type = EventDeepLink;
        self.data = nil;
    }
    return self;
}

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
