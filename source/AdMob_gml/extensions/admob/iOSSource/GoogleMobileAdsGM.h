
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <AdSupport/ASIdentifierManager.h>
#include <CommonCrypto/CommonDigest.h>
#include <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

@class ThreadSafeQueue;

@interface GoogleMobileAdsGM : NSObject <GADBannerViewDelegate, GADFullScreenContentDelegate>

@property(nonatomic, assign) BOOL isInitialized;
@property(nonatomic, assign) BOOL isTestDevice;
@property(nonatomic, assign) BOOL isRdpEnabled;
@property(nonatomic, assign) BOOL isShowingAd;

@property(nonatomic, strong) NSString *bannerAdUnitId;
@property(nonatomic, assign) int currentBannerAlignment;
@property(nonatomic, strong) GADBannerView *bannerView; // Retain the currently showing ad

@property(nonatomic, strong) NSString *interstitialAdUnitId;
@property(nonatomic, assign) int interstitialAdQueueCapacity;
@property(nonatomic, strong) ThreadSafeQueue *interstitialAdQueue;
@property (nonatomic, strong) GADInterstitialAd *interstitialAd; // Retain the currently showing ad

@property(nonatomic, strong) NSString *rewardedUnitId;
@property(nonatomic, assign) int rewardedAdQueueCapacity;
@property(nonatomic, strong) ThreadSafeQueue *rewardedAdQueue;
@property(nonatomic, strong) GADRewardedAd *rewardedAd; // Retain the currently showing ad
@property(nonatomic, strong) NSString *serverSideVerificationUserId;
@property(nonatomic, strong) NSString *serverSideVerificationCustomData;

@property(nonatomic, strong) NSString *rewardedInterstitialAdUnitId;
@property(nonatomic, assign) int rewardedAdInterstitialQueueCapacity;
@property(nonatomic, strong) ThreadSafeQueue *rewardedInterstitialAdQueue;
@property(nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAd; // Retain the currently showing ad

@property(nonatomic, strong) NSString *appOpenAdUnitId;
@property(nonatomic, assign) UIInterfaceOrientation appOpenAdOrientation;
@property(nonatomic, strong) NSDate *appOpenAdLoadTime;
@property(nonatomic, assign) int appOpenAdExpirationTime;
@property(nonatomic, strong) GADAppOpenAd *appOpenAd; // Retain the currently showing ad

@property(nonatomic, assign) BOOL triggerOnPaidEvent;
@property(nonatomic, assign) BOOL triggerAppOpenAd;

@property(nonatomic, strong) UMPConsentForm *consentForm;

@end


