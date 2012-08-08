
#import "UAAppDelegateSurrogate.h"

@interface UAAppDelegateSurrogate()

@property (nonatomic, readwrite, copy) NSDictionary *launchOptions;

@end

@implementation UAAppDelegateSurrogate

@synthesize surrogateDelegate;
@synthesize launchOptions;

SINGLETON_IMPLEMENTATION(UAAppDelegateSurrogate);

/* this method is called the moment the class is made known to the obj-c runtime,
 before app launch completes. */
+ (void)load {
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center addObserver:[UAAppDelegateSurrogate shared] selector:@selector(handleLaunchNotification:) name:@"UIApplicationDidFinishLaunchingNotification" object:nil];
}

- (void)handleLaunchNotification:(NSNotification *)notification {
    //store the launch options for later use
    self.launchOptions = notification.userInfo;

    //swap pointers with the initial app delegate
    @synchronized ([UIApplication sharedApplication]) {
        id<UIApplicationDelegate> del = [UIApplication sharedApplication].delegate;
        /* note: although according to the docs [UIApplication setDelegate] doesn't retain, the 
         initial delegate does appear to be released (deallocated) when a new delegate
         is set, unless we retain it here */
        defaultAppDelegate = [del retain];
        [UIApplication sharedApplication].delegate = self;
    }
}

#pragma mark Message forwarding

- (void)forwardInvocation:(NSInvocation *)invocation {
    SEL selector = [invocation selector];

    if ([defaultAppDelegate respondsToSelector:selector] || [surrogateDelegate respondsToSelector:selector]) {
        //allow the surrogate delegate a chance to respond first
        if ([surrogateDelegate respondsToSelector:selector]) {
            [invocation invokeWithTarget:surrogateDelegate];
        }

        //forward to the default delate if necessary
        if ([defaultAppDelegate respondsToSelector:selector]) {
            [invocation invokeWithTarget:defaultAppDelegate];
        }
    }

    //otherwise trigger default behavior (usually throws NSInvalidArgumentException)
    else {
        [super forwardInvocation:invocation];
    }
}


- (BOOL)respondsToSelector:(SEL)selector {
    if ([super respondsToSelector:selector]) {
        return YES;
    }

    else {
        //if this isn't a selector we normally respond to, say we do as long as either delegate does
        if ([defaultAppDelegate respondsToSelector:selector] || [surrogateDelegate respondsToSelector:selector]) {
            return YES;
        }
    }

    return NO;
}

- (NSMethodSignature*)methodSignatureForSelector:(SEL)selector {
    NSMethodSignature* signature = [super methodSignatureForSelector:selector];
    if (!signature) {
        signature = [defaultAppDelegate methodSignatureForSelector:selector];
    } else {
        signature = [surrogateDelegate methodSignatureForSelector:selector];
    }
    return signature;
}

#pragma mark Other stuff

- (void)dealloc {
    RELEASE_SAFELY(launchOptions);
}

@end
