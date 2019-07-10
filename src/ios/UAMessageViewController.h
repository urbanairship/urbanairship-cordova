/* Copyright Urban Airship and Contributors */

#if __has_include(<AirshipKit/AirshipLib.h>)
#import <AirshipKit/AirshipLib.h>
#elif __has_include("AirshipLib.h")
#import "AirshipLib.h"
#else
@import AirshipKit;
#endif

@interface UAMessageViewController : UAMessageCenterMessageViewController

@end

