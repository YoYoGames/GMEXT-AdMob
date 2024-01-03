
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <AdSupport/ASIdentifierManager.h>
#include <CommonCrypto/CommonDigest.h>
#include <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

@class ThreadSafeQueue;

@interface GoogleMobileAdsGM : NSObject <GADBannerViewDelegate, GADFullScreenContentDelegate>

@property(nonatomic, assign) Boolean isInitialized;
@property(nonatomic, assign) Boolean isTestDevice;
@property(nonatomic, assign) Boolean nonPersonalizedAds;

@property(nonatomic, strong) NSString *interstitialAdUnitId;
@property(nonatomic, assign) int interstitialMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedInterstitialQueue;
@property(nonatomic, strong) GADInterstitialAd *interstitialKeepMe;

@property(nonatomic, strong) NSString *rewardedVideoUnitId;
@property(nonatomic, assign) int rewardedVideoMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedRewardedVideoQueue;
@property(nonatomic, strong) GADRewardedAd *rewardVideoAdKeepMe;

@property(nonatomic, strong) NSString *rewardedInterstitialAdUnitId;
@property(nonatomic, assign) int rewardedInterstitialMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedRewardedInterstitialQueue;
@property(nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAdKeepMe;

@property(nonatomic, assign) bool triggerOnPaidEvent;

@property(nonatomic, strong) NSString *bannerAdUnitId;
@property(nonatomic, strong) GADBannerView *bannerView;


@property(nonatomic, strong) NSString *appOpenAdUnitId;
@property(nonatomic, strong) GADAppOpenAd *appOpenAdInstance;
@property(nonatomic, assign) UIInterfaceOrientation appOpenAdOrientation;
@property(nonatomic, strong) NSDate *appOpenAdLoadTime;
@property(nonatomic, assign) bool isAppOpenAdEnabled;
@property(nonatomic, assign) bool isShowingAd;

@property(nonatomic, strong) UMPConsentForm *consentForm;

@end


