/* Copyright Urban Airship and Contributors */

#import "UACordovaPreferenceCenterEvent.h"

NSString *const PreferenceCenterLink = @"urbanairship.open_preference_center";
NSString *const PreferenceCenterKey = @"prefrenceCenter";

@implementation UACordovaPreferenceCenterEvent

+ (instancetype)eventWithPreferenceCenterId:(NSString *)preferenceCenterId {
    return [[UACordovaPreferenceCenterEvent alloc] initWithPreferenceCenterId:preferenceCenterId];
}

- (instancetype)initWithPreferenceCenterId:(NSString *)preferenceCenterId {
    self = [super init];
    if (self) {
        self.data = @{PreferenceCenterKey:preferenceCenterId};
        self.type = PreferenceCenterLink;
    }
    return self;
}

@end