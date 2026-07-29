#import "GMAdMob_ios.h"

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AdSupport/AdSupport.h>
#import <CommonCrypto/CommonDigest.h>
#include <atomic>
#include <cstring>
#include <unordered_map>


extern UIViewController *g_controller;
extern UIView *g_glView;
extern int g_DeviceWidth;
extern int g_DeviceHeight;

extern "C" const char* extOptGetString(char* _ext, char* _opt);
extern "C" const char* extGetVersion(char* _ext);

static gm::wire::GMFunction g_paid_event_callback = nil;
static gm::wire::GMFunction g_banner_callback = nil;
static std::unordered_map<void *, gm::wire::GMFunction> g_interstitial_show_callbacks;
static std::unordered_map<void *, gm::wire::GMFunction> g_rewarded_video_show_callbacks;
static std::unordered_map<void *, gm::wire::GMFunction> g_rewarded_interstitial_show_callbacks;

// Shared across interstitial/rewarded video/rewarded interstitial handles so a handle from one
// ad type can never coincide with a live handle from another - a mismatched call site (wrong
// variable passed to the wrong show()) fails loud as InvalidHandle instead of potentially
// matching an unrelated ad by coincidence. Atomic because GADInterstitialAd/GADRewardedAd/
// GADRewardedInterstitialAd's load completion handler has no documented main-thread guarantee.
static std::atomic<int64_t> g_next_ad_handle{1};
static gm::wire::GMFunction g_app_open_enable_callback = nil;
// Single slot each (not a queue) - app open is genuinely single-instance, matching Android and
// Google's own AppOpenAdManager sample; an overlapping load/show call overwrites the pending one.
static gm::wire::GMFunction g_app_open_load_callback = nil;
static gm::wire::GMFunction g_app_open_show_callback = nil;

static NSString *AdMobStringFromStringView(std::string_view value)
{
    NSString *string =
        [[NSString alloc]
            initWithBytes:value.data()
            length:value.size()
            encoding:NSUTF8StringEncoding];

    return string != nil ? string : @"";
}

static const char *AdMobCString(NSString *value)
{
    return value != nil ? value.UTF8String : "";
}

@interface GMAdMob () <GADFullScreenContentDelegate, GADBannerViewDelegate>
@property (nonatomic, assign) BOOL isInitialized;
@property (nonatomic, assign) BOOL isTestDevice;
@property (nonatomic, assign) BOOL isRdpEnabled;
@property (nonatomic, assign) BOOL isShowingAd;
@property (nonatomic, assign) BOOL triggerOnPaidEvent;
@property (nonatomic, assign) BOOL triggerAppOpenAd;
@property (nonatomic, assign) BOOL targetCOPPA;
@property (nonatomic, assign) BOOL targetUnderAge;
@property (nonatomic, strong) NSString *bannerAdUnitId;
@property (nonatomic, strong) NSString *interstitialAdUnitId;
@property (nonatomic, strong) NSString *rewardedUnitId;
@property (nonatomic, strong) NSString *rewardedInterstitialAdUnitId;
@property (nonatomic, strong) NSString *appOpenAdUnitId;
@property (nonatomic, strong) GADBannerView *bannerView;
@property (nonatomic, assign) int currentBannerAlignment;
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, GADInterstitialAd *> *interstitialAdHandles;
@property (nonatomic, strong) GADInterstitialAd *interstitialAd;
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, GADRewardedAd *> *rewardedAdHandles;
@property (nonatomic, strong) GADRewardedAd *rewardedAd;
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, GADRewardedInterstitialAd *> *rewardedInterstitialAdHandles;
@property (nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAd;
// Guards interstitialAdHandles/rewardedAdHandles/rewardedInterstitialAdHandles - load()'s
// completion handler has no documented main-thread guarantee, so it can race is_valid()/
// dispose()/show() (always called from the game thread) and onStop's teardown.
@property (nonatomic, strong) NSObject *adHandlesLock;
@property (nonatomic, strong) GADAppOpenAd *appOpenAd;
@property (nonatomic, strong) NSDate *appOpenAdLoadTime;
@property (nonatomic, assign) UIInterfaceOrientation appOpenAdOrientation;
@property (nonatomic, assign) int appOpenAdExpirationTime;
@property (nonatomic, strong) NSString *serverSideVerificationUserId;
@property (nonatomic, strong) NSString *serverSideVerificationCustomData;
@property (nonatomic, strong) UMPConsentForm *consentForm;
@property (nonatomic, assign) BOOL bannerLoadPending;
@end

@implementation GMAdMob

-(id)init {
    if ( self = [super init] ) {
        
        self.isInitialized = NO;
        self.isTestDevice = NO;
        
        self.bannerAdUnitId = @"";
        self.interstitialAdUnitId = @"";
        self.rewardedUnitId = @"";
        self.rewardedInterstitialAdUnitId = @"";
        self.appOpenAdUnitId = @"";
        
        self.bannerLoadPending = NO;

        self.interstitialAdHandles = [NSMutableDictionary dictionary];
        self.rewardedAdHandles = [NSMutableDictionary dictionary];
        self.rewardedInterstitialAdHandles = [NSMutableDictionary dictionary];
        self.adHandlesLock = [[NSObject alloc] init];

        self.serverSideVerificationUserId = nil;
        self.serverSideVerificationCustomData = nil;

        self.triggerOnPaidEvent = NO;
        self.targetCOPPA = NO;
        self.targetUnderAge = NO;
        
        self.triggerAppOpenAd = NO;
        self.appOpenAdOrientation = UIInterfaceOrientationUnknown;
        self.appOpenAdExpirationTime = 4;
        
        return self;
    }
    return NULL;
}


#pragma mark - Extension Generator API

- (gm_enums::AdMobError)admob_initialize:
    (gm::wire::GMFunction)callback
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::IllegalCall;

    if (self.isTestDevice)
    {
#if TARGET_OS_SIMULATOR
        // GADSimulatorID was removed in Google Mobile Ads SDK 12.0.0 - simulators
        // are now automatically treated as test devices, no identifier needed.
#else
        NSString *device =
            [NSString stringWithCString:getDeviceId()
                                encoding:NSUTF8StringEncoding];

        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers =
            @[device];
#endif
    }

    GADMobileAds *ads =
        [GADMobileAds sharedInstance];

    [ads startWithCompletionHandler:
        ^(GADInitializationStatus *status)
        {
            NSDictionary *adapterStatuses =
                [status adapterStatusesByClassName];

            for (NSString *adapter in adapterStatuses)
            {
                GADAdapterStatus *adapterStatus =
                    adapterStatuses[adapter];

                NSLog(
                    @"Adapter Name: %@, Description: %@, Latency: %f",
                    adapter,
                    adapterStatus.description,
                    adapterStatus.latency
                );
            }

            [self initializeAdUnits];
            self.isInitialized = YES;

            gm_structs::AdMobResult result{};
            result.success = true;
            callback.call(result);
        }];

    return gm_enums::AdMobError::Ok;
}

- (gm_enums::AdMobError)admob_set_test_device_id
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::IllegalCall;

    self.isTestDevice = YES;
    return gm_enums::AdMobError::Ok;
}

- (void)admob_events_on_paid_event:
            (bool)enabled
                            callback:
            (gm::wire::GMFunction)callback
{
    self.triggerOnPaidEvent = enabled;
    g_paid_event_callback = enabled ? callback : nil;
}

- (void)admob_banner_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.bannerAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (gm_enums::AdMobError)admob_banner_create:
            (gm_enums::AdMobBannerSize)size
                         bottom:
            (bool)bottom
                       callback:
            (gm::wire::GMFunction)callback
{
    g_banner_callback = callback;

    double code =
        [self
            createBannerAdViewWithSize:(double)(int32_t)size
            bottom:(bottom ? 1.0 : 0.0)
            alignment:1
            callingMethod:__FUNCTION__];

    if (code != (int)gm_enums::AdMobError::Ok)
        g_banner_callback = nil;

    return (gm_enums::AdMobError)(int)code;
}

- (gm_enums::AdMobError)admob_banner_create_ext:
            (gm_enums::AdMobBannerSize)size
                             bottom:
            (bool)bottom
                          alignment:
            (gm_enums::AdMobBannerAlignment)alignment
                           callback:
            (gm::wire::GMFunction)callback
{
    g_banner_callback = callback;

    double code =
        [self
            createBannerAdViewWithSize:(double)(int32_t)size
            bottom:(bottom ? 1.0 : 0.0)
            alignment:(int)(int32_t)alignment
            callingMethod:__FUNCTION__];

    if (code != (int)gm_enums::AdMobError::Ok)
        g_banner_callback = nil;

    return (gm_enums::AdMobError)(int)code;
}

- (double)admob_banner_get_width
{
    return self.bannerView != nil
        ? self.bannerView.frame.size.width
        : 0.0;
}

- (double)admob_banner_get_height
{
    return self.bannerView != nil
        ? self.bannerView.frame.size.height
        : 0.0;
}

- (void)admob_banner_move:(bool)bottom
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return;

    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return;

    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.bannerView == nil)
            return;

        CGRect frame = self.bannerView.frame;
        CGSize screenSize =
            UIScreen.mainScreen.bounds.size;

        frame.origin.y =
            bottom
                ? screenSize.height - frame.size.height
                : 0;

        self.bannerView.frame = frame;
    });
}

- (void)admob_banner_show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return;

    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return;

    dispatch_async(dispatch_get_main_queue(), ^{
        self.bannerView.hidden = NO;
    });
}

- (void)admob_banner_hide
{
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return;

    dispatch_async(dispatch_get_main_queue(), ^{
        self.bannerView.hidden = YES;
    });
}

- (void)admob_banner_remove
{
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return;

    [self deleteBannerAdView];
    g_banner_callback = nil;
}

- (void)admob_interstitial_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.interstitialAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (gm_enums::AdMobError)admob_interstitial_load:
    (gm::wire::GMFunction)callback
    ad_unit_id:(std::optional<std::string_view>)ad_unit_id
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSString *resolvedAdUnitId =
        ad_unit_id.has_value()
            ? AdMobStringFromStringView(ad_unit_id.value())
            : self.interstitialAdUnitId;

    if (![self validateAdId:resolvedAdUnitId
              callingMethod:__FUNCTION__])
        return gm_enums::AdMobError::InvalidAdId;

    GADRequest *request =
        [self buildAdRequest];

    [GADInterstitialAd
        loadWithAdUnitID:resolvedAdUnitId
        request:request
        completionHandler:
            ^(GADInterstitialAd *interstitialAd,
              NSError *error)
            {
                if (error)
                {
                    gm_structs::AdMobResult result{};
                    result.success = false;
                    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)error.code;

                    callback.call(result);
                    return;
                }

                if (self.triggerOnPaidEvent)
                {
                    __weak GMAdMob *weakSelf = self;
                    __weak GADInterstitialAd *weakInterstitialAd =
                        interstitialAd;

                    interstitialAd.paidEventHandler =
                        ^void(GADAdValue *_Nonnull value)
                        {
                            GADAdNetworkResponseInfo *responseInfo =
                                weakInterstitialAd.responseInfo
                                    .loadedAdNetworkResponseInfo;

                            NSString *adapterClassName =
                                weakInterstitialAd.responseInfo
                                    .adNetworkInfoArray.firstObject
                                    .adNetworkClassName;

                            [weakSelf
                                onPaidEventHandler:value
                                adUnitId:weakInterstitialAd.adUnitID
                                adType:gm_enums::AdMobAdType::Interstitial
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                int64_t handle = g_next_ad_handle.fetch_add(1);
                @synchronized (self.adHandlesLock) {
                    self.interstitialAdHandles[@(handle)] = interstitialAd;
                }

                gm_structs::AdMobResult result{};
                result.success = true;

                callback.call(result, (std::uint64_t)handle);
            }];

    return gm_enums::AdMobError::Ok;
}

- (bool)admob_interstitial_is_valid:
    (std::uint64_t)handle
{
    @synchronized (self.adHandlesLock) {
        return self.interstitialAdHandles[@((int64_t)handle)] != nil;
    }
}

- (void)admob_interstitial_dispose:
    (std::uint64_t)handle
{
    NSNumber *key = @((int64_t)handle);
    GADInterstitialAd *ad = nil;
    @synchronized (self.adHandlesLock) {
        ad = self.interstitialAdHandles[key];
        [self.interstitialAdHandles removeObjectForKey:key];
    }

    [self cleanAd:ad
      withCleaner:^(id ad)
    {
        [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
    }];
}

- (gm_enums::AdMobError)admob_interstitial_show:
    (std::uint64_t)handle
    callback:(gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSNumber *key = @((int64_t)handle);
    GADInterstitialAd *interstitialAd = nil;
    @synchronized (self.adHandlesLock) {
        interstitialAd = self.interstitialAdHandles[key];
        if (interstitialAd != nil)
            [self.interstitialAdHandles removeObjectForKey:key];
    }

    if (interstitialAd == nil)
        return gm_enums::AdMobError::InvalidHandle;

    g_interstitial_show_callbacks[(__bridge void *)interstitialAd] = callback;
    interstitialAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [interstitialAd presentFromRootViewController:g_controller];
    });

    self.interstitialAd = interstitialAd;
    self.isShowingAd = YES;

    return gm_enums::AdMobError::Ok;
}

- (void)admob_server_side_verification_set:
            (std::string_view)user_id
                                      custom_data:
            (std::string_view)custom_data
{
    self.serverSideVerificationUserId =
        AdMobStringFromStringView(user_id);

    self.serverSideVerificationCustomData =
        AdMobStringFromStringView(custom_data);
}

- (void)admob_server_side_verification_clear
{
    self.serverSideVerificationUserId = nil;
    self.serverSideVerificationCustomData = nil;
}

- (void)admob_rewarded_video_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.rewardedUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (gm_enums::AdMobError)admob_rewarded_video_load:
    (gm::wire::GMFunction)callback
    ad_unit_id:(std::optional<std::string_view>)ad_unit_id
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSString *resolvedAdUnitId =
        ad_unit_id.has_value()
            ? AdMobStringFromStringView(ad_unit_id.value())
            : self.rewardedUnitId;

    if (![self validateAdId:resolvedAdUnitId
              callingMethod:__FUNCTION__])
        return gm_enums::AdMobError::InvalidAdId;

    GADRequest *request =
        [self buildAdRequest];

    [self
        configureServerSideVerification:request
        withUserId:self.serverSideVerificationUserId
        customData:self.serverSideVerificationCustomData];

    [GADRewardedAd
        loadWithAdUnitID:resolvedAdUnitId
        request:request
        completionHandler:
            ^(GADRewardedAd *rewardedAd,
              NSError *error)
            {
                if (error)
                {
                    gm_structs::AdMobResult result{};
                    result.success = false;
                    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)error.code;

                    callback.call(result);
                    return;
                }

                if (self.triggerOnPaidEvent)
                {
                    __weak GMAdMob *weakSelf = self;
                    __weak GADRewardedAd *weakRewardedAd =
                        rewardedAd;

                    rewardedAd.paidEventHandler =
                        ^void(GADAdValue *_Nonnull value)
                        {
                            GADAdNetworkResponseInfo *responseInfo =
                                weakRewardedAd.responseInfo
                                    .loadedAdNetworkResponseInfo;

                            NSString *adapterClassName =
                                weakRewardedAd.responseInfo
                                    .adNetworkInfoArray.firstObject
                                    .adNetworkClassName;

                            [weakSelf
                                onPaidEventHandler:value
                                adUnitId:weakRewardedAd.adUnitID
                                adType:gm_enums::AdMobAdType::RewardedVideo
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                int64_t handle = g_next_ad_handle.fetch_add(1);
                @synchronized (self.adHandlesLock) {
                    self.rewardedAdHandles[@(handle)] = rewardedAd;
                }

                gm_structs::AdMobResult result{};
                result.success = true;

                callback.call(result, (std::uint64_t)handle);
            }];

    return gm_enums::AdMobError::Ok;
}

- (bool)admob_rewarded_video_is_valid:
    (std::uint64_t)handle
{
    @synchronized (self.adHandlesLock) {
        return self.rewardedAdHandles[@((int64_t)handle)] != nil;
    }
}

- (void)admob_rewarded_video_dispose:
    (std::uint64_t)handle
{
    NSNumber *key = @((int64_t)handle);
    GADRewardedAd *ad = nil;
    @synchronized (self.adHandlesLock) {
        ad = self.rewardedAdHandles[key];
        [self.rewardedAdHandles removeObjectForKey:key];
    }

    [self cleanAd:ad
      withCleaner:^(id ad)
    {
        [self cleanUpRewardedAd:(GADRewardedAd *)ad];
    }];
}

- (gm_enums::AdMobError)admob_rewarded_video_show:
    (std::uint64_t)handle
    callback:(gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSNumber *key = @((int64_t)handle);
    GADRewardedAd *rewardedAd = nil;
    @synchronized (self.adHandlesLock) {
        rewardedAd = self.rewardedAdHandles[key];
        if (rewardedAd != nil)
            [self.rewardedAdHandles removeObjectForKey:key];
    }

    if (rewardedAd == nil)
        return gm_enums::AdMobError::InvalidHandle;

    g_rewarded_video_show_callbacks[(__bridge void *)rewardedAd] = callback;
    rewardedAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [rewardedAd
            presentFromRootViewController:g_controller
            userDidEarnRewardHandler:^
            {
                gm_structs::AdMobResult result{};
                result.success = true;

                gm_structs::AdMobReward reward{};
                reward.amount = rewardedAd.adReward.amount.doubleValue;
                reward.type = std::string(AdMobCString(rewardedAd.adReward.type));

                callback.call(result, gm_enums::AdMobRewardedVideoShowEvent::Reward, reward);
            }];
    });

    self.rewardedAd = rewardedAd;
    self.isShowingAd = YES;

    return gm_enums::AdMobError::Ok;
}

- (void)admob_rewarded_interstitial_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.rewardedInterstitialAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (gm_enums::AdMobError)admob_rewarded_interstitial_load:
    (gm::wire::GMFunction)callback
    ad_unit_id:(std::optional<std::string_view>)ad_unit_id
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSString *resolvedAdUnitId =
        ad_unit_id.has_value()
            ? AdMobStringFromStringView(ad_unit_id.value())
            : self.rewardedInterstitialAdUnitId;

    if (![self validateAdId:resolvedAdUnitId
              callingMethod:__FUNCTION__])
        return gm_enums::AdMobError::InvalidAdId;

    GADRequest *request =
        [self buildAdRequest];

    [self
        configureServerSideVerification:request
        withUserId:self.serverSideVerificationUserId
        customData:self.serverSideVerificationCustomData];

    [GADRewardedInterstitialAd
        loadWithAdUnitID:resolvedAdUnitId
        request:request
        completionHandler:
            ^(GADRewardedInterstitialAd *rewardedInterstitialAd,
              NSError *error)
            {
                if (error)
                {
                    gm_structs::AdMobResult result{};
                    result.success = false;
                    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)error.code;

                    callback.call(result);
                    return;
                }

                if (self.triggerOnPaidEvent)
                {
                    __weak GMAdMob *weakSelf = self;
                    __weak GADRewardedInterstitialAd *weakAd =
                        rewardedInterstitialAd;

                    rewardedInterstitialAd.paidEventHandler =
                        ^void(GADAdValue *_Nonnull value)
                        {
                            GADAdNetworkResponseInfo *responseInfo =
                                weakAd.responseInfo
                                    .loadedAdNetworkResponseInfo;

                            NSString *adapterClassName =
                                weakAd.responseInfo
                                    .adNetworkInfoArray.firstObject
                                    .adNetworkClassName;

                            [weakSelf
                                onPaidEventHandler:value
                                adUnitId:weakAd.adUnitID
                                adType:gm_enums::AdMobAdType::RewardedInterstitial
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                int64_t handle = g_next_ad_handle.fetch_add(1);
                @synchronized (self.adHandlesLock) {
                    self.rewardedInterstitialAdHandles[@(handle)] = rewardedInterstitialAd;
                }

                gm_structs::AdMobResult result{};
                result.success = true;

                callback.call(result, (std::uint64_t)handle);
            }];

    return gm_enums::AdMobError::Ok;
}

- (bool)admob_rewarded_interstitial_is_valid:
    (std::uint64_t)handle
{
    @synchronized (self.adHandlesLock) {
        return self.rewardedInterstitialAdHandles[@((int64_t)handle)] != nil;
    }
}

- (void)admob_rewarded_interstitial_dispose:
    (std::uint64_t)handle
{
    NSNumber *key = @((int64_t)handle);
    GADRewardedInterstitialAd *ad = nil;
    @synchronized (self.adHandlesLock) {
        ad = self.rewardedInterstitialAdHandles[key];
        [self.rewardedInterstitialAdHandles removeObjectForKey:key];
    }

    [self cleanAd:ad
      withCleaner:^(id ad)
    {
        [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
    }];
}

- (gm_enums::AdMobError)admob_rewarded_interstitial_show:
    (std::uint64_t)handle
    callback:(gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    NSNumber *key = @((int64_t)handle);
    GADRewardedInterstitialAd *rewardedInterstitialAd = nil;
    @synchronized (self.adHandlesLock) {
        rewardedInterstitialAd = self.rewardedInterstitialAdHandles[key];
        if (rewardedInterstitialAd != nil)
            [self.rewardedInterstitialAdHandles removeObjectForKey:key];
    }

    if (rewardedInterstitialAd == nil)
        return gm_enums::AdMobError::InvalidHandle;

    g_rewarded_interstitial_show_callbacks[(__bridge void *)rewardedInterstitialAd] = callback;
    rewardedInterstitialAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [rewardedInterstitialAd
            presentFromRootViewController:g_controller
            userDidEarnRewardHandler:^
            {
                gm_structs::AdMobResult result{};
                result.success = true;

                gm_structs::AdMobReward reward{};
                reward.amount = rewardedInterstitialAd.adReward.amount.doubleValue;
                reward.type = std::string(AdMobCString(rewardedInterstitialAd.adReward.type));

                callback.call(result, gm_enums::AdMobRewardedInterstitialShowEvent::Reward, reward);
            }];
    });

    self.rewardedInterstitialAd =
        rewardedInterstitialAd;

    self.isShowingAd = YES;

    return gm_enums::AdMobError::Ok;
}

- (void)admob_app_open_ad_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.appOpenAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (gm_enums::AdMobError)admob_app_open_ad_enable:
            (gm::wire::GMFunction)callback
{
    g_app_open_enable_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        g_app_open_enable_callback = nil;
        return gm_enums::AdMobError::NotInitialized;
    }

    if (![self validateAdId:self.appOpenAdUnitId
              callingMethod:__FUNCTION__])
    {
        g_app_open_enable_callback = nil;
        return gm_enums::AdMobError::InvalidAdId;
    }

    self.triggerAppOpenAd = YES;

    if (![self appOpenAdIsValid:__FUNCTION__])
        return [self admob_app_open_ad_load:callback];

    return gm_enums::AdMobError::Ok;
}

- (void)admob_app_open_ad_disable
{
    self.triggerAppOpenAd = NO;
    g_app_open_enable_callback = nil;
}

- (bool)admob_app_open_ad_is_enabled
{
    return self.triggerAppOpenAd;
}

- (bool)admob_app_open_ad_is_loaded
{
    return [self appOpenAdIsValid:__FUNCTION__];
}

- (gm_enums::AdMobError)admob_app_open_ad_load:
    (gm::wire::GMFunction)callback
{
    g_app_open_load_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    if (![self validateAdId:self.appOpenAdUnitId
              callingMethod:__FUNCTION__])
        return gm_enums::AdMobError::InvalidAdId;

    if ([self appOpenAdIsValid:__FUNCTION__])
        return gm_enums::AdMobError::Ok;

    NSString *adUnitId =
        self.appOpenAdUnitId;

    @synchronized (self.adHandlesLock) {
        self.appOpenAd = nil;
        self.appOpenAdOrientation =
            [[UIApplication sharedApplication] statusBarOrientation];
    }

    GADRequest *request =
        [self buildAdRequest];

    [GADAppOpenAd
        loadWithAdUnitID:self.appOpenAdUnitId
        request:request
        completionHandler:
            ^(GADAppOpenAd *_Nullable appOpenAd,
              NSError *_Nullable error)
            {
                gm::wire::GMFunction resolvedCallback =
                    g_app_open_load_callback ? g_app_open_load_callback : g_app_open_enable_callback;
                g_app_open_load_callback = nil;

                if (error)
                {
                    gm_structs::AdMobResult result{};
                    result.success = false;
                    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)error.code;

                    if (resolvedCallback)
                        resolvedCallback.call(result);

                    return;
                }

                @synchronized (self.adHandlesLock) {
                    self.appOpenAd = appOpenAd;
                    self.appOpenAdLoadTime = [NSDate date];
                }

                if (self.triggerOnPaidEvent)
                {
                    __weak GMAdMob *weakSelf = self;

                    self.appOpenAd.paidEventHandler =
                        ^void(GADAdValue *_Nonnull value)
                        {
                            GADAdNetworkResponseInfo *responseInfo =
                                weakSelf.appOpenAd.responseInfo
                                    .loadedAdNetworkResponseInfo;

                            NSString *adapterClassName =
                                weakSelf.appOpenAd.responseInfo
                                    .adNetworkInfoArray.firstObject
                                    .adNetworkClassName;

                            [weakSelf
                                onPaidEventHandler:value
                                adUnitId:adUnitId
                                adType:gm_enums::AdMobAdType::AppOpen
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                gm_structs::AdMobResult result{};
                result.success = true;

                if (resolvedCallback)
                    resolvedCallback.call(result);
            }];

    return gm_enums::AdMobError::Ok;
}

- (gm_enums::AdMobError)admob_app_open_ad_show:
    (gm::wire::GMFunction)callback
{
    g_app_open_show_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return gm_enums::AdMobError::NotInitialized;

    if (![self appOpenAdIsValid:__FUNCTION__])
        return gm_enums::AdMobError::NoAdsLoaded;

    self.appOpenAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.appOpenAd presentFromRootViewController:g_controller];
    });

    self.isShowingAd = YES;

    return gm_enums::AdMobError::Ok;
}

- (void)admob_targeting_coppa:(bool)coppa
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return;

    self.targetCOPPA = coppa;
}

- (void)admob_targeting_under_age:(bool)under_age
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return;

    self.targetUnderAge = under_age;
}

- (void)admob_targeting_max_ad_content_rating:
    (gm_enums::AdMobMaxAdContentRating)content_rating
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return;

    switch ((int32_t)content_rating)
    {
        case 0:
            [GADMobileAds.sharedInstance.requestConfiguration
                setMaxAdContentRating:GADMaxAdContentRatingGeneral];
            break;

        case 1:
            [GADMobileAds.sharedInstance.requestConfiguration
                setMaxAdContentRating:GADMaxAdContentRatingParentalGuidance];
            break;

        case 2:
            [GADMobileAds.sharedInstance.requestConfiguration
                setMaxAdContentRating:GADMaxAdContentRatingTeen];
            break;

        case 3:
            [GADMobileAds.sharedInstance.requestConfiguration
                setMaxAdContentRating:GADMaxAdContentRatingMatureAudience];
            break;
    }
}

- (gm_enums::AdMobError)admob_consent_request_info_update:
            (gm_enums::AdMobConsentDebugGeography)debug_geography
                                      callback:
            (gm::wire::GMFunction)callback
{
    UMPRequestParameters *parameters =
        [[UMPRequestParameters alloc] init];

    double testing =
        (double)(int32_t)debug_geography;

    if (testing > 0)
    {
        UMPDebugSettings *debugSettings =
            [[UMPDebugSettings alloc] init];

        debugSettings.testDeviceIdentifiers =
            @[[[[UIDevice currentDevice]
                identifierForVendor] UUIDString]];

        debugSettings.geography =
            (UMPDebugGeography)testing;

        parameters.debugSettings =
            debugSettings;
    }

    [UMPConsentInformation.sharedInstance
        requestConsentInfoUpdateWithParameters:parameters
        completionHandler:
            ^(NSError *_Nullable error)
            {
                gm_structs::AdMobResult result{};

                if (error)
                {
                    result.success = false;
                    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)error.code;
                }
                else
                {
                    result.success = true;
                }

                callback.call(result);
            }];

    return gm_enums::AdMobError::Ok;
}

- (gm_enums::AdMobConsentStatus)admob_consent_get_status
{
    // NOTE: iOS UMP SDK has inverted enum values vs Android!
    // iOS: Unknown=0, Required=1, NotRequired=2, Obtained=3
    // Android: Unknown=0, NotRequired=1, Required=2, Obtained=3
    // Convert iOS values to Android/spec values for consistency
    UMPConsentStatus iosStatus = UMPConsentInformation.sharedInstance.consentStatus;
    switch (iosStatus) {
        case UMPConsentStatusUnknown:
            return gm_enums::AdMobConsentStatus::Unknown;
        case UMPConsentStatusRequired:
            return gm_enums::AdMobConsentStatus::Required; // 1 in iOS -> 2 in Android
        case UMPConsentStatusNotRequired:
            return gm_enums::AdMobConsentStatus::NotRequired; // 2 in iOS -> 1 in Android
        case UMPConsentStatusObtained:
            return gm_enums::AdMobConsentStatus::Obtained;
        default:
            return gm_enums::AdMobConsentStatus::Unknown;
    }
}

- (gm_enums::AdMobConsentType)admob_consent_get_type
{
    // Note: Direct comparison with iOS UMP enum value (UMPConsentStatusObtained = 3)
    if (UMPConsentInformation.sharedInstance.consentStatus == UMPConsentStatusObtained) {
        if (!canShowAds())
            return gm_enums::AdMobConsentType::Declined;
        return canShowPersonalizedAds() ? gm_enums::AdMobConsentType::Personalized : gm_enums::AdMobConsentType::NonPersonalized;
    }
    return gm_enums::AdMobConsentType::Unknown;
}

- (bool)admob_consent_is_form_available
{
    return UMPConsentInformation.sharedInstance.formStatus ==
        UMPFormStatusAvailable;
}

- (gm_enums::AdMobError)admob_consent_load:
    (gm::wire::GMFunction)callback
{
    [UMPConsentForm
        loadWithCompletionHandler:
            ^(UMPConsentForm *form,
              NSError *loadError)
            {
                if (loadError)
                {
                    gm_structs::AdMobResult result{};
                    result.success = false;
                    result.error_message = std::string(AdMobCString([loadError.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)loadError.code;

                    callback.call(result);
                    return;
                }

                @synchronized (self.adHandlesLock) {
                    self.consentForm = form;
                }

                gm_structs::AdMobResult result{};
                result.success = true;
                callback.call(result);
            }];

    return gm_enums::AdMobError::Ok;
}

- (gm_enums::AdMobError)admob_consent_show:
    (gm::wire::GMFunction)callback
{
    UMPConsentForm *consentForm;
    @synchronized (self.adHandlesLock) {
        consentForm = self.consentForm;
    }

    if (consentForm == nil)
        return gm_enums::AdMobError::NoAdsLoaded;

    [consentForm
        presentFromViewController:g_controller
        completionHandler:
            ^(NSError *dismissError)
            {
                gm_structs::AdMobResult result{};

                if (dismissError)
                {
                    result.success = false;
                    result.error_message = std::string(AdMobCString([dismissError.localizedDescription copy]));
                    result.sdk_error_code = (std::int32_t)dismissError.code;
                }
                else
                {
                    result.success = true;
                }

                callback.call(result);

                @synchronized (self.adHandlesLock) {
                    self.consentForm = nil;
                }
            }];

    return gm_enums::AdMobError::Ok;
}

- (void)admob_consent_reset
{
    [UMPConsentInformation.sharedInstance reset];
}

- (void)admob_consent_set_rdp:(bool)enabled
{
    self.isRdpEnabled = enabled;
}

- (void)admob_settings_set_volume:(double)value
{
    [GADMobileAds.sharedInstance setApplicationVolume:value];
}

- (void)admob_settings_set_muted:(bool)muted
{
    [GADMobileAds.sharedInstance setApplicationMuted:muted];
}

#pragma mark - Setup Methods
- (void)initializeAdUnits
{
    NSDictionary *adUnitKeys = @{
        @"iOS_BANNER": @"bannerAdUnitId",
        @"iOS_INTERSTITIAL": @"interstitialAdUnitId",
        @"iOS_REWARDED": @"rewardedUnitId",
        @"iOS_REWARDED_INTERSTITIAL": @"rewardedInterstitialAdUnitId",
        @"iOS_OPENAPPAD": @"appOpenAdUnitId"
    };

    for (NSString *key in adUnitKeys)
    {
        const char *temp =
            extOptGetString((char*)"GMAdMob", (char*)[key UTF8String]);

        if (temp == nullptr || strlen(temp) == 0)
        {
            temp =
                extOptGetString((char*)"AdMob", (char*)[key UTF8String]);
        }

        if (temp != nullptr && strlen(temp) > 0)
        {
            NSString *adUnit =
                [NSString stringWithUTF8String:temp];

            [self setValue:adUnit forKey:adUnitKeys[key]];
        }
    }
}

#pragma mark - Delegate Methods

-(void)bannerView:(nonnull GADBannerView *)bannerView didFailToReceiveAdWithError:(nonnull NSError *)error
{
    self.bannerLoadPending = NO;

    gm_structs::AdMobResult result{};
    result.success = false;
    result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
    result.sdk_error_code = (std::int32_t)error.code;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::LoadFailed);
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView
{
    self.bannerLoadPending = NO;

    gm_structs::AdMobResult result{};
    result.success = true;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::Loaded);
}

- (void)bannerViewWillPresentScreen:(GADBannerView *)bannerView {
    gm_structs::AdMobResult result{};
    result.success = true;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::Opened);
}

- (void)bannerViewWillDismissScreen:(GADBannerView *)bannerView {
	//This event doesn't exists on Andorid, ignore...
}

- (void)bannerViewDidDismissScreen:(GADBannerView *)bannerView {
    gm_structs::AdMobResult result{};
    result.success = true;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::Closed);
}

- (void)bannerViewDidRecordClick:(GADBannerView *)bannerView {
    gm_structs::AdMobResult result{};
    result.success = true;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::Clicked);
}

- (void)bannerViewDidRecordImpression:(GADBannerView *)bannerView {
    gm_structs::AdMobResult result{};
    result.success = true;

    if (g_banner_callback)
        g_banner_callback.call(result, gm_enums::AdMobBannerCallbackEvent::Impression);
}

- (void)invokeInterstitialShowEvent:(GADInterstitialAd *)ad
                                type:(gm_enums::AdMobInterstitialShowEvent)type
{
    auto it = g_interstitial_show_callbacks.find((__bridge void *)ad);
    if (it == g_interstitial_show_callbacks.end())
        return;

    gm_structs::AdMobResult result{};
    result.success = true;

    it->second.call(result, type);
}

- (void)invokeRewardedVideoShowEvent:(GADRewardedAd *)ad
                                 type:(gm_enums::AdMobRewardedVideoShowEvent)type
{
    auto it = g_rewarded_video_show_callbacks.find((__bridge void *)ad);
    if (it == g_rewarded_video_show_callbacks.end())
        return;

    gm_structs::AdMobResult result{};
    result.success = true;

    it->second.call(result, type);
}

- (void)invokeRewardedInterstitialShowEvent:(GADRewardedInterstitialAd *)ad
                                        type:(gm_enums::AdMobRewardedInterstitialShowEvent)type
{
    auto it = g_rewarded_interstitial_show_callbacks.find((__bridge void *)ad);
    if (it == g_rewarded_interstitial_show_callbacks.end())
        return;

    gm_structs::AdMobResult result{};
    result.success = true;

    it->second.call(result, type);
}

// App open is single-instance (not a handle map), so unlike the three helpers above this
// resolves against the persistent show/enable callback slots directly, same fallback as Shown.
- (void)invokeAppOpenShowEvent:(gm_enums::AdMobAppOpenAdShowEvent)type
{
    gm::wire::GMFunction resolvedCallback =
        g_app_open_show_callback ? g_app_open_show_callback : g_app_open_enable_callback;

    if (!resolvedCallback)
        return;

    gm_structs::AdMobResult result{};
    result.success = true;

    resolvedCallback.call(result, type);
}

-(void)ad:(nonnull id<GADFullScreenPresentingAd>)presentingAd didFailToPresentFullScreenContentWithError:(nonnull NSError *)error
{
    self.isShowingAd = NO;

    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        GADInterstitialAd *interstitialAd = (GADInterstitialAd *)presentingAd;
        self.interstitialAd = nil;

        [self cleanAd:interstitialAd
          withCleaner:^(id ad)
        {
            [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
        }];

        auto it = g_interstitial_show_callbacks.find((__bridge void *)interstitialAd);
        if (it != g_interstitial_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_interstitial_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = false;
            result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
            result.sdk_error_code = (std::int32_t)error.code;

            callback.call(result);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        GADRewardedAd *rewardedAd = (GADRewardedAd *)presentingAd;
        self.rewardedAd = nil;

        [self cleanAd:rewardedAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedAd:(GADRewardedAd *)ad];
        }];

        auto it = g_rewarded_video_show_callbacks.find((__bridge void *)rewardedAd);
        if (it != g_rewarded_video_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_rewarded_video_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = false;
            result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
            result.sdk_error_code = (std::int32_t)error.code;

            callback.call(result);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        GADRewardedInterstitialAd *rewardedInterstitialAd = (GADRewardedInterstitialAd *)presentingAd;
        self.rewardedInterstitialAd = nil;

        [self cleanAd:rewardedInterstitialAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
        }];

        auto it = g_rewarded_interstitial_show_callbacks.find((__bridge void *)rewardedInterstitialAd);
        if (it != g_rewarded_interstitial_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_rewarded_interstitial_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = false;
            result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
            result.sdk_error_code = (std::int32_t)error.code;

            callback.call(result);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADAppOpenAd class]]) {
        @synchronized (self.adHandlesLock) {
            self.appOpenAd = nil;
        }

        gm::wire::GMFunction resolvedCallback =
            g_app_open_show_callback ? g_app_open_show_callback : g_app_open_enable_callback;
        g_app_open_show_callback = nil;

        gm_structs::AdMobResult result{};
        result.success = false;
        result.error_message = std::string(AdMobCString([error.localizedDescription copy]));
        result.sdk_error_code = (std::int32_t)error.code;

        if (resolvedCallback)
            resolvedCallback.call(result);

        // If AppOpenAd is being automatically managed
        if (self.triggerAppOpenAd) {
            // Reload the App Open Ad after failure
            [self admob_app_open_ad_load:g_app_open_enable_callback];
        }
    }
}

-(void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        [self invokeInterstitialShowEvent:(GADInterstitialAd *)presentingAd
                                      type:gm_enums::AdMobInterstitialShowEvent::Shown];
        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        [self invokeRewardedVideoShowEvent:(GADRewardedAd *)presentingAd
                                       type:gm_enums::AdMobRewardedVideoShowEvent::Shown];
        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        [self invokeRewardedInterstitialShowEvent:(GADRewardedInterstitialAd *)presentingAd
                                              type:gm_enums::AdMobRewardedInterstitialShowEvent::Shown];
        return;
    }

    if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        gm::wire::GMFunction resolvedCallback =
            g_app_open_show_callback ? g_app_open_show_callback : g_app_open_enable_callback;

        gm_structs::AdMobResult result{};
        result.success = true;

        if (resolvedCallback)
            resolvedCallback.call(result, gm_enums::AdMobAppOpenAdShowEvent::Shown);

        if (self.triggerAppOpenAd)
            [self admob_app_open_ad_load:g_app_open_enable_callback];
    }
}

// Unverified against the real GoogleMobileAds SDK header (not vendored in this repo) -
// adDidRecordImpression:/adDidRecordClick: are the stable, long-standing selector names
// on GADFullScreenContentDelegate; confirm against the real header on the next Xcode build.
-(void)adDidRecordImpression:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        [self invokeInterstitialShowEvent:(GADInterstitialAd *)presentingAd
                                      type:gm_enums::AdMobInterstitialShowEvent::Impression];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        [self invokeRewardedVideoShowEvent:(GADRewardedAd *)presentingAd
                                       type:gm_enums::AdMobRewardedVideoShowEvent::Impression];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        [self invokeRewardedInterstitialShowEvent:(GADRewardedInterstitialAd *)presentingAd
                                              type:gm_enums::AdMobRewardedInterstitialShowEvent::Impression];
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        [self invokeAppOpenShowEvent:gm_enums::AdMobAppOpenAdShowEvent::Impression];
    }
}

-(void)adDidRecordClick:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        [self invokeInterstitialShowEvent:(GADInterstitialAd *)presentingAd
                                      type:gm_enums::AdMobInterstitialShowEvent::Clicked];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        [self invokeRewardedVideoShowEvent:(GADRewardedAd *)presentingAd
                                       type:gm_enums::AdMobRewardedVideoShowEvent::Clicked];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        [self invokeRewardedInterstitialShowEvent:(GADRewardedInterstitialAd *)presentingAd
                                              type:gm_enums::AdMobRewardedInterstitialShowEvent::Clicked];
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        [self invokeAppOpenShowEvent:gm_enums::AdMobAppOpenAdShowEvent::Clicked];
    }
}

-(void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        self.isShowingAd = NO;

        GADInterstitialAd *interstitialAd = (GADInterstitialAd *)presentingAd;

        [self cleanAd:interstitialAd
          withCleaner:^(id ad)
        {
            [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
        }];

        self.interstitialAd = nil;

        auto it = g_interstitial_show_callbacks.find((__bridge void *)interstitialAd);
        if (it != g_interstitial_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_interstitial_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = true;

            callback.call(result, gm_enums::AdMobInterstitialShowEvent::Dismissed);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        self.isShowingAd = NO;

        GADRewardedAd *rewardedAd = (GADRewardedAd *)presentingAd;

        [self cleanAd:rewardedAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedAd:(GADRewardedAd *)ad];
        }];

        self.rewardedAd = nil;

        auto it = g_rewarded_video_show_callbacks.find((__bridge void *)rewardedAd);
        if (it != g_rewarded_video_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_rewarded_video_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = true;

            callback.call(result, gm_enums::AdMobRewardedVideoShowEvent::Dismissed);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        self.isShowingAd = NO;

        GADRewardedInterstitialAd *rewardedInterstitialAd = (GADRewardedInterstitialAd *)presentingAd;

        [self cleanAd:rewardedInterstitialAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
        }];

        self.rewardedInterstitialAd = nil;

        auto it = g_rewarded_interstitial_show_callbacks.find((__bridge void *)rewardedInterstitialAd);
        if (it != g_rewarded_interstitial_show_callbacks.end())
        {
            gm::wire::GMFunction callback = it->second;
            g_rewarded_interstitial_show_callbacks.erase(it);

            gm_structs::AdMobResult result{};
            result.success = true;

            callback.call(result, gm_enums::AdMobRewardedInterstitialShowEvent::Dismissed);
        }

        return;
    }

    if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        self.isShowingAd = NO;

        [self cleanAd:(GADAppOpenAd *)presentingAd
          withCleaner:^(id ad)
        {
            [self cleanUpAppOpenAd:(GADAppOpenAd *)ad];
        }];

        @synchronized (self.adHandlesLock) {
            self.appOpenAd = nil;
        }

        gm::wire::GMFunction resolvedCallback =
            g_app_open_show_callback ? g_app_open_show_callback : g_app_open_enable_callback;
        g_app_open_show_callback = nil;

        if (resolvedCallback)
        {
            gm_structs::AdMobResult result{};
            result.success = true;
            resolvedCallback.call(result, gm_enums::AdMobAppOpenAdShowEvent::Dismissed);
        }

        if (self.triggerAppOpenAd)
            [self admob_app_open_ad_load:g_app_open_enable_callback];
    }
}

#pragma mark - Banner Methods
- (double)createBannerAdViewWithSize:(double)size bottom:(double)bottom alignment:(int)alignment callingMethod:(const char *)callingMethod
{
    // Validate initialization
    if (![self validateInitializedWithCallingMethod:callingMethod]) {
        return (int)gm_enums::AdMobError::NotInitialized;
    }
    
    // Validate Ad Unit ID
    if (![self validateAdId:self.bannerAdUnitId callingMethod:callingMethod]) {
        return (int)gm_enums::AdMobError::InvalidAdId;
    }

    // Reject a concurrent create while a previous create's load is still in
    // flight - deleteBannerAdView below would otherwise silently drop the
    // still-pending callback of the banner being replaced.
    if (self.bannerLoadPending) {
        return (int)gm_enums::AdMobError::IllegalCall;
    }

    // Remove the previous banner view if it exists
    if (self.bannerView != nil) {
        [self deleteBannerAdView];
    }

    self.bannerLoadPending = YES;

    // UIKit view construction/layout must happen on the main thread - every
    // other banner mutation (move/show/hide/remove) is already dispatched
    // there, this was the one inconsistent entry point.
    dispatch_async(dispatch_get_main_queue(), ^{
        // Create and configure the banner view
        GADAdSize bannerSize = getBannerSize(size);
        GADBannerView *bannerView = [[GADBannerView alloc] initWithAdSize:bannerSize];

        bannerView.translatesAutoresizingMaskIntoConstraints = NO;
        bannerView.adUnitID = self.bannerAdUnitId;
        bannerView.rootViewController = g_controller;
        bannerView.delegate = self;

        // Assign the banner view to the instance variable
        self.bannerView = bannerView;

        // Store the alignment for future reference
        self.currentBannerAlignment = alignment;

        // Set up paid event handler if necessary
        if (self.triggerOnPaidEvent) {
            __weak GMAdMob *weakSelf = self;
            __weak GADBannerView *weakBannerView = bannerView;
            bannerView.paidEventHandler = ^void(GADAdValue *_Nonnull value) {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = weakBannerView.responseInfo.loadedAdNetworkResponseInfo;
                [weakSelf onPaidEventHandler:value
                                    adUnitId:weakBannerView.adUnitID
                                      adType:gm_enums::AdMobAdType::Banner
                 loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo
                   mediationAdapterClassName:weakBannerView.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
            };
        }

        // Determine the vertical position based on the 'bottom' parameter
        NSLayoutAttribute position = (bottom > 0.5) ? NSLayoutAttributeBottom : NSLayoutAttributeTop;

        // Position the banner using the helper method with alignment
        [self addBannerView:self.bannerView
                 toPosition:position
                  alignment:self.currentBannerAlignment];

        // Load the ad request
        GADRequest *request = [self buildAdRequest];
        [bannerView loadRequest:request];
    });

    return (int)gm_enums::AdMobError::Ok;
}

- (void)addBannerView:(UIView *)bannerView toPosition:(NSLayoutAttribute)position alignment:(int)alignment
{
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [g_glView addSubview:bannerView];
    
    // Vertical Constraints: Adjust the positioning logic
    if (position == NSLayoutAttributeTop) {
        // Align to the top safely
        [g_glView addConstraint:[bannerView.topAnchor constraintEqualToAnchor:g_controller.view.safeAreaLayoutGuide.topAnchor]];
    } else {
        // Align to the bottom safely, accounting for the banner's height
        [g_glView addConstraint:[bannerView.bottomAnchor constraintEqualToAnchor:g_controller.view.safeAreaLayoutGuide.bottomAnchor]];
    }
    
    // Horizontal Constraints based on alignment
    NSLayoutConstraint *horizontalConstraint;
    switch (alignment) {
        case (int)gm_enums::AdMobBannerAlignment::Left:
            horizontalConstraint = [bannerView.leadingAnchor constraintEqualToAnchor:g_controller.view.leadingAnchor];
            break;
        case (int)gm_enums::AdMobBannerAlignment::Center:
            horizontalConstraint = [bannerView.centerXAnchor constraintEqualToAnchor:g_controller.view.centerXAnchor];
            break;
        case (int)gm_enums::AdMobBannerAlignment::Right:
            horizontalConstraint = [bannerView.trailingAnchor constraintEqualToAnchor:g_controller.view.trailingAnchor];
            break;
        default:
            NSLog(@"Invalid alignment value provided. Defaulting to center alignment.");
            horizontalConstraint = [bannerView.centerXAnchor constraintEqualToAnchor:g_controller.view.centerXAnchor];
            break;
    }
    
    [g_glView addConstraint:horizontalConstraint];
}

-(void) deleteBannerAdView
{
    [self cleanAd:self.bannerView withCleaner:^(id ad){
        [self cleanUpBannerView:(GADBannerView *)ad];
    }];
    self.bannerView = nil;
}

static GADAdSize getBannerSize(double size)
{
    switch((int)size)
    {
        case 0: return GADAdSizeBanner;
        case 1: return GADAdSizeLargeBanner;
        case 2: return GADAdSizeMediumRectangle;
        case 3: return GADAdSizeFullBanner;
        case 4: return GADAdSizeLeaderboard;
        case 5:
        {
            UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
            if(orientation == UIInterfaceOrientationPortrait or orientation == 0)
            {
                return GADPortraitInlineAdaptiveBannerAdSizeWithWidth(g_controller.view.frame.size.width);
            }
            else
            {
                return GADPortraitInlineAdaptiveBannerAdSizeWithWidth(g_controller.view.frame.size.height);
            }
            break;
        }
        case 6:
        {
            CGRect frame = g_controller.view.frame;
            if (@available(iOS 11.0, *)) {
                frame = UIEdgeInsetsInsetRect(g_controller.view.frame, g_controller.view.safeAreaInsets);
            }
            CGFloat viewWidth = frame.size.width;
            return GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth);
            break;
        }
        default: {NSLog(@"AddBanner illegal banner size type"); break;}
    }
    
    return GADAdSize();
}

#pragma mark - Interstitial Methods
#pragma mark - Server Side Verification
- (void)configureServerSideVerification:(GADRequest *)request withUserId:(NSString *)userId customData:(NSString *)customData
{
    // Check if either userId or customData is a non-empty string
    if (userId.length > 0 || customData.length > 0) {
        // Create GADExtras to add custom parameters
        GADExtras *extras = [[GADExtras alloc] init];
        
        // Initialize a mutable dictionary to hold custom parameters
        NSMutableDictionary *customParameters = [NSMutableDictionary dictionary];
        
        // Conditionally add "userId" if it's not an empty string
        if (userId.length > 0) {
            customParameters[@"userId"] = userId;
        }
        
        // Conditionally add "customData" if it's not an empty string
        if (customData.length > 0) {
            customParameters[@"customData"] = customData;
        }
        
        // Assign the non-empty custom parameters to GADExtras
        extras.additionalParameters = [customParameters copy];
        
        // Register the extras with the ad request
        [request registerAdNetworkExtras:extras];
    }
}

#pragma mark - Rewarded Video Methods
#pragma mark - Rewarded Interstitial Methods
#pragma mark - App Open Methods
-(BOOL)appOpenAdIsValid:(const char *)callingMethod {
    GADAppOpenAd *ad;
    NSDate *loadTime;
    UIInterfaceOrientation orientation;

    @synchronized (self.adHandlesLock) {
        ad = self.appOpenAd;
        loadTime = self.appOpenAdLoadTime;
        orientation = self.appOpenAdOrientation;
    }

    // Check if is loaded
    if (ad == nil) {
        NSLog(@"%s :: There is no app open ad loaded.", callingMethod);
        return NO;
    }

    // Check if is expired
    NSTimeInterval dateDifference = [[NSDate date] timeIntervalSinceDate:loadTime];
    BOOL expired = dateDifference >= (3600 * self.appOpenAdExpirationTime);
    if (expired) {
        NSLog(@"%s :: The loaded app open ad expired, reloading...", callingMethod);
        return NO;
    }

    // Check if is correct orientation
    UIInterfaceOrientation currentOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    if (currentOrientation != orientation) {
        NSLog(@"%s :: The loaded app open ad has incorrect orientation, reloading...", callingMethod);
        return NO;
    }

    return YES;
}

#pragma mark - Targeting Methods

//https://developers.google.com/admob/ios/targeting#child-directed_setting
//https://developers.google.com/admob/ios/targeting#users_under_the_age_of_consent
//https://developers.google.com/admob/ios/targeting#ad_content_filtering
#pragma mark - Consent Management
// https://stackoverflow.com/questions/69307205/mandatory-consent-for-admob-user-messaging-platform
Boolean canShowAds()
{
    NSString *purposeConsent = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_PurposeConsents"];
    NSString *vendorConsent = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_VendorConsents"];
    NSString *vendorLI = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_VendorLegitimateInterests"];
    NSString *purposeLI = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_PurposeLegitimateInterests"];
    
    int googleId = 755;
    Boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
    Boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);
    
    int indexes[1] = {1};
    int indexesLI[4] = {2, 7, 9, 10};
    
    return hasConsentFor(indexes, 1, purposeConsent, hasGoogleVendorConsent) && hasConsentOrLegitimateInterestFor(indexesLI, 4, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
}

Boolean canShowPersonalizedAds()
{
    NSString *purposeConsent = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_PurposeConsents"];
    NSString *vendorConsent = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_VendorConsents"];
    NSString *vendorLI = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_VendorLegitimateInterests"];
    NSString *purposeLI = [[NSUserDefaults standardUserDefaults] stringForKey:@"IABTCF_PurposeLegitimateInterests"];
    
    int googleId = 755;
    Boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
    Boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);
    
    int indexes[3] = {1, 3, 4};
    int indexesLI[4] = {2, 7, 9, 10};
    
    return hasConsentFor(indexes, 3, purposeConsent, hasGoogleVendorConsent) && hasConsentOrLegitimateInterestFor(indexesLI, 4, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
}

Boolean hasAttribute(NSString* input, int index)
{
    if (input == nil) return NO;
    if (index <= 0 || (NSUInteger)index > input.length) return NO;
    return [input characterAtIndex:(NSUInteger)index-1] == '1';
}

Boolean hasConsentFor(int* indexes, int size, NSString* purposeConsent, Boolean hasVendorConsent)
{
    int index;
    for (int i = 0; i < size; i++)
    {
        index = indexes[i];
        if (!hasAttribute(purposeConsent, index)) {
            NSLog(@"hasConsentFor: denied for purpose #%d", index);
            return NO;
        }
    }
    return hasVendorConsent;
}

Boolean hasConsentOrLegitimateInterestFor(int* indexes, int size, NSString* purposeConsent, NSString* purposeLI, Boolean hasVendorConsent, Boolean hasVendorLI)
{
    int index;
    for (int i = 0; i < size; i++)
    {
        index = indexes[i];
        Boolean purposeAndVendorLI = hasAttribute(purposeLI, index) && hasVendorLI;
        Boolean purposeConsentAndVendorConsent = hasAttribute(purposeConsent, index) && hasVendorConsent;
        Boolean isOk = purposeAndVendorLI || purposeConsentAndVendorConsent;
        if (!isOk) {
            NSLog(@"hasConsentOrLegitimateInterestFor: denied for purpose #%d", index);
            return NO;
        }
    }
    
    return YES;
}

#pragma mark - Settings Methods
#pragma mark - Activity Lifecycle Methods

-(void) onResume
{
    if (self.triggerAppOpenAd) {
        if(![self appOpenAdIsValid:"onResume"]) {
            [self admob_app_open_ad_load:g_app_open_enable_callback];
            self.isShowingAd = NO;
            return;
        }

        if (!self.isShowingAd) {
            [self admob_app_open_ad_show:g_app_open_enable_callback];
        }
    }
}

-(void) onStop
{
    // Clean up Banner Ad
    if (self.bannerView != nil) {
        [self deleteBannerAdView];
        self.bannerView = nil;
    }
    
    // Clear Interstitial Ads
    @synchronized (self.adHandlesLock) {
        for (GADInterstitialAd *ad in self.interstitialAdHandles.allValues) {
            [self cleanAd:ad withCleaner:^(id ad){
                [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
            }];
        }
        [self.interstitialAdHandles removeAllObjects];
    }
    g_interstitial_show_callbacks.clear();

    // Clear Rewarded Ads
    @synchronized (self.adHandlesLock) {
        for (GADRewardedAd *ad in self.rewardedAdHandles.allValues) {
            [self cleanAd:ad withCleaner:^(id ad){
                [self cleanUpRewardedAd:(GADRewardedAd *)ad];
            }];
        }
        [self.rewardedAdHandles removeAllObjects];
    }
    g_rewarded_video_show_callbacks.clear();

    // Clear Rewarded Interstitial Ads
    @synchronized (self.adHandlesLock) {
        for (GADRewardedInterstitialAd *ad in self.rewardedInterstitialAdHandles.allValues) {
            [self cleanAd:ad withCleaner:^(id ad){
                [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
            }];
        }
        [self.rewardedInterstitialAdHandles removeAllObjects];
    }
    g_rewarded_interstitial_show_callbacks.clear();

    // Nullify App Open Ad
    @synchronized (self.adHandlesLock) {
        if (self.appOpenAd != nil) {
            [self cleanAd:self.appOpenAd withCleaner:^(id ad){
                [self cleanUpAppOpenAd:(GADAppOpenAd *)ad];
            }];
            self.appOpenAd = nil;
        }
    }

    // Nullify Consent Form
    @synchronized (self.adHandlesLock) {
        self.consentForm = nil;
    }
}

#pragma mark - Helper Methods

typedef void (^AdCleanerBlock)(id ad);

- (void)cleanAd:(id)ad withCleaner:(AdCleanerBlock)cleaner {
    if (ad != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            cleaner(ad);
        });
    }
}

- (void)cleanUpBannerView:(GADBannerView *)ad {
    ad.delegate = nil;  // Remove the delegate to avoid retain cycles
    ad.paidEventHandler = nil;  // Remove any paid event listener
    [ad removeFromSuperview];  // Remove the ad from the view hierarchy if necessary
    // Additional cleanup if needed
}

- (void)cleanUpInterstitialAd:(GADInterstitialAd *)ad {
    ad.fullScreenContentDelegate = nil;
    ad.paidEventHandler = nil;
    // Additional cleanup if needed
}

- (void)cleanUpRewardedAd:(GADRewardedAd *)ad {
    ad.fullScreenContentDelegate = nil;
    ad.paidEventHandler = nil;
    // Additional cleanup if needed
}

- (void)cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad {
    ad.fullScreenContentDelegate = nil;
    ad.paidEventHandler = nil;
    // Additional cleanup if needed
}

- (void)cleanUpAppOpenAd:(GADAppOpenAd *)ad {
    ad.fullScreenContentDelegate = nil;
    ad.paidEventHandler = nil;
    // Additional cleanup if needed
}


-(void)onPaidEventHandler:(GADAdValue*) value adUnitId:(NSString*)adUnitId adType:(gm_enums::AdMobAdType)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
    std::optional<std::string> adSourceName = std::nullopt;
    std::optional<std::string> adSourceId = std::nullopt;
    std::optional<std::string> adSourceInstanceName = std::nullopt;
    std::optional<std::string> adSourceInstanceId = std::nullopt;

    if (loadedAdNetworkResponseInfo != nil)
    {
        adSourceName = std::string(AdMobCString(loadedAdNetworkResponseInfo.adSourceName));
        adSourceId = std::string(AdMobCString(loadedAdNetworkResponseInfo.adSourceID));
        adSourceInstanceName = std::string(AdMobCString(loadedAdNetworkResponseInfo.adSourceInstanceName));
        adSourceInstanceId = std::string(AdMobCString(loadedAdNetworkResponseInfo.adSourceInstanceID));
    }

    gm_structs::AdMobPaidEvent event{};
    event.ad_type = adType;
    event.ad_unit_id = std::string(AdMobCString(adUnitId));
    event.value_micros = value.value.doubleValue * 1000000.0;
    event.currency_code = std::string(AdMobCString(value.currencyCode));
    event.precision = (gm_enums::AdMobPrecisionType)(int32_t)value.precision;
    event.mediation_adapter_class_name = std::string(AdMobCString(mediationAdapterClassName));
    event.ad_source_name = adSourceName;
    event.ad_source_id = adSourceId;
    event.ad_source_instance_name = adSourceInstanceName;
    event.ad_source_instance_id = adSourceInstanceId;

    if (g_paid_event_callback)
        g_paid_event_callback.call(event);
}

- (GADRequest*) buildAdRequest
{
    GADRequest *request = [GADRequest request];
    
    // Set the request agent as per Google's requirement
    request.requestAgent = [NSString stringWithFormat:@"gmext-admob-%s", extGetVersion((char*)"AdMob")];
    
    // Additional network request parameters for AdMob.
    NSMutableDictionary<NSString *, NSString *> *additionalParams = [NSMutableDictionary dictionary];
    
    // Handle Revenue Data Processing (rdp)
    if (self.isRdpEnabled)
    {
        additionalParams[@"rdp"] = @"1";
    }
    
    // If there are any additional parameters to add
    if (additionalParams.count > 0)
    {
        // Create GADExtras with the additional parameters
        GADExtras *extras = [[GADExtras alloc] init];
        extras.additionalParameters = [additionalParams copy];
        
        // Register the extras with the request
        [request registerAdNetworkExtras:extras];
    }
    
    return request;
}

const char * getDeviceId()
{
    NSUUID* adid = [[ASIdentifierManager sharedManager] advertisingIdentifier];
    const char *cStr = [adid.UUIDString UTF8String];
    unsigned char digest[16];
    CC_MD5( cStr, (CC_LONG) strlen(cStr), digest );
    
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++)
    {
        [output appendFormat:@"%02x", digest[i]];
    }
    
    return [output UTF8String];
}

#pragma mark - Validations

- (BOOL)validateNotInitializedWithCallingMethod:(const char *)callingMethod {
    if (self.isInitialized) {
        NSLog(@"%s :: Method cannot be called after initialization.", callingMethod);
    }
    return !self.isInitialized;
}

- (BOOL)validateInitializedWithCallingMethod:(const char *)callingMethod {
    if (!self.isInitialized) {
        NSLog(@"%s :: Extension was not initialized.", callingMethod);
    }
    return self.isInitialized;
}

- (BOOL)validateActiveBannerAdWithCallingMethod:(const char *)callingMethod {
    if (self.bannerView == nil) {
        NSLog(@"%s :: There is no active banner ad.", callingMethod);
        return NO;
    }
    return YES;
}

- (BOOL)validateAdId:(NSString *)adUnitId callingMethod:(const char *)callingMethod {
    if (adUnitId.length == 0) {
        NSLog(@"%s :: Extension was not initialized.", callingMethod);
        return NO;
    }
    return YES;
}

@end
