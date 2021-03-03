/* Copyright Urban Airship and Contributors */

#if __has_include("AirshipLib.h")
#import "AirshipLib.h"
#import "AirshipMessageCenterLib.h"
#else
@import Airship;
#endif

@interface UAMessageViewController : UINavigationController

- (void)loadMessageForID:(nullable NSString *)messageID;

@end

