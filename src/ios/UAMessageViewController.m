/* Copyright Urban Airship and Contributors */

#import "UAMessageViewController.h"

@implementation UAMessageViewController

- (void) viewDidLoad {
    [super viewDidLoad];

    UIBarButtonItem *done = [[UIBarButtonItem alloc]
                             initWithBarButtonSystemItem:UIBarButtonSystemItemDone
                             target:self
                             action:@selector(inboxMessageDone:)];

    self.navigationItem.rightBarButtonItem = done;

     __weak UAMessageViewController *weakSelf = self;
    self.closeBlock = ^(BOOL animated) {
        [weakSelf dismissViewControllerAnimated:animated completion:nil];
    };
}


- (void)inboxMessageDone:(id)sender {
    [self dismissViewControllerAnimated:true completion:nil];
}

@end
