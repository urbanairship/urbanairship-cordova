
#import "UAAppDelegateSurrogate.h"
#import "UAPush.h"

NSString * const UADefaultDelegateNilException = @"UADefaultDelegateNilException";

@interface UAAppDelegateSurrogate()

@property (nonatomic, readwrite, copy) NSDictionary *launchOptions;

@end

@implementation UAAppDelegateSurrogate

@synthesize surrogateDelegate;
@synthesize defaultAppDelegate;
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
    // Set default value of UAPush to no, this allows a developer to register for notification types at app
    // app start, and then simply enable push at a later time. This will defer the UIAlertView prompting the user
    // to accept push until after the developer has a chance to make the case for push
    [UAPush setDefaultPushEnabledValue:NO];
}

- (void)clearLaunchOptions {
    self.launchOptions = nil;
}

#pragma mark Message forwarding

- (void)forwardInvocation:(NSInvocation *)invocation {
    SEL selector = [invocation selector];
    // Throw the exception here to make debugging easier. We are going to forward the invocation to the
    // defaultAppDelegate without checking if it responds for the purpose of crashing the app in the right place
    // if the delegate does not respond which would be expected behavior. If the defaultAppDelegate is nil, we
    // need to exception here, and not fail silently.
    if (!defaultAppDelegate) {
        NSString *errorMsg = @"UAAppDelegateSurrogate defaultAppDelegate was nil while forwarding an invocation";
        NSException *defaultAppDelegateNil = [NSException exceptionWithName:UADefaultDelegateNilException
                                                                     reason:errorMsg
                                                                   userInfo:nil];
        [defaultAppDelegateNil raise];
    }

    BOOL responds = NO;

    //give the surrogate and default app delegates an opportunity to handle the message
    if ([surrogateDelegate respondsToSelector:selector]) {
        responds = YES;
        [invocation invokeWithTarget:surrogateDelegate];
    }
    if ([defaultAppDelegate respondsToSelector:selector]) {
        responds = YES;
        [invocation invokeWithTarget:defaultAppDelegate];
    }

    if (!responds) {
        //in the off chance that neither app delegate responds, forward the message
        //to the default app delegate anyway.  this will likely result in a crash,
        //but that way the exception will come from the expected location
        [invocation invokeWithTarget:defaultAppDelegate];
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
    NSMethodSignature *signature = nil;
    // First non nil method signature returns
    signature = [super methodSignatureForSelector:selector];
    if (signature) return signature;

    signature = [defaultAppDelegate methodSignatureForSelector:selector];
    if (signature) return signature;

    signature = [surrogateDelegate methodSignatureForSelector:selector];
    if (signature) return signature;

    // If none of the above classes return a non nil method signature, this will likely crash
    return signature;
}

#pragma mark Other stuff

- (void)dealloc {
    RELEASE_SAFELY(launchOptions);
    [super dealloc];
}

@end
