
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <AdSupport/ASIdentifierManager.h>
#include <CommonCrypto/CommonDigest.h>
#include <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

@interface ThreadSafeQueue : NSObject

@property (nonatomic, strong) NSMutableArray *array;
@property (nonatomic, strong) dispatch_queue_t queue;

- (void)enqueue:(id)object;
- (id)dequeue;
- (NSUInteger)size;

@end


@interface GoogleMobileAdsGM : NSObject <GADBannerViewDelegate, GADFullScreenContentDelegate>

@property(nonatomic, assign) Boolean isInitialized;
@property(nonatomic, strong) NSMutableDictionary *listenerMap;
@property(nonatomic, assign) Boolean isTestDevice;
@property(nonatomic, assign) Boolean NPA;

@property(nonatomic, assign) int interstitialMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedInterstitialQueue;

@property(nonatomic, assign) int rewardedVideoMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedRewardedVideoQueue;

@property(nonatomic, assign) int rewardedInterstitialMaxLoadedInstances;
@property(nonatomic, strong) ThreadSafeQueue *loadedRewardedInterstitialQueue;

@property(nonatomic, assign) bool triggerPaidEventCallback;
@property(nonatomic, assign) bool isAppOpenAdEnabled;
@property(nonatomic, assign) UIInterfaceOrientation appOpenAdOrientation;

@property(nonatomic, strong) GADBannerView *bannerView;
@property(nonatomic, strong) GADInterstitialAd *interstitialKeepMe;
@property(nonatomic, strong) const GADRewardedAd *rewardAdKeepMe;
@property(nonatomic, strong) const GADRewardedInterstitialAd *rewardedInterstitialAdKeepMe;

@property(nonatomic, strong) NSString *bannerAdUnitId;
@property(nonatomic, strong) NSString *interstitialAdUnitId;
@property(nonatomic, strong) NSString *rewardedVideoUnitId;
@property(nonatomic, strong) NSString *rewardedInterstitialAdUnitId;
@property(nonatomic, strong) NSString *appOpenAdUnitId;

@property(nonatomic, strong) UMPConsentForm *myForm;

@property(nonatomic, strong) GADRequest *requestInterstitial;
@property(nonatomic, strong) GADRequest *requestRewarded;

@property(nonatomic, strong) GADAppOpenAd *appOpenAd;
@property(nonatomic, assign) bool isShowingAd;
@property(nonatomic, strong) NSDate *loadTime;

@end


