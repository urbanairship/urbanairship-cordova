/* Copyright Airship and Contributors */

#import "AirshipCordovaBootstrap.h"

@implementation AirshipCordovaBootstrap


static UALaunchBlock _launchBlock = nil;

+ (void)setOnLaunch:(UALaunchBlock)onLaunch {
    _launchBlock = onLaunch;

    if (_didLaunch) {
        onLaunch(_launchOptions);
    } else {
        _launchBlock = onLaunch;
    }
}

+ (UALaunchBlock)onLaunch {
    return _launchBlock;
}

static BOOL _didLaunch = false;
static NSDictionary *_launchOptions = nil;

+ (void)load {
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center addObserverForName:UIApplicationDidFinishLaunchingNotification
                        object:nil
                         queue:nil usingBlock:^(NSNotification * _Nonnull note) {

        _didLaunch = YES;
        _launchOptions = note.userInfo.copy;
        if (_launchBlock) {
            _launchBlock(_launchOptions);
            _launchBlock = nil;
        }
    }];
}

@end
