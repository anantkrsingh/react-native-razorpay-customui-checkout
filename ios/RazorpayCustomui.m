//
//  RazorpayCheckout.m
//  RazorpayCheckout
//
//  Created by Abhinav Arora on 11/10/17.
//  Copyright © 2016 Razorpay. All rights reserved.
//

#import <WebKit/WebKit.h>

#import "RazorpayCustomui.h"
#import "RazorpayEventEmitterCustom.h"

#import <RazorpayCustom/RazorpayCustom-Swift.h>
@import RazorpayCore;
@interface RazorpayCustomui () <RazorpayPaymentCompletionProtocolWithData, WKNavigationDelegate, WKUIDelegate>{
    RazorpayCheckout *razorpay;
    UIViewController *parentVC;
    WKWebView *webview;
    UINavigationController *navController;
    UIBarButtonItem *cancelBtn;
    UIWindow *window;
}
@end

@implementation RazorpayCustomui

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(getAppsWhichSupportUpi) {
    [RazorpayCheckout getAppsWhichSupportUpiWithHandler:^(NSArray<NSDictionary *> * supportedApps) {
        [RazorpayEventEmitterCustom upiApps:supportedApps];
    }];
}

RCT_EXPORT_METHOD(initRazorpay: (NSString *)key){
    dispatch_sync(dispatch_get_main_queue(), ^{
       razorpay = [RazorpayCheckout initWithKey:key andDelegate:self withPaymentWebView:nil];
   });
}

RCT_EXPORT_METHOD(getCardsNetwork : (NSString *)cardNumber){
   NSString *cardNetwork =  [razorpay getCardNetworkFromCardNumber:cardNumber];
    NSDictionary *dict = @{@"data":cardNetwork};
   [RazorpayEventEmitterCustom cardNetwork:dict];
}

RCT_EXPORT_METHOD(getCardNetworkLength: (NSString *)cardNetwork){
    NSInteger length = (long)[razorpay getCardNetworkLengthOfNetwork:cardNetwork];
    NSNumber *lenNum = [NSNumber numberWithInteger:(long)length];
    NSDictionary *dict = @{@"data":lenNum};
    [RazorpayEventEmitterCustom cardNetworkLength:dict];
}

RCT_EXPORT_METHOD(isCredAppAvailable){
    NSString *credUrl = @"credpay%3A%2F%2F";
    UIApplication *application = [UIApplication sharedApplication];
    NSURL *URL = [NSURL URLWithString:credUrl];
    [application openURL:URL options:@{} completionHandler:^(BOOL status){
        NSDictionary *dict = @{@"data":@(status)};
        [RazorpayEventEmitterCustom credAppAvailable:dict];

    }];


}

 RCT_EXPORT_METHOD(getPaymentMethods){
        [razorpay getPaymentMethodsWithOptions: nil withSuccessCallback: ^(NSDictionary *methods){
                 [RazorpayEventEmitterCustom paymentMethods:methods];
             } andFailureCallback: ^(NSString *error){
                  [RazorpayEventEmitterCustom paymentMethodsError:error];
             }];
 }

RCT_EXPORT_METHOD(getRecommendedInstruments:(NSDictionary *)options){
    [razorpay getRecommendedInstrumentsWithOptions:options withSuccessCallback:^(NSDictionary *recommendedInstruments){
        [RazorpayEventEmitterCustom recommendedInstruments:recommendedInstruments];
    } andFailureCallback:^(NSString *error){
        NSDictionary *dict = @{@"error": error};
        [RazorpayEventEmitterCustom recommendedInstrumentsError:dict];
    }];
}

RCT_EXPORT_METHOD(getWalletLogoUrl:(NSString *)walletName){
    NSURL *url = [razorpay getWalletLogoWithHavingWalletName:walletName];
    // getWalletLogoWithHavingWalletName: is declared nullable — building
    // @{} with a nil value throws NSInvalidArgumentException and crashes.
    NSDictionary *dict = url ? @{@"data":url.absoluteString} : @{@"error":@"Wallet logo not found"};

    [RazorpayEventEmitterCustom walletLogoUrl:dict];
}

RCT_EXPORT_METHOD(getSubscriptionAmount:(NSString *)subscriptionId){
    [razorpay getSubscriptionAmountWithHavingSubscriptionId:subscriptionId withSuccessCallback:^(UInt64 subAmount){
        NSDictionary *dict = @{@"data":@(subAmount)};
        [RazorpayEventEmitterCustom subscriptionAmount:dict];
    } andFailureCallback:^(NSString *error){
        NSDictionary *dict = @{@"error":error};
        [RazorpayEventEmitterCustom subscriptionAmount:dict];
    }];
}

RCT_EXPORT_METHOD(isValidCardNumber:(NSString *)cardNumber){
    BOOL isCardValid = [razorpay isCardValid:cardNumber];
    NSDictionary *dict = @{@"data":@(isCardValid)};

    [RazorpayEventEmitterCustom cardNumberValidity:dict];
}

RCT_EXPORT_METHOD(isValidVpa:(NSString *)vpaAddress){
    [razorpay isValidVpa:vpaAddress withSuccessCallback:^(NSDictionary *success){
        [RazorpayEventEmitterCustom validVpa:success];
    } withFailure:^(NSDictionary *failure){
        [RazorpayEventEmitterCustom validVpa:failure];
    }];
}

RCT_EXPORT_METHOD(getBankLogoUrl:(NSString *)bankName){
    NSURL *url = [razorpay getBankLogoWithHavingBankCode:bankName];
    // getBankLogoWithHavingBankCode: is declared nullable — same nil-crash guard as above.
    NSDictionary *dict = url ? @{@"data":url.absoluteString} : @{@"error":@"Bank logo not found"};
    [RazorpayEventEmitterCustom bankLogoUrl:dict];
}

RCT_EXPORT_METHOD(getSqWalletLogoUrl:(NSString *)walletName){
    NSURL *url = [razorpay getWalletSqLogoWithHavingWalletName:walletName];
    // getWalletSqLogoWithHavingWalletName: is declared nullable — same nil-crash guard as above.
    NSDictionary *dict = url ? @{@"data":url.absoluteString} : @{@"error":@"Wallet logo not found"};
    [RazorpayEventEmitterCustom sqWalletLogoUrl:dict];
}


RCT_EXPORT_METHOD(open : (NSDictionary *)options) {

    NSString *keyID = (NSString *)[options objectForKey:@"key_id"];

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleRotation) name:UIDeviceOrientationDidChangeNotification object:nil];

    dispatch_sync(dispatch_get_main_queue(), ^{

        cancelBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(onTapCancel:)];
        WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
        //Setting web view
        webview = [[WKWebView alloc] initWithFrame: parentVC.view.frame configuration:configuration];
        webview.UIDelegate = self;
        webview.opaque = NO;
        webview.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.4];

        [self resizeView];

        //setting parent view controller
        parentVC = [UIViewController new];
        [parentVC.view addSubview:webview];

        if ([[parentVC navigationController] navigationBar] != nil) {
            navController = parentVC.navigationController;
        } else {
            navController = [[UINavigationController alloc] initWithRootViewController:parentVC];
        }

        parentVC.title = @"Authorize Payment";
        parentVC.navigationItem.leftBarButtonItem = cancelBtn;


        parentVC.view.autoresizingMask =
        (UIViewAutoresizingFlexibleLeftMargin |
         UIViewAutoresizingFlexibleRightMargin |
         UIViewAutoresizingFlexibleBottomMargin |
         UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleHeight |
         UIViewAutoresizingFlexibleWidth);

        webview.autoresizingMask =
        (UIViewAutoresizingFlexibleLeftMargin |
         UIViewAutoresizingFlexibleRightMargin |
         UIViewAutoresizingFlexibleBottomMargin |
         UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleHeight |
         UIViewAutoresizingFlexibleWidth);

        window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
        window.windowLevel = UIWindowLevelStatusBar;
        window.backgroundColor = [UIColor whiteColor];
        webview.backgroundColor = [UIColor whiteColor];
        [window makeKeyAndVisible];
        [window setRootViewController:navController];

        razorpay = [RazorpayCheckout initWithKey:keyID andDelegate:self withPaymentWebView:webview];

        NSMutableDictionary * tempOptions = [[NSMutableDictionary alloc] initWithDictionary:options];
        [razorpay authorize:tempOptions];
    });
}


-(void)onTapCancel: (id) sender{
    [razorpay userCancelledPayment];
    [RazorpayEventEmitterCustom onPaymentError:1 description:@"User Cancelled Payment" andData:[NSMutableDictionary dictionary]];
    [razorpay close];
    [self close];
}

- (void)onPaymentSuccess:(nonnull NSString *)payment_id
                 andData:(NSDictionary *)response {
    if (response == nil){
        [RazorpayEventEmitterCustom onPaymentSuccess:payment_id andData:[NSMutableDictionary dictionary]];
    }else{
        [RazorpayEventEmitterCustom onPaymentSuccess:payment_id andData:response];
    }
    [razorpay close];
    [self close];
}

- (void)onPaymentError:(int)code
           description:(nonnull NSString *)str
               andData:(NSDictionary *)response {
    if (response == nil){
        [RazorpayEventEmitterCustom onPaymentError:code description:str andData:[NSMutableDictionary dictionary]];
    }else{
        [RazorpayEventEmitterCustom onPaymentError:code description:str andData:response];
    }

    [razorpay close];
    [self close];
}

- (void)webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation withError:(NSError *)error{
    [razorpay webView:webview didFail:navigation withError:error];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation{
    [razorpay webView:webview didFinish:navigation];
}

- (void)webView:(WKWebView *)webView didCommitNavigation:(WKNavigation *)navigation{
    [razorpay webView:webview didCommit:navigation];
}

- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(WKNavigation *)navigation withError:(NSError *)error{
    [razorpay webView:webview didFailProvisionalNavigation:navigation withError:error];
}

- (void)handleRotation {
    [self resizeView];
}

- (void)resizeView{
    CGFloat paddingTop = [UIApplication sharedApplication].statusBarHidden ? 0 : 20;
    CGSize size = [[UIScreen mainScreen] bounds].size;
    CGFloat statusHeight = 44;
    [webview setFrame:CGRectMake(0, statusHeight + paddingTop, size.width,
                                 size.height - statusHeight - paddingTop)];
}

- (void)close{


    if (webview != nil){
        [webview loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"about:blank"]]];
        webview.backgroundColor = [UIColor clearColor];
        [webview stopLoading];
    }

    if (navController != nil){
        [navController dismissViewControllerAnimated:true completion:nil];
    }

    razorpay = nil;

    webview = nil;

    parentVC.view = nil;
    parentVC = nil;

    cancelBtn = nil;

    window.hidden = YES;
    window = nil;

}

@end
