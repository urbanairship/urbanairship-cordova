/* Copyright Urban Airship and Contributors */

#import "UAMessageViewController.h"

@interface UAMessageViewController() <UAMessageCenterMessageViewDelegate>
@property (nonatomic, copy) NSString *pendingMessageID;
@property (nonatomic, strong) UADefaultMessageCenterMessageViewController *airshipMessageViewController;
@end

@implementation UAMessageViewController

- (void) viewDidLoad {
    [super viewDidLoad];

    self.airshipMessageViewController = [[UADefaultMessageCenterMessageViewController alloc]
                                         initWithNibName:@"UADefaultMessageCenterMessageViewController"
                                         bundle:[UAMessageCenterResources bundle]];
    self.airshipMessageViewController.delegate = self;


    UIBarButtonItem *done = [[UIBarButtonItem alloc]
                             initWithBarButtonSystemItem:UIBarButtonSystemItemDone
                             target:self
                             action:@selector(inboxMessageDone:)];

    self.airshipMessageViewController.navigationItem.leftBarButtonItem = done;

    self.viewControllers = @[self.airshipMessageViewController];

    if (self.pendingMessageID) {
        [self.airshipMessageViewController loadMessageForID:self.pendingMessageID];
        self.pendingMessageID = nil;
    }
}

- (void)inboxMessageDone:(id)sender {
    [self dismissViewControllerAnimated:true completion:nil];
}

- (void)loadMessageForID:(NSString *)messageID {
    if (self.airshipMessageViewController) {
        [self.airshipMessageViewController loadMessageForID:messageID];
        self.pendingMessageID = nil;
    } else {
        self.pendingMessageID = messageID;
    }
}

#pragma mark UAMessageCenterMessageViewDelegate

- (void)messageClosed:(NSString *)messageID {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)messageLoadStarted:(NSString *)messageID {
    // no-op
}

- (void)messageLoadSucceeded:(NSString *)messageID {
    // no-op
}

- (void)displayFailedToLoadAlertOnOK:(void (^)(void))okCompletion onRetry:(void (^)(void))retryCompletion {
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:UAMessageCenterLocalizedString(@"ua_connection_error")
                                                                   message:UAMessageCenterLocalizedString(@"ua_mc_failed_to_load")
                                                            preferredStyle:UIAlertControllerStyleAlert];

    UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:UAMessageCenterLocalizedString(@"ua_ok")
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction * action) {
        if (okCompletion) {
            okCompletion();
        }
    }];

    [alert addAction:defaultAction];

    if (retryCompletion) {
        UIAlertAction *retryAction = [UIAlertAction actionWithTitle:UAMessageCenterLocalizedString(@"ua_retry_button")
                                                              style:UIAlertActionStyleDefault
                                                            handler:^(UIAlertAction * _Nonnull action) {
            if (retryCompletion) {
                retryCompletion();
            }
        }];

        [alert addAction:retryAction];
    }

    [self presentViewController:alert animated:YES completion:nil];
}

- (void)displayNoLongerAvailableAlertOnOK:(void (^)(void))okCompletion {
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:UAMessageCenterLocalizedString(@"ua_content_error")
                                                                   message:UAMessageCenterLocalizedString(@"ua_mc_no_longer_available")
                                                            preferredStyle:UIAlertControllerStyleAlert];

    UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:UAMessageCenterLocalizedString(@"ua_ok")
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction * action) {
        if (okCompletion) {
            okCompletion();
        }
    }];

    [alert addAction:defaultAction];

    [self presentViewController:alert animated:YES completion:nil];
}

- (void)messageLoadFailed:(NSString *)messageID error:(NSError *)error {
    UA_LTRACE(@"message load failed: %@", messageID);

    void (^retry)(void) = ^{
        UA_WEAKIFY(self);
        [self displayFailedToLoadAlertOnOK:^{
            UA_STRONGIFY(self)
            [self dismissViewControllerAnimated:true completion:nil];
        } onRetry:^{
            UA_STRONGIFY(self)
            [self loadMessageForID:messageID];
        }];
    };

    void (^handleFailed)(void) = ^{
        UA_WEAKIFY(self);
        [self displayFailedToLoadAlertOnOK:^{
            UA_STRONGIFY(self)
            [self dismissViewControllerAnimated:true completion:nil];
        } onRetry:nil];
    };

    void (^handleExpired)(void) = ^{
        UA_WEAKIFY(self);
        [self displayNoLongerAvailableAlertOnOK:^{
            UA_STRONGIFY(self)
            [self dismissViewControllerAnimated:true completion:nil];
        }];
    };

    if ([error.domain isEqualToString:UAMessageCenterMessageLoadErrorDomain]) {
        if (error.code == UAMessageCenterMessageLoadErrorCodeFailureStatus) {
            // Encountered a failure status code
            NSUInteger status = [error.userInfo[UAMessageCenterMessageLoadErrorHTTPStatusKey] unsignedIntValue];

            if (status >= 500) {
                retry();
            } else if (status == 410) {
                // Gone: message has been permanently deleted from the backend.
                handleExpired();
            } else {
                handleFailed();
            }
        } else if (error.code == UAMessageCenterMessageLoadErrorCodeMessageExpired) {
            handleExpired();
        } else {
            retry();
        }
    } else {
        // Other errors
        retry();
    }
}
@end

