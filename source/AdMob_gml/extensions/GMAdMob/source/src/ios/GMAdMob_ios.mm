#import "GMAdMob_ios.h"

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <GoogleMobileAds/GoogleMobileAds.h>
#import <UserMessagingPlatform/UserMessagingPlatform.h>
#import <AdSupport/AdSupport.h>
#import <CommonCrypto/CommonDigest.h>
#include <cstring>
#include <deque>


extern int CreateDsMap( int _num, ... );
extern void CreateAsynEventWithDSMap(int dsmapindex, int event_index);
extern UIViewController *g_controller;
extern UIView *g_glView;
extern int g_DeviceWidth;
extern int g_DeviceHeight;

extern "C" void dsMapClear(int _dsMap );
extern "C" int dsMapCreate();
extern "C" void dsMapAddInt(int _dsMap, char* _key, int _value);
extern "C" void dsMapAddDouble(int _dsMap, char* _key, double _value);
extern "C" void dsMapAddString(int _dsMap, char* _key, char* _value);

extern "C" int dsListCreate();
extern "C" void dsListAddInt(int _dsList, int _value);
extern "C" void dsListAddString(int _dsList, char* _value);
extern "C" const char* dsListGetValueString(int _dsList, int _listIdx);
extern "C" double dsListGetValueDouble(int _dsList, int _listIdx);
extern "C" int dsListGetSize(int _dsList);

extern "C" const char* extOptGetString(char* _ext, char* _opt);
extern "C" const char* extGetVersion(char* _ext);

extern "C" void createSocialAsyncEventWithDSMap(int dsmapindex);

static gm::wire::GMFunction g_initialize_callback = nil;
static gm::wire::GMFunction g_paid_event_callback = nil;
static gm::wire::GMFunction g_banner_callback = nil;
static gm::wire::GMFunction g_interstitial_show_callback = nil;
static gm::wire::GMFunction g_rewarded_video_show_callback = nil;
static gm::wire::GMFunction g_rewarded_interstitial_show_callback = nil;
static gm::wire::GMFunction g_app_open_enable_callback = nil;
static gm::wire::GMFunction g_app_open_load_callback = nil;
static gm::wire::GMFunction g_app_open_show_callback = nil;
static gm::wire::GMFunction g_consent_request_callback = nil;
static gm::wire::GMFunction g_consent_load_callback = nil;
static gm::wire::GMFunction g_consent_show_callback = nil;
static std::deque<gm::wire::GMFunction> g_interstitial_load_callbacks;
static std::deque<gm::wire::GMFunction> g_rewarded_video_load_callbacks;
static std::deque<gm::wire::GMFunction> g_rewarded_interstitial_load_callbacks;

static const int ADMOB_INIT_EVENT_INITIALIZED = 0;
static const int ADMOB_INIT_EVENT_FAILED = 1;

static const int ADMOB_BANNER_EVENT_LOADED = 0;
static const int ADMOB_BANNER_EVENT_LOAD_FAILED = 1;
static const int ADMOB_BANNER_EVENT_OPENED = 2;
static const int ADMOB_BANNER_EVENT_CLICKED = 3;
static const int ADMOB_BANNER_EVENT_CLOSED = 4;

static const int ADMOB_FULLSCREEN_EVENT_LOADED = 0;
static const int ADMOB_FULLSCREEN_EVENT_LOAD_FAILED = 1;
static const int ADMOB_FULLSCREEN_EVENT_FULLY_SHOWN = 2;
static const int ADMOB_FULLSCREEN_EVENT_SHOW_FAILED = 3;
static const int ADMOB_FULLSCREEN_EVENT_DISMISSED = 4;
static const int ADMOB_FULLSCREEN_EVENT_REWARD = 5;

static const int ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATED = 0;
static const int ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATE_FAILED = 1;
static const int ADMOB_CONSENT_EVENT_LOADED = 2;
static const int ADMOB_CONSENT_EVENT_LOAD_FAILED = 3;
static const int ADMOB_CONSENT_EVENT_SHOWN = 4;
static const int ADMOB_CONSENT_EVENT_SHOW_FAILED = 5;

static const int ADMOB_PAID_EVENT_PAID = 0;

static NSString *AdMobStringFromStringView(std::string_view value)
{
    NSString *string =
        [[NSString alloc]
            initWithBytes:value.data()
            length:value.size()
            encoding:NSUTF8StringEncoding];

    return string != nil ? string : @"";
}

static NSString *AdMobSnakeCase(NSString *value)
{
    if (value.length == 0)
        return @"";

    NSString *normalized =
        [[value stringByReplacingOccurrencesOfString:@"AdMob_"
                                          withString:@"admob_"]
            stringByReplacingOccurrencesOfString:@"AdMob"
                                      withString:@"admob"];

    NSMutableString *result = [NSMutableString string];
    NSCharacterSet *uppercase = [NSCharacterSet uppercaseLetterCharacterSet];

    for (NSUInteger i = 0; i < normalized.length; ++i)
    {
        unichar c = [normalized characterAtIndex:i];
        if ([uppercase characterIsMember:c])
        {
            if (i > 0)
                [result appendString:@"_"];

            NSString *letter =
                [[NSString stringWithCharacters:&c length:1] lowercaseString];
            [result appendString:letter];
        }
        else
        {
            [result appendFormat:@"%C", c];
        }
    }

    while ([result containsString:@"__"])
        [result replaceOccurrencesOfString:@"__"
                                withString:@"_"
                                   options:0
                                     range:NSMakeRange(0, result.length)];

    return [result lowercaseString];
}

static int AdMobCallbackEventTypeForName(NSString *eventType);

static const char *AdMobCString(NSString *value)
{
    return value != nil ? value.UTF8String : "";
}

static gm::wire::StructStream AdMobPayload(
    NSString *eventType,
    double code,
    const char *errorMessage)
{
    BOOL failed =
        ((int)code != 0) ||
        [eventType hasSuffix:@"failed"] ||
        [eventType hasSuffix:@"load_failed"] ||
        [eventType hasSuffix:@"show_failed"] ||
        [eventType hasSuffix:@"request_info_update_failed"];

    const char *safeError =
        errorMessage != nullptr ? errorMessage : "";

    if (strlen(safeError) > 0)
        failed = YES;

    gm::wire::StructStream payload;
    payload.add("success", failed ? false : true);
    payload.add(
        "event_type",
        (int32_t)AdMobCallbackEventTypeForName(eventType)
    );
    payload.add("code", code);
    payload.add("error_code", code);
    payload.add("error_message", safeError);

    return payload;
}

static gm::wire::StructStream AdMobPayload(NSString *eventType)
{
    return AdMobPayload(eventType, 0.0, "");
}

static const char *AdMobErrorMessageForCode(double code)
{
    switch ((int)code)
    {
        case 0: return "";
        case -1: return "AdMob SDK is not initialized.";
        case -2: return "The AdMob ad unit ID is invalid or empty.";
        case -3: return "The loaded ad instance limit was reached.";
        case -4: return "There are no ads loaded.";
        case -5: return "There is no active banner ad.";
        case -6: return "This call is not valid in the current AdMob state.";
        case -7: return "RunnerActivity.ViewHandler is null.";
        default: return "Unknown AdMob error.";
    }
}

static int AdMobCallbackEventTypeForName(NSString *eventType)
{
    if ([eventType isEqualToString:@"admob_on_initialized"])
        return ADMOB_INIT_EVENT_INITIALIZED;

    if ([eventType isEqualToString:@"admob_banner_on_loaded"])
        return ADMOB_BANNER_EVENT_LOADED;

    if ([eventType isEqualToString:@"admob_banner_on_load_failed"])
        return ADMOB_BANNER_EVENT_LOAD_FAILED;

    if ([eventType isEqualToString:@"admob_banner_on_opened"])
        return ADMOB_BANNER_EVENT_OPENED;

    if ([eventType isEqualToString:@"admob_banner_on_clicked"])
        return ADMOB_BANNER_EVENT_CLICKED;

    if ([eventType isEqualToString:@"admob_banner_on_closed"])
        return ADMOB_BANNER_EVENT_CLOSED;

    if ([eventType isEqualToString:@"admob_interstitial_on_loaded"] ||
        [eventType isEqualToString:@"admob_rewarded_video_on_loaded"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_loaded"] ||
        [eventType isEqualToString:@"admob_app_open_ad_on_loaded"])
        return ADMOB_FULLSCREEN_EVENT_LOADED;

    if ([eventType isEqualToString:@"admob_interstitial_on_load_failed"] ||
        [eventType isEqualToString:@"admob_rewarded_video_on_load_failed"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_load_failed"] ||
        [eventType isEqualToString:@"admob_app_open_ad_on_load_failed"])
        return ADMOB_FULLSCREEN_EVENT_LOAD_FAILED;

    if ([eventType isEqualToString:@"admob_interstitial_on_fully_shown"] ||
        [eventType isEqualToString:@"admob_rewarded_video_on_fully_shown"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_fully_shown"] ||
        [eventType isEqualToString:@"admob_app_open_ad_on_fully_shown"])
        return ADMOB_FULLSCREEN_EVENT_FULLY_SHOWN;

    if ([eventType isEqualToString:@"admob_interstitial_on_show_failed"] ||
        [eventType isEqualToString:@"admob_rewarded_video_on_show_failed"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_show_failed"] ||
        [eventType isEqualToString:@"admob_app_open_ad_on_show_failed"])
        return ADMOB_FULLSCREEN_EVENT_SHOW_FAILED;

    if ([eventType isEqualToString:@"admob_interstitial_on_dismissed"] ||
        [eventType isEqualToString:@"admob_rewarded_video_on_dismissed"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_dismissed"] ||
        [eventType isEqualToString:@"admob_app_open_ad_on_dismissed"])
        return ADMOB_FULLSCREEN_EVENT_DISMISSED;

    if ([eventType isEqualToString:@"admob_rewarded_video_on_reward"] ||
        [eventType isEqualToString:@"admob_rewarded_interstitial_on_reward"])
        return ADMOB_FULLSCREEN_EVENT_REWARD;

    if ([eventType isEqualToString:@"admob_consent_on_request_info_updated"])
        return ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATED;

    if ([eventType isEqualToString:@"admob_consent_on_request_info_update_failed"])
        return ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATE_FAILED;

    if ([eventType isEqualToString:@"admob_consent_on_loaded"])
        return ADMOB_CONSENT_EVENT_LOADED;

    if ([eventType isEqualToString:@"admob_consent_on_load_failed"])
        return ADMOB_CONSENT_EVENT_LOAD_FAILED;

    if ([eventType isEqualToString:@"admob_consent_on_shown"])
        return ADMOB_CONSENT_EVENT_SHOWN;

    if ([eventType isEqualToString:@"admob_consent_on_show_failed"])
        return ADMOB_CONSENT_EVENT_SHOW_FAILED;

    if ([eventType isEqualToString:@"admob_on_paid_event"])
        return ADMOB_PAID_EVENT_PAID;

    return -1;
}

static void AdMobInvokeCallback(
    gm::wire::GMFunction callback,
    gm::wire::StructStream payload)
{
    if (callback)
        callback.call(payload);
}

static void AdMobCallbackResult(
    gm::wire::GMFunction callback,
    int eventType,
    double code)
{
    bool success = ((int)code == 0);

    gm::wire::StructStream payload;
    payload.add("success", success);
    payload.add("event_type", (int32_t)eventType);
    payload.add("code", code);
    payload.add("error_code", code);
    payload.add(
        "error_message",
        success ? "" : AdMobErrorMessageForCode(code)
    );

    AdMobInvokeCallback(callback, payload);
}

@interface ThreadSafeQueue : NSObject
@property (nonatomic, strong) NSMutableArray *array;
@property (nonatomic, strong) NSObject *lock; // A dedicated lock object for synchronization
@end

@implementation ThreadSafeQueue

- (instancetype)init {
    self = [super init];
    if (self) {
        _array = [[NSMutableArray alloc] init];
        _lock = [[NSObject alloc] init];
    }
    return self;
}

- (void)enqueue:(id)object {
    @synchronized(_lock) {
        [_array addObject:object];
    }
}

- (id)dequeue {
    id dequeuedObject = nil;
    @synchronized(_lock) {
        if (_array.count > 0) {
            dequeuedObject = _array.firstObject;
            [_array removeObjectAtIndex:0];
        }
    }
    return dequeuedObject;
}

- (NSUInteger)size {
    @synchronized(_lock) {
        return _array.count;
    }
}

@end


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
@property (nonatomic, strong) ThreadSafeQueue *interstitialAdQueue;
@property (nonatomic, assign) int interstitialAdQueueCapacity;
@property (nonatomic, strong) GADInterstitialAd *interstitialAd;
@property (nonatomic, strong) ThreadSafeQueue *rewardedAdQueue;
@property (nonatomic, assign) int rewardedAdQueueCapacity;
@property (nonatomic, strong) GADRewardedAd *rewardedAd;
@property (nonatomic, strong) ThreadSafeQueue *rewardedInterstitialAdQueue;
@property (nonatomic, assign) int rewardedAdInterstitialQueueCapacity;
@property (nonatomic, strong) GADRewardedInterstitialAd *rewardedInterstitialAd;
@property (nonatomic, strong) GADAppOpenAd *appOpenAd;
@property (nonatomic, strong) NSDate *appOpenAdLoadTime;
@property (nonatomic, assign) UIInterfaceOrientation appOpenAdOrientation;
@property (nonatomic, assign) int appOpenAdExpirationTime;
@property (nonatomic, strong) NSString *serverSideVerificationUserId;
@property (nonatomic, strong) NSString *serverSideVerificationCustomData;
@property (nonatomic, strong) UMPConsentForm *consentForm;
@end

@implementation GMAdMob

const int ADMOB_OK = 0;
const int ADMOB_ERROR_NOT_INITIALIZED = -1;
const int ADMOB_ERROR_INVALID_AD_ID = -2;
const int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
const int ADMOB_ERROR_NO_ADS_LOADED = -4;
const int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
const int ADMOB_ERROR_ILLEGAL_CALL = -6;

const int ADMOB_BANNER_ALIGNMENT_LEFT = 0;
const int ADMOB_BANNER_ALIGNMENT_CENTER = 1;
const int ADMOB_BANNER_ALIGNMENT_RIGHT = 2;

-(id)init {
    if ( self = [super init] ) {
        
        self.isInitialized = NO;
        self.isTestDevice = NO;
        
        self.bannerAdUnitId = @"";
        self.interstitialAdUnitId = @"";
        self.rewardedUnitId = @"";
        self.rewardedInterstitialAdUnitId = @"";
        self.appOpenAdUnitId = @"";
        
        self.interstitialAdQueueCapacity = 1;
        self.interstitialAdQueue = [[ThreadSafeQueue alloc] init];
        
        self.rewardedAdQueueCapacity = 1;
        self.rewardedAdQueue = [[ThreadSafeQueue alloc] init];
        
        self.rewardedAdInterstitialQueueCapacity = 1;
        self.rewardedInterstitialAdQueue = [[ThreadSafeQueue alloc] init];
        
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

- (double)admob_initialize:
    (gm::wire::GMFunction)callback
{
    g_initialize_callback = callback;

    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
    {
        g_initialize_callback = nil;
        AdMobCallbackResult(
            callback,
            ADMOB_INIT_EVENT_FAILED,
            ADMOB_ERROR_ILLEGAL_CALL
        );

        return ADMOB_ERROR_ILLEGAL_CALL;
    }

    if (self.isTestDevice)
    {
#if TARGET_OS_SIMULATOR
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers =
            @[GADSimulatorID];
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
            gm::wire::StructStream eventData =
                AdMobPayload(@"AdMob_OnInitialized");
            [self sendAsyncEvent:"AdMob_OnInitialized"
                       eventData:eventData];
        }];

    return ADMOB_OK;
}

- (double)admob_set_test_device_id
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_ILLEGAL_CALL;

    self.isTestDevice = YES;
    return ADMOB_OK;
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

- (double)admob_banner_create:
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

    if (code != ADMOB_OK)
    {
        g_banner_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_BANNER_EVENT_LOAD_FAILED,
            code
        );
    }

    return code;
}

- (double)admob_banner_create_ext:
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

    if (code != ADMOB_OK)
    {
        g_banner_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_BANNER_EVENT_LOAD_FAILED,
            code
        );
    }

    return code;
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

- (double)admob_banner_move:(bool)bottom
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NOT_INITIALIZED;

    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

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

    return ADMOB_OK;
}

- (double)admob_banner_show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NOT_INITIALIZED;

    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

    dispatch_async(dispatch_get_main_queue(), ^{
        self.bannerView.hidden = NO;
    });

    return ADMOB_OK;
}

- (double)admob_banner_hide
{
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

    dispatch_async(dispatch_get_main_queue(), ^{
        self.bannerView.hidden = YES;
    });

    return ADMOB_OK;
}

- (double)admob_banner_remove
{
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

    [self deleteBannerAdView];
    g_banner_callback = nil;

    return ADMOB_OK;
}

- (void)admob_interstitial_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.interstitialAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (void)admob_interstitial_free_loaded_instances:
    (double)count
{
    [self
        freeLoadedInstances:self.interstitialAdQueue
        count:count
        withCleaner:^(id ad)
        {
            [self cleanUpInterstitialAd:ad];
        }];
}

- (void)admob_interstitial_max_instances:
    (double)value
{
    self.interstitialAdQueueCapacity = (int)value;

    [self
        trimLoadedAdsQueue:self.interstitialAdQueue
        maxSize:self.interstitialAdQueueCapacity
        withCleaner:^(id ad)
        {
            [self cleanUpInterstitialAd:ad];
        }];
}

- (double)admob_interstitial_load:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self validateAdId:self.interstitialAdUnitId
              callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_INVALID_AD_ID
        );

        return ADMOB_ERROR_INVALID_AD_ID;
    }

    if (![self validateLoadedAdsLimit:self.interstitialAdQueue
                              maxSize:self.interstitialAdQueueCapacity
                        callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_AD_LIMIT_REACHED
        );

        return ADMOB_ERROR_AD_LIMIT_REACHED;
    }

    g_interstitial_load_callbacks.push_back(callback);

    const NSString *adUnitId =
        self.interstitialAdUnitId;

    GADRequest *request =
        [self buildAdRequest];

    [GADInterstitialAd
        loadWithAdUnitID:self.interstitialAdUnitId
        request:request
        completionHandler:
            ^(GADInterstitialAd *interstitialAd,
              NSError *error)
            {
                if (error)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Interstitial_OnLoadFailed", error.code, AdMobCString([error.localizedDescription copy]));
                    eventData.add("unit_id", AdMobCString([adUnitId copy]));
                    [self sendAsyncEvent:"AdMob_Interstitial_OnLoadFailed" eventData:eventData];

                    return;
                }

                if (![self
                    validateLoadedAdsLimit:self.interstitialAdQueue
                    maxSize:self.interstitialAdQueueCapacity
                    callingMethod:__FUNCTION__])
                {
                    return;
                }

                [self.interstitialAdQueue enqueue:interstitialAd];

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
                                adType:@"Interstitial"
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_Interstitial_OnLoaded");
                eventData.add("unit_id", AdMobCString([adUnitId copy]));
                [self sendAsyncEvent:"AdMob_Interstitial_OnLoaded" eventData:eventData];
            }];

    return ADMOB_OK;
}

- (double)admob_interstitial_show:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    GADInterstitialAd *interstitialAd =
        [self.interstitialAdQueue dequeue];

    if (interstitialAd == nil)
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NO_ADS_LOADED
        );

        return ADMOB_ERROR_NO_ADS_LOADED;
    }

    g_interstitial_show_callback = callback;
    interstitialAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [interstitialAd presentFromRootViewController:g_controller];
    });

    self.interstitialAd = interstitialAd;
    self.isShowingAd = YES;

    return ADMOB_OK;
}

- (bool)admob_interstitial_is_loaded
{
    return [self.interstitialAdQueue size] > 0;
}

- (double)admob_interstitial_instances_count
{
    return [self.interstitialAdQueue size];
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

- (void)admob_rewarded_video_free_loaded_instances:
    (double)count
{
    [self
        freeLoadedInstances:self.rewardedAdQueue
        count:count
        withCleaner:^(id ad)
        {
            [self cleanUpRewardedAd:ad];
        }];
}

- (void)admob_rewarded_video_max_instances:
    (double)value
{
    self.rewardedAdQueueCapacity = (int)value;

    [self
        trimLoadedAdsQueue:self.rewardedAdQueue
        maxSize:self.rewardedAdQueueCapacity
        withCleaner:^(id ad)
        {
            [self cleanUpRewardedAd:ad];
        }];
}

- (double)admob_rewarded_video_load:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self validateAdId:self.rewardedUnitId
              callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_INVALID_AD_ID
        );

        return ADMOB_ERROR_INVALID_AD_ID;
    }

    if (![self validateLoadedAdsLimit:self.rewardedAdQueue
                              maxSize:self.rewardedAdQueueCapacity
                        callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_AD_LIMIT_REACHED
        );

        return ADMOB_ERROR_AD_LIMIT_REACHED;
    }

    g_rewarded_video_load_callbacks.push_back(callback);

    const NSString *adUnitId =
        self.rewardedUnitId;

    GADRequest *request =
        [self buildAdRequest];

    [self
        configureServerSideVerification:request
        withUserId:self.serverSideVerificationUserId
        customData:self.serverSideVerificationCustomData];

    [GADRewardedAd
        loadWithAdUnitID:self.rewardedUnitId
        request:request
        completionHandler:
            ^(GADRewardedAd *rewardedAd,
              NSError *error)
            {
                if (error)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_RewardedVideo_OnLoadFailed", error.code, AdMobCString([error.localizedDescription copy]));
                    eventData.add("unit_id", AdMobCString([adUnitId copy]));
                    [self sendAsyncEvent:"AdMob_RewardedVideo_OnLoadFailed" eventData:eventData];

                    return;
                }

                if (![self
                    validateLoadedAdsLimit:self.rewardedAdQueue
                    maxSize:self.rewardedAdQueueCapacity
                    callingMethod:__FUNCTION__])
                {
                    return;
                }

                [self.rewardedAdQueue enqueue:rewardedAd];

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
                                adType:@"RewardedVideo"
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_RewardedVideo_OnLoaded");
                eventData.add("unit_id", AdMobCString([adUnitId copy]));
                [self sendAsyncEvent:"AdMob_RewardedVideo_OnLoaded" eventData:eventData];
            }];

    return ADMOB_OK;
}

- (double)admob_rewarded_video_show:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    GADRewardedAd *rewardedAd =
        [self.rewardedAdQueue dequeue];

    if (rewardedAd == nil)
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NO_ADS_LOADED
        );

        return ADMOB_ERROR_NO_ADS_LOADED;
    }

    g_rewarded_video_show_callback = callback;
    rewardedAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [rewardedAd
            presentFromRootViewController:g_controller
            userDidEarnRewardHandler:^
            {
                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_RewardedVideo_OnReward");
                eventData.add("unit_id", AdMobCString(rewardedAd.adUnitID));
                eventData.add("reward_amount", rewardedAd.adReward.amount.doubleValue);
                eventData.add("reward_type", AdMobCString(rewardedAd.adReward.type));
                [self sendAsyncEvent:"AdMob_RewardedVideo_OnReward" eventData:eventData];
            }];
    });

    self.rewardedAd = rewardedAd;
    self.isShowingAd = YES;

    return ADMOB_OK;
}

- (bool)admob_rewarded_video_is_loaded
{
    return [self.rewardedAdQueue size] > 0;
}

- (double)admob_rewarded_video_instances_count
{
    return [self.rewardedAdQueue size];
}

- (void)admob_rewarded_interstitial_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.rewardedInterstitialAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (void)admob_rewarded_interstitial_free_loaded_instances:
    (double)count
{
    [self
        freeLoadedInstances:self.rewardedInterstitialAdQueue
        count:count
        withCleaner:^(id ad)
        {
            [self cleanUpRewardedInterstitialAd:ad];
        }];
}

- (void)admob_rewarded_interstitial_max_instances:
    (double)value
{
    self.rewardedAdInterstitialQueueCapacity =
        (int)value;

    [self
        trimLoadedAdsQueue:self.rewardedInterstitialAdQueue
        maxSize:self.rewardedAdInterstitialQueueCapacity
        withCleaner:^(id ad)
        {
            [self cleanUpRewardedInterstitialAd:ad];
        }];
}

- (double)admob_rewarded_interstitial_load:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self validateAdId:self.rewardedInterstitialAdUnitId
              callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_INVALID_AD_ID
        );

        return ADMOB_ERROR_INVALID_AD_ID;
    }

    if (![self validateLoadedAdsLimit:self.rewardedInterstitialAdQueue
                              maxSize:self.rewardedAdInterstitialQueueCapacity
                        callingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_AD_LIMIT_REACHED
        );

        return ADMOB_ERROR_AD_LIMIT_REACHED;
    }

    g_rewarded_interstitial_load_callbacks.push_back(callback);

    const NSString *adUnitId =
        self.rewardedInterstitialAdUnitId;

    GADRequest *request =
        [self buildAdRequest];

    [self
        configureServerSideVerification:request
        withUserId:self.serverSideVerificationUserId
        customData:self.serverSideVerificationCustomData];

    [GADRewardedInterstitialAd
        loadWithAdUnitID:self.rewardedInterstitialAdUnitId
        request:request
        completionHandler:
            ^(GADRewardedInterstitialAd *rewardedInterstitialAd,
              NSError *error)
            {
                if (error)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_RewardedInterstitial_OnLoadFailed", error.code, AdMobCString([error.localizedDescription copy]));
                    eventData.add("unit_id", AdMobCString([adUnitId copy]));
                    [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnLoadFailed" eventData:eventData];

                    return;
                }

                if (![self
                    validateLoadedAdsLimit:self.rewardedInterstitialAdQueue
                    maxSize:self.rewardedAdInterstitialQueueCapacity
                    callingMethod:__FUNCTION__])
                {
                    return;
                }

                [self.rewardedInterstitialAdQueue
                    enqueue:rewardedInterstitialAd];

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
                                adType:@"RewardedInterstitial"
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_RewardedInterstitial_OnLoaded");
                eventData.add("unit_id", AdMobCString([adUnitId copy]));
                [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnLoaded" eventData:eventData];
            }];

    return ADMOB_OK;
}

- (double)admob_rewarded_interstitial_show:
    (gm::wire::GMFunction)callback
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    GADRewardedInterstitialAd *rewardedInterstitialAd =
        [self.rewardedInterstitialAdQueue dequeue];

    if (rewardedInterstitialAd == nil)
    {
        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NO_ADS_LOADED
        );

        return ADMOB_ERROR_NO_ADS_LOADED;
    }

    g_rewarded_interstitial_show_callback = callback;
    rewardedInterstitialAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [rewardedInterstitialAd
            presentFromRootViewController:g_controller
            userDidEarnRewardHandler:^
            {
                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_RewardedInterstitial_OnReward");
                eventData.add("unit_id", AdMobCString(rewardedInterstitialAd.adUnitID));
                eventData.add("reward_amount", rewardedInterstitialAd.adReward.amount.doubleValue);
                eventData.add("reward_type", AdMobCString(rewardedInterstitialAd.adReward.type));
                [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnReward" eventData:eventData];
            }];
    });

    self.rewardedInterstitialAd =
        rewardedInterstitialAd;

    self.isShowingAd = YES;

    return ADMOB_OK;
}

- (bool)admob_rewarded_interstitial_is_loaded
{
    return [self.rewardedInterstitialAdQueue size] > 0;
}

- (double)admob_rewarded_interstitial_instances_count
{
    return [self.rewardedInterstitialAdQueue size];
}

- (void)admob_app_open_ad_set_ad_unit:
    (std::string_view)ad_unit_id
{
    self.appOpenAdUnitId =
        AdMobStringFromStringView(ad_unit_id);
}

- (double)admob_app_open_ad_enable:
            (double)orientation
                         callback:
            (gm::wire::GMFunction)callback
{
    g_app_open_enable_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        g_app_open_enable_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self validateAdId:self.appOpenAdUnitId
              callingMethod:__FUNCTION__])
    {
        g_app_open_enable_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_INVALID_AD_ID
        );

        return ADMOB_ERROR_INVALID_AD_ID;
    }

    self.triggerAppOpenAd = YES;

    if (![self appOpenAdIsValid:__FUNCTION__])
        return [self admob_app_open_ad_load:callback];

    return ADMOB_OK;
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

- (double)admob_app_open_ad_load:
    (gm::wire::GMFunction)callback
{
    g_app_open_load_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        g_app_open_load_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self validateAdId:self.appOpenAdUnitId
              callingMethod:__FUNCTION__])
    {
        g_app_open_load_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_LOAD_FAILED,
            ADMOB_ERROR_INVALID_AD_ID
        );

        return ADMOB_ERROR_INVALID_AD_ID;
    }

    if ([self appOpenAdIsValid:__FUNCTION__])
        return ADMOB_OK;

    NSString *adUnitId =
        self.appOpenAdUnitId;

    self.appOpenAd = nil;

    GADRequest *request =
        [self buildAdRequest];

    self.appOpenAdOrientation =
        [[UIApplication sharedApplication] statusBarOrientation];

    [GADAppOpenAd
        loadWithAdUnitID:self.appOpenAdUnitId
        request:request
        completionHandler:
            ^(GADAppOpenAd *_Nullable appOpenAd,
              NSError *_Nullable error)
            {
                if (error)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_AppOpenAd_OnLoadFailed", error.code, AdMobCString([error.localizedDescription copy]));
                    eventData.add("unit_id", AdMobCString([adUnitId copy]));
                    [self sendAsyncEvent:"AdMob_AppOpenAd_OnLoadFailed" eventData:eventData];

                    return;
                }

                self.appOpenAd = appOpenAd;
                self.appOpenAdLoadTime = [NSDate date];

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
                                adType:@"AppOpen"
                                loadedAdNetworkResponseInfo:responseInfo
                                mediationAdapterClassName:adapterClassName];
                        };
                }

                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_AppOpenAd_OnLoaded");
                eventData.add("unit_id", AdMobCString([adUnitId copy]));
                [self sendAsyncEvent:"AdMob_AppOpenAd_OnLoaded" eventData:eventData];
            }];

    return ADMOB_OK;
}

- (double)admob_app_open_ad_show:
    (gm::wire::GMFunction)callback
{
    g_app_open_show_callback = callback;

    if (![self validateInitializedWithCallingMethod:__FUNCTION__])
    {
        g_app_open_show_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NOT_INITIALIZED
        );

        return ADMOB_ERROR_NOT_INITIALIZED;
    }

    if (![self appOpenAdIsValid:__FUNCTION__])
    {
        g_app_open_show_callback = nil;

        AdMobCallbackResult(
            callback,
            ADMOB_FULLSCREEN_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NO_ADS_LOADED
        );

        return ADMOB_ERROR_NO_ADS_LOADED;
    }

    self.appOpenAd.fullScreenContentDelegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.appOpenAd presentFromRootViewController:g_controller];
    });

    self.isShowingAd = YES;

    return ADMOB_OK;
}

- (double)admob_targeting_coppa:(bool)coppa
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_ILLEGAL_CALL;

    self.targetCOPPA = coppa;
    return ADMOB_OK;
}

- (double)admob_targeting_under_age:(bool)under_age
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_ILLEGAL_CALL;

    self.targetUnderAge = under_age;
    return ADMOB_OK;
}

- (double)admob_targeting_max_ad_content_rating:
    (gm_enums::AdMobMaxAdContentRating)content_rating
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__])
        return ADMOB_ERROR_ILLEGAL_CALL;

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

    return ADMOB_OK;
}

- (double)admob_consent_request_info_update:
            (gm_enums::AdMobConsentDebugGeography)debug_geography
                                      callback:
            (gm::wire::GMFunction)callback
{
    g_consent_request_callback = callback;

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
                if (error)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Consent_OnRequestInfoUpdateFailed", error.code, AdMobCString([error.localizedDescription copy]));
                    [self sendAsyncEvent:"AdMob_Consent_OnRequestInfoUpdateFailed" eventData:eventData];
                }
                else
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Consent_OnShown");
                    [self sendAsyncEvent:"AdMob_Consent_OnShown"
                               eventData:eventData];
                }
            }];

    return ADMOB_OK;
}

- (double)admob_consent_get_status
{
    return UMPConsentInformation.sharedInstance.consentStatus;
}

- (double)admob_consent_get_type
{
    if (UMPConsentInformation.sharedInstance.consentStatus ==
        UMPConsentStatusObtained)
    {
        if (!canShowAds())
            return 3.0;

        return canShowPersonalizedAds() ? 2.0 : 1.0;
    }

    return 0.0;
}

- (bool)admob_consent_is_form_available
{
    return UMPConsentInformation.sharedInstance.formStatus ==
        UMPFormStatusAvailable;
}

- (double)admob_consent_load:
    (gm::wire::GMFunction)callback
{
    g_consent_load_callback = callback;

    [UMPConsentForm
        loadWithCompletionHandler:
            ^(UMPConsentForm *form,
              NSError *loadError)
            {
                if (loadError)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Consent_OnLoadFailed", loadError.code, AdMobCString([loadError.localizedDescription copy]));
                    [self sendAsyncEvent:"AdMob_Consent_OnLoadFailed" eventData:eventData];

                    return;
                }

                self.consentForm = form;

                gm::wire::StructStream eventData =
                    AdMobPayload(@"AdMob_Consent_OnLoaded");
                [self sendAsyncEvent:"AdMob_Consent_OnLoaded"
                           eventData:eventData];
            }];

    return ADMOB_OK;
}

- (double)admob_consent_show:
    (gm::wire::GMFunction)callback
{
    g_consent_show_callback = callback;

    if (self.consentForm == nil)
    {
        AdMobCallbackResult(
            callback,
            ADMOB_CONSENT_EVENT_SHOW_FAILED,
            ADMOB_ERROR_NO_ADS_LOADED
        );

        return ADMOB_ERROR_NO_ADS_LOADED;
    }

    [self.consentForm
        presentFromViewController:g_controller
        completionHandler:
            ^(NSError *dismissError)
            {
                if (dismissError)
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Consent_OnShowFailed", dismissError.code, AdMobCString([dismissError.localizedDescription copy]));
                    [self sendAsyncEvent:"AdMob_Consent_OnShowFailed" eventData:eventData];
                }
                else
                {
                    gm::wire::StructStream eventData =
                        AdMobPayload(@"AdMob_Consent_OnShown");
                    [self sendAsyncEvent:"AdMob_Consent_OnShown"
                               eventData:eventData];
                }

                self.consentForm = nil;
            }];

    return ADMOB_OK;
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
        gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_Banner_OnLoadFailed", error.code, AdMobCString([error.localizedDescription copy]));
    [self sendAsyncEvent:"AdMob_Banner_OnLoadFailed" eventData:eventData];
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView
{
        gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_Banner_OnLoaded");
    eventData.add("unit_id", AdMobCString(bannerView.adUnitID));
    [self sendAsyncEvent:"AdMob_Banner_OnLoaded" eventData:eventData];
}

- (void)bannerViewWillPresentScreen:(GADBannerView *)bannerView {
    gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_Banner_OnOpened");
    eventData.add("unit_id", AdMobCString(bannerView.adUnitID));
    [self sendAsyncEvent:"AdMob_Banner_OnOpened" eventData:eventData];
}

- (void)bannerViewWillDismissScreen:(GADBannerView *)bannerView {
	//This event doesn't exists on Andorid, ignore...
}

- (void)bannerViewDidDismissScreen:(GADBannerView *)bannerView {
    gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_Banner_OnClosed");
    eventData.add("unit_id", AdMobCString(bannerView.adUnitID));
    [self sendAsyncEvent:"AdMob_Banner_OnClosed" eventData:eventData];
}

- (void)bannerViewDidRecordClick:(GADBannerView *)bannerView {
    gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_Banner_OnClicked");
    eventData.add("unit_id", AdMobCString(bannerView.adUnitID));
    [self sendAsyncEvent:"AdMob_Banner_OnClicked" eventData:eventData];
}

-(void)ad:(nonnull id<GADFullScreenPresentingAd>)presentingAd didFailToPresentFullScreenContentWithError:(nonnull NSError *)error
{
    self.isShowingAd = NO;
    
    NSString *eventType = nil;
    NSString *adUnitID = nil;
    
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]]) {
        eventType = @"AdMob_Interstitial_OnShowFailed";
        adUnitID = [(GADInterstitialAd *)presentingAd adUnitID];
        self.interstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]]) {
        eventType = @"AdMob_RewardedVideo_OnShowFailed";
        adUnitID = [(GADRewardedAd *)presentingAd adUnitID];
        self.rewardedAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]]) {
        eventType = @"AdMob_RewardedInterstitial_OnShowFailed";
        adUnitID = [(GADRewardedInterstitialAd *)presentingAd adUnitID];
        self.rewardedInterstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]]) {
        eventType = @"AdMob_AppOpenAd_OnShowFailed";
        adUnitID = [(GADAppOpenAd *)presentingAd adUnitID];
        self.appOpenAd = nil;
        
        // If AppOpenAd is being automatically managed
        if (self.triggerAppOpenAd) {
            // Reload the App Open Ad after failure
            [self admob_app_open_ad_load:g_app_open_enable_callback];
        }
    }
    
    if (eventType && adUnitID) {
                gm::wire::StructStream eventData =
            AdMobPayload(eventType, error.code, AdMobCString([error.localizedDescription copy]));
        eventData.add("unit_id", AdMobCString(adUnitID));
        [self sendAsyncEvent:[eventType UTF8String] eventData:eventData];
    }
}

-(void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    NSString *eventType = nil;
    NSString *adUnitID = nil;

    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        eventType = @"AdMob_Interstitial_OnFullyShown";
        adUnitID = [(GADInterstitialAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        eventType = @"AdMob_RewardedVideo_OnFullyShown";
        adUnitID = [(GADRewardedAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        eventType = @"AdMob_RewardedInterstitial_OnFullyShown";
        adUnitID = [(GADRewardedInterstitialAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        eventType = @"AdMob_AppOpenAd_OnFullyShown";
        adUnitID = [(GADAppOpenAd *)presentingAd adUnitID];

        if (self.triggerAppOpenAd)
            [self admob_app_open_ad_load:g_app_open_enable_callback];
    }

    if (eventType != nil && adUnitID != nil)
    {
        gm::wire::StructStream eventData =
            AdMobPayload(eventType);

        eventData.add("unit_id", AdMobCString(adUnitID));

        [self sendAsyncEvent:[eventType UTF8String]
                   eventData:eventData];
    }
}

-(void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    NSString *eventType = nil;
    NSString *adUnitID = nil;

    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        eventType = @"AdMob_Interstitial_OnDismissed";
        adUnitID = [(GADInterstitialAd *)presentingAd adUnitID];

        [self cleanAd:(GADInterstitialAd *)presentingAd
          withCleaner:^(id ad)
        {
            [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
        }];

        self.interstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        eventType = @"AdMob_RewardedVideo_OnDismissed";
        adUnitID = [(GADRewardedAd *)presentingAd adUnitID];

        [self cleanAd:(GADRewardedAd *)presentingAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedAd:(GADRewardedAd *)ad];
        }];

        self.rewardedAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        eventType = @"AdMob_RewardedInterstitial_OnDismissed";
        adUnitID = [(GADRewardedInterstitialAd *)presentingAd adUnitID];

        [self cleanAd:(GADRewardedInterstitialAd *)presentingAd
          withCleaner:^(id ad)
        {
            [self cleanUpRewardedInterstitialAd:
                (GADRewardedInterstitialAd *)ad];
        }];

        self.rewardedInterstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        eventType = @"AdMob_AppOpenAd_OnDismissed";
        adUnitID = self.appOpenAdUnitId;

        [self cleanAd:(GADAppOpenAd *)presentingAd
          withCleaner:^(id ad)
        {
            [self cleanUpAppOpenAd:(GADAppOpenAd *)ad];
        }];

        self.appOpenAd = nil;

        if (self.triggerAppOpenAd)
            [self admob_app_open_ad_load:g_app_open_enable_callback];
    }

    if (eventType != nil && adUnitID != nil)
    {
        gm::wire::StructStream eventData =
            AdMobPayload(eventType);

        eventData.add("unit_id", AdMobCString(adUnitID));

        [self sendAsyncEvent:[eventType UTF8String]
                   eventData:eventData];
    }
}

#pragma mark - Banner Methods
- (double)createBannerAdViewWithSize:(double)size bottom:(double)bottom alignment:(int)alignment callingMethod:(const char *)callingMethod
{
    // Validate initialization
    if (![self validateInitializedWithCallingMethod:callingMethod]) {
        return ADMOB_ERROR_NOT_INITIALIZED;
    }
    
    // Validate Ad Unit ID
    if (![self validateAdId:self.bannerAdUnitId callingMethod:callingMethod]) {
        return ADMOB_ERROR_INVALID_AD_ID;
    }
    
    // Remove the previous banner view if it exists
    if (self.bannerView != nil) {
        [self deleteBannerAdView];
    }
    
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
                                  adType:@"Banner"
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
    
    return ADMOB_OK;
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
        case ADMOB_BANNER_ALIGNMENT_LEFT:
            horizontalConstraint = [bannerView.leadingAnchor constraintEqualToAnchor:g_controller.view.leadingAnchor];
            break;
        case ADMOB_BANNER_ALIGNMENT_CENTER:
            horizontalConstraint = [bannerView.centerXAnchor constraintEqualToAnchor:g_controller.view.centerXAnchor];
            break;
        case ADMOB_BANNER_ALIGNMENT_RIGHT:
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
    // Check if is loaded
    if (self.appOpenAd == nil) {
        NSLog(@"%s :: There is no app open ad loaded.", callingMethod);
        return NO;
    }
    
    // Check if is expired
    NSTimeInterval dateDifference = [[NSDate date] timeIntervalSinceDate: self.appOpenAdLoadTime];
    BOOL expired = dateDifference >= (3600 * self.appOpenAdExpirationTime);
    if (expired) {
        NSLog(@"%s :: The loaded app open ad expired, reloading...", callingMethod);
        return NO;
    }
    
    // Check if is correct orientation
    UIInterfaceOrientation currentOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    if (currentOrientation != self.appOpenAdOrientation) {
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
    self.isShowingAd = NO;
}

-(void) onStop
{
    // Clean up Banner Ad
    if (self.bannerView != nil) {
        [self deleteBannerAdView];
        self.bannerView = nil;
    }
    
    // Clear Interstitial Ads
    [self freeLoadedInstances:self.interstitialAdQueue count:-1 withCleaner:^(id ad){
        [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
    }];
    
    // Clear Rewarded Ads
    [self freeLoadedInstances:self.rewardedAdQueue count:-1 withCleaner:^(id ad){
        [self cleanUpRewardedAd:(GADRewardedAd *)ad];
    }];
    
    // Clear Rewarded Interstitial Ads
    [self freeLoadedInstances:self.rewardedInterstitialAdQueue count:-1 withCleaner:^(id ad){
        [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
    }];
    
    // Nullify App Open Ad
    if (self.appOpenAd != nil) {
        [self cleanAd:self.appOpenAd withCleaner:^(id ad){
            [self cleanUpAppOpenAd:(GADAppOpenAd *)ad];
        }];
        self.appOpenAd = nil;
    }
    
    // Nullify Consent Form
    self.consentForm = nil;
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

- (void)freeLoadedInstances:(ThreadSafeQueue *)queue count:(double)count withCleaner:(AdCleanerBlock)cleaner {
    __block NSInteger blockCount = count;
    dispatch_async(dispatch_get_main_queue(), ^{
        if (blockCount < 0) {
            blockCount = [queue size];
        }
        
        while (blockCount > 0) {
            id ad = [queue dequeue];
            if (ad != nil) {
                cleaner(ad);
            }
            else break;
            blockCount--;
        }
    });
}

- (void)trimLoadedAdsQueue:(ThreadSafeQueue *)queue maxSize:(int)maxSize withCleaner:(AdCleanerBlock)cleaner {
    int size = (int)[queue size];
    if (size <= maxSize) return;
    
    [self freeLoadedInstances:queue count:size - maxSize withCleaner:cleaner];
}

-(void)sendAsyncEvent:(const char *)eventType
            eventData:(gm::wire::StructStream)payload
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *rawEvent =
            eventType ? [NSString stringWithUTF8String:eventType] : @"";

        NSString *normalizedEvent =
            AdMobSnakeCase(rawEvent);

        gm::wire::GMFunction callback = nil;
        BOOL clearShowCallback = NO;

        if ([normalizedEvent isEqualToString:@"admob_on_initialized"])
        {
            callback = g_initialize_callback;
            g_initialize_callback = nil;
        }
        else if ([normalizedEvent hasPrefix:@"admob_banner_"])
        {
            callback = g_banner_callback;
        }
        else if ([normalizedEvent isEqualToString:@"admob_interstitial_on_loaded"] ||
                 [normalizedEvent isEqualToString:@"admob_interstitial_on_load_failed"])
        {
            if (!g_interstitial_load_callbacks.empty())
            {
                callback = g_interstitial_load_callbacks.front();
                g_interstitial_load_callbacks.pop_front();
            }
        }
        else if ([normalizedEvent isEqualToString:@"admob_interstitial_on_fully_shown"])
        {
            callback = g_interstitial_show_callback;
        }
        else if ([normalizedEvent isEqualToString:@"admob_interstitial_on_dismissed"] ||
                 [normalizedEvent isEqualToString:@"admob_interstitial_on_show_failed"])
        {
            callback = g_interstitial_show_callback;
            clearShowCallback = YES;
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_video_on_loaded"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_video_on_load_failed"])
        {
            if (!g_rewarded_video_load_callbacks.empty())
            {
                callback = g_rewarded_video_load_callbacks.front();
                g_rewarded_video_load_callbacks.pop_front();
            }
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_video_on_fully_shown"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_video_on_reward"])
        {
            callback = g_rewarded_video_show_callback;
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_video_on_dismissed"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_video_on_show_failed"])
        {
            callback = g_rewarded_video_show_callback;
            clearShowCallback = YES;
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_loaded"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_load_failed"])
        {
            if (!g_rewarded_interstitial_load_callbacks.empty())
            {
                callback = g_rewarded_interstitial_load_callbacks.front();
                g_rewarded_interstitial_load_callbacks.pop_front();
            }
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_fully_shown"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_reward"])
        {
            callback = g_rewarded_interstitial_show_callback;
        }
        else if ([normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_dismissed"] ||
                 [normalizedEvent isEqualToString:@"admob_rewarded_interstitial_on_show_failed"])
        {
            callback = g_rewarded_interstitial_show_callback;
            clearShowCallback = YES;
        }
        else if ([normalizedEvent isEqualToString:@"admob_app_open_ad_on_loaded"] ||
                 [normalizedEvent isEqualToString:@"admob_app_open_ad_on_load_failed"])
        {
            callback =
                g_app_open_load_callback
                    ? g_app_open_load_callback
                    : g_app_open_enable_callback;

            g_app_open_load_callback = nil;
        }
        else if ([normalizedEvent isEqualToString:@"admob_app_open_ad_on_fully_shown"])
        {
            callback =
                g_app_open_show_callback
                    ? g_app_open_show_callback
                    : g_app_open_enable_callback;
        }
        else if ([normalizedEvent isEqualToString:@"admob_app_open_ad_on_dismissed"] ||
                 [normalizedEvent isEqualToString:@"admob_app_open_ad_on_show_failed"])
        {
            callback =
                g_app_open_show_callback
                    ? g_app_open_show_callback
                    : g_app_open_enable_callback;

            g_app_open_show_callback = nil;
        }
        else if ([normalizedEvent isEqualToString:@"admob_consent_on_request_info_updated"] ||
                 [normalizedEvent isEqualToString:@"admob_consent_on_request_info_update_failed"])
        {
            callback = g_consent_request_callback;
            g_consent_request_callback = nil;
        }
        else if ([normalizedEvent isEqualToString:@"admob_consent_on_loaded"] ||
                 [normalizedEvent isEqualToString:@"admob_consent_on_load_failed"])
        {
            callback = g_consent_load_callback;
            g_consent_load_callback = nil;
        }
        else if ([normalizedEvent isEqualToString:@"admob_consent_on_shown"] ||
                 [normalizedEvent isEqualToString:@"admob_consent_on_show_failed"])
        {
            callback = g_consent_show_callback;
            g_consent_show_callback = nil;
        }
        else if ([normalizedEvent isEqualToString:@"admob_on_paid_event"])
        {
            callback = g_paid_event_callback;
        }

        AdMobInvokeCallback(callback, payload);

        if (clearShowCallback)
        {
            if ([normalizedEvent hasPrefix:@"admob_interstitial_"])
                g_interstitial_show_callback = nil;
            else if ([normalizedEvent hasPrefix:@"admob_rewarded_video_"])
                g_rewarded_video_show_callback = nil;
            else if ([normalizedEvent hasPrefix:@"admob_rewarded_interstitial_"])
                g_rewarded_interstitial_show_callback = nil;
        }
    });
}

-(void)onPaidEventHandler:(GADAdValue*) value adUnitId:(NSString*)adUnitId adType:(NSString*)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
        gm::wire::StructStream eventData =
        AdMobPayload(@"AdMob_OnPaidEvent");
    eventData.add("mediation_adapter_class_name", AdMobCString(mediationAdapterClassName));
    eventData.add("unit_id", AdMobCString(adUnitId));
    eventData.add("ad_type", AdMobCString(adType));
    eventData.add("micros", value.value.doubleValue * 1000000.0);
    eventData.add("currency_code", AdMobCString(value.currencyCode));
    eventData.add("precision", (int32_t)value.precision);
    eventData.add("ad_source_name", AdMobCString(loadedAdNetworkResponseInfo.adSourceName));
    eventData.add("ad_source_id", AdMobCString(loadedAdNetworkResponseInfo.adSourceID));
    eventData.add("ad_source_instance_name", AdMobCString(loadedAdNetworkResponseInfo.adSourceInstanceName));
    eventData.add("ad_source_instance_id", AdMobCString(loadedAdNetworkResponseInfo.adSourceInstanceID));
    [self sendAsyncEvent:"AdMob_OnPaidEvent" eventData:eventData];
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

- (BOOL)validateLoadedAdsLimit:(ThreadSafeQueue *)queue maxSize:(int)maxSize callingMethod:(const char *)callingMethod {
    if ([queue size] >= (NSUInteger)maxSize) {
        NSLog(@"%s :: Maximum number of loaded ads reached.", callingMethod);
        return NO;
    }
    return YES;
}

- (BOOL)validateAdLoaded:(ThreadSafeQueue *)queue callingMethod:(const char *)callingMethod {
    if ([queue size] == 0) {
        NSLog(@"%s :: There is no loaded ad in queue.", callingMethod);
        return NO;
    }
    return YES;
}

@end
