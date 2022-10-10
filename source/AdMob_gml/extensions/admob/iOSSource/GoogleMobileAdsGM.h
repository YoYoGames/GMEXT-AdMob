
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <AdSupport/ASIdentifierManager.h>
#include <CommonCrypto/CommonDigest.h>
#include <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

@interface GoogleMobileAdsGM:NSObject <GADBannerViewDelegate,GADFullScreenContentDelegate>
{
    NSMutableDictionary *ListenerMap;
    Boolean testingAds;
	Boolean NPA;
}

@property(nonatomic, strong) GADBannerView *bannerView;
@property(nonatomic, strong) GADInterstitialAd*interstitial;
@property(nonatomic, strong) GADInterstitialAd*interstitial_keepMe;
@property(nonatomic, strong) GADRewardedAd *rewardAd;
@property(nonatomic, strong) GADRewardedAd *rewardAd_keepMe;
@property(nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAd;
@property(nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAd_keepMe;

@property(nonatomic, strong) NSString *BannerAdID;
@property(nonatomic, strong) NSString *interstitialAdID;
@property(nonatomic, strong) NSString *rewardAd_ID;
@property(nonatomic, strong) NSString *rewardInterstitialAd_ID;
@property(nonatomic, strong) NSString *appOpenAdID;

@property(nonatomic, strong) UMPConsentForm *myForm;

@property(nonatomic, strong) GADRequest *request_interstitial;
@property(nonatomic, strong) GADRequest *request_rewarded;

@property(nonatomic, strong) GADAppOpenAd *appOpenAd;

@property(nonatomic, strong) NSDate *loadTime;


-(double) AdMob_AppOpenAd_isAdAvailable;

@end


