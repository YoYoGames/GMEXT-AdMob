#import "GoogleMobileAdsGM.h"

const int EVENT_OTHER_SOCIAL = 70;
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


const int ADMOB_ERROR_NOT_INITIALIZED = -1;
const int ADMOB_ERROR_INVALID_AD_ID = -2;
const int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
const int ADMOB_ERROR_NO_ADS_LOADED = -4;
const int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
const int ADMOB_ERROR_ILLEGAL_CALL = -6;

@implementation ThreadSafeQueue

- (instancetype)init {
    self = [super init];
    if (self) {
        _array = [[NSMutableArray alloc] init];
        _queue = dispatch_queue_create("com.myapp.threadSafeQueue", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

- (void)enqueue:(id)object {
    dispatch_sync(_queue, ^{
        [_array addObject:object];
    });
}

- (id)dequeue {
    __block id object = nil;
    dispatch_sync(_queue, ^{
        if ([_array count] > 0) {
            object = [_array firstObject];
            [_array removeObjectAtIndex:0];
        }
    });
    return object;
}

- (NSUInteger)size {
    __block NSUInteger count;
    dispatch_sync(_queue, ^{
        count = [_array count];
    });
    return count;
}

@end

@implementation GoogleMobileAdsGM

-(id)init {
    if ( self = [super init] ) {
        
        _isInitialized = false;
        _isTestDevice = false;
        _NPA = false;
        
        _bannerAdUnitId = @"";
        _interstitialAdUnitId = @"";
        _rewardedVideoUnitId = @"";
        _rewardedInterstitialAdUnitId = @"";
        _appOpenAdUnitId = @"";
        
        _interstitialMaxLoadedInstances = 1;
        _loadedInterstitialQueue = [[ThreadSafeQueue alloc] init];
        
        _rewardedVideoMaxLoadedInstances = 1;
        _loadedRewardedVideoQueue = [[ThreadSafeQueue alloc] init];
        
        _rewardedInterstitialMaxLoadedInstances = 1;
        _loadedRewardedInterstitialQueue = [[ThreadSafeQueue alloc] init];
        
        _triggerPaidEventCallback = false;
        
        _isAppOpenAdEnabled = false;
        _appOpenAdOrientation = UIInterfaceOrientationLandscapeRight;
        
        return self;
    }
    return NULL;
}

/////////////////////////////////////////////////////GoogleMobileAds

-(double) AdMob_Initialize
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    if (_isTestDevice) {
        #if TARGET_OS_SIMULATOR
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = @[ GADSimulatorID ];
        NSLog(@"Testing on Simulator: %@", GADSimulatorID);
        #else
        NSString *device = [NSString stringWithCString: getDeviceId() encoding: NSUTF8StringEncoding];
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = @[device];
        NSLog(@"Testing on Real Device: %@", device);
        #endif
    }

    //[[GADMobileAds sharedInstance] startWithCompletionHandler:nil];
    GADMobileAds *ads = [GADMobileAds sharedInstance];
    [ads startWithCompletionHandler:^(GADInitializationStatus *status)
    {
        NSDictionary *adapterStatuses = [status adapterStatusesByClassName];
        for (NSString *adapter in adapterStatuses)
        {
            GADAdapterStatus *adapterStatus = adapterStatuses[adapter];
            NSLog(@"Adapter Name: %@, Description: %@, Latency: %f", adapter,
            adapterStatus.description, adapterStatus.latency);
        }

        // Initialize ad types using extension options (defaults)
        const char* temp;
        temp = extOptGetString((char*)"AdMob", (char*)"iOS_BANNER");
        if (temp && strlen(temp) > 0) {
            _bannerAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_INTERSTITIAL");
        if (temp && strlen(temp) > 0) {
            _interstitialAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED");
        if (temp && strlen(temp) > 0) {
            _rewardedVideoUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED_INTERSTITIAL");
        if (temp && strlen(temp) > 0) {
            _rewardedInterstitialAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_OPENAPPAD");
        if (temp && strlen(temp) > 0) {
            _appOpenAdUnitId = [NSString stringWithUTF8String: temp];
        }
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_OnInitialized");
        createSocialAsyncEventWithDSMap(dsMapIndex);
        
        _isInitialized = true;
    }];
    
    return 0;
}

-(double) AdMob_SetTestDeviceId
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    _isTestDevice = true;
    return 0;
}

-(void)bannerView:(nonnull GADBannerView *)bannerView didFailToReceiveAdWithError:(nonnull NSError *)error{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoadFailed");
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoaded");
    dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[bannerView.adUnitID UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad failed to present full screen content.
-(void)ad:(nonnull id<GADFullScreenPresentingAd>)ad didFailToPresentFullScreenContentWithError:(nonnull NSError *)error
{
    isShowingAd = false;

    int dsMapIndex = dsMapCreate();
    if ([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if ([ad isMemberOfClass:[GADRewardedAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if ([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        [self loadAppOpenAd];
    }
    
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad presented full screen content.
-(void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad
{
    int dsMapIndex = dsMapCreate();

    if([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        [self loadAppOpenAd];
    }

    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad dismissed full screen content.
-(void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad
{
    isShowingAd = false;
    int dsMapIndex = dsMapCreate();

    if([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        [self loadAppOpenAd];
    }

    createSocialAsyncEventWithDSMap(dsMapIndex);
}

///// BANNER //////////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Banner_Set_AdUnit:(NSString*) adUnitId
{
    _bannerAdUnitId = adUnitId;
}

-(void) deleteBannerAdView {
    [_bannerView removeFromSuperview];
    _bannerView.delegate = nil;
    _bannerView = nil;
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

-(double) AdMob_Banner_Create:(double) size bottom: (double)bottom
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:_bannerAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if(_bannerView != nil)
    {
        [self deleteBannerAdView];
    }

    GADAdSize bannerSize = getBannerSize(size);
    _bannerView = [[GADBannerView alloc] initWithAdSize:bannerSize];
    
    _bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    _bannerView.adUnitID = _bannerAdUnitId;
    _bannerView.rootViewController = g_controller;
    _bannerView.delegate = self;
    [g_glView addSubview:self.bannerView];

    
    if(_triggerPaidEventCallback) {
        _bannerView.paidEventHandler = ^void(GADAdValue *_Nonnull value)
        {
            GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = _bannerView.responseInfo.loadedAdNetworkResponseInfo;
            [self onPaidEventHandler:value adUnitId: _bannerView.adUnitID adType:@"Banner" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:_bannerView.responseInfo.adNetworkClassName];
        };
    }
    
    [self AdMob_Banner_Move:bottom];
    
    GADRequest *request = [self AdMob_AdRequest];
    
    [_bannerView loadRequest:request];
    
    return 0;
}

-(double)AdMob_Banner_Move:(double)bottom
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if(_bannerView != nil) {
        CGSize size = CGSizeFromGADAdSize(_bannerView.adSize);
        CGFloat adWidth = size.width;
        CGFloat adHeight = size.height;

        CGFloat x = (g_glView.bounds.size.width - adWidth) / 2; // Center horizontally (ALWAYS)
        CGFloat y = bottom ? (g_glView.bounds.size.height - adHeight) : 0; // Position at bottom or top

        CGRect frame = CGRectMake(x, y, adWidth, adHeight);
        _bannerView.frame = frame;
    }
    
    return 0;
}

-(double) AdMob_Banner_GetWidth
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return 0;
    
    CGSize size = CGSizeFromGADAdSize(_bannerView.adSize);
    int adW = size.width;

    int dispW = (int)(( adW * g_DeviceWidth ) / g_glView.bounds.size.width);
    return dispW;
}

-(double) AdMob_Banner_GetHeight
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return 0;
    
    CGSize size = CGSizeFromGADAdSize(_bannerView.adSize);
    int adH = size.height;

    int dispH = (int)(( adH * g_DeviceHeight ) / g_glView.bounds.size.height);
    return dispH;
}

-(double) AdMob_Banner_Hide
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( _bannerView != nil )
    {
        _bannerView.hidden = true;
    }
    return 0;
}

-(double) AdMob_Banner_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( _bannerView != nil )
    {
        _bannerView.hidden = false;
    }
    return 0;
}

-(double) AdMob_Banner_Remove
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    [self deleteBannerAdView];
    
    return 0;
}

///// INTERSTITIAL ////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Interstitial_Set_AdUnit:(NSString*) adUnitId
{
    _interstitialAdUnitId = adUnitId;
}

-(void) Admob_Interstitial_Free_Loaded_Instances:(double) count
{
    if (count < 0)
    {
        count = [_loadedInterstitialQueue size];
    }
    
    while (count > 0 && [_loadedInterstitialQueue size]) {
        [_loadedInterstitialQueue dequeue];
        count--;
    }
}

-(void) Admob_Interstitial_Max_Instances:(double) value
{
    _interstitialMaxLoadedInstances = value;
    
    NSUInteger size = [_loadedInterstitialQueue size];
    if (value >= size) return;
    
    [self Admob_Interstitial_Free_Loaded_Instances: size - value];
}

-(double) AdMob_Interstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:_interstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:_loadedInterstitialQueue maxSize:_interstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
        
    const NSString* adUnitId = _interstitialAdUnitId;
    
    GADRequest *requestInterstitial = [GADRequest request];
    [GADInterstitialAd loadWithAdUnitID: _interstitialAdUnitId request:requestInterstitial completionHandler:^(GADInterstitialAd *interstitialAd, NSError *error)
    {
        if (error)
        {
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", (double)error.code);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            return;
        }
        
        if (![self validateLoadedAdsLimit:_loadedInterstitialQueue maxSize:_interstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return;
            
        [_loadedInterstitialQueue enqueue: interstitialAd];
        
        interstitialAd.fullScreenContentDelegate = self;
        
        if (_triggerPaidEventCallback) {
            const GADInterstitialAd* interstitialRef = interstitialAd;
            
            interstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = interstitialRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:interstitialRef.adUnitID adType:@"Interstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:interstitialRef.responseInfo.adNetworkClassName];
            };
        }
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnLoaded");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    return 0;
}

-(double) AdMob_Interstitial_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdLoaded:_loadedInterstitialQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    GADInterstitialAd *interstitialAdRef = [_loadedInterstitialQueue dequeue];
        
    [interstitialAdRef presentFromRootViewController:g_controller];
    
    _interstitialKeepMe = interstitialAdRef;
    _isShowingAd = true;

    return 0;
}

-(double) AdMob_Interstitial_IsLoaded
{
    return [self AdMob_Interstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_Interstitial_Instances_Count
{
    return [_loadedInterstitialQueue size];
}

///// REWARDED VIDEO //////////////////////////////////////////////////////////////////////////////

-(void) AdMob_RewardedVideo_Set_AdUnit:(NSString*) adUnitId
{
    _rewardedVideoUnitId = adUnitId;
}

-(void) AdMob_RewardedVideo_Free_Loaded_Instances:(double) count
{
    if (count < 0)
    {
        count = [_loadedRewardedVideoQueue size];
    }
    
    while (count > 0 && [_loadedRewardedVideoQueue size]) {
        [_loadedRewardedVideoQueue dequeue];
        count--;
    }
}

-(void) AdMob_RewardedVideo_Max_Instances:(double) value
{
    _rewardedVideoMaxLoadedInstances = value;
    
    NSUInteger size = [_loadedRewardedVideoQueue size];
    if (value >= size) return;
    
    [self AdMob_RewardedVideo_Free_Loaded_Instances: size - value];
}

-(double) AdMob_RewardedVideo_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:_rewardedVideoUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:_loadedRewardedVideoQueue maxSize:_rewardedVideoMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = _rewardedVideoUnitId;
    
    GADRequest *requestRewarded = [GADRequest request];
    [GADRewardedAd loadWithAdUnitID: self.rewardedVideoUnitId request: requestRewarded completionHandler:^(GADRewardedAd *rewardedAd, NSError *error)
    {
        if (error)
        {
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            return;
        }

        if (![self validateLoadedAdsLimit:_loadedRewardedVideoQueue maxSize:_rewardedVideoMaxLoadedInstances callingMethod:__FUNCTION__]) return;
        
        [_loadedRewardedVideoQueue enqueue: rewardedAd];
        
        rewardedAd.fullScreenContentDelegate = self;
        
        if(_triggerPaidEventCallback) {
            const GADRewardedAd* rewardedRef = rewardedAd;
            
            rewardedAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = rewardedRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:rewardedRef.adUnitID adType:@"Rewarded" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:rewardedRef.responseInfo.adNetworkClassName];
            };
        }

        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnLoaded");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    return 0;
}

-(double) AdMob_RewardedVideo_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdLoaded:_loadedRewardedVideoQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    const GADRewardedAd *rewardAdRef = [_loadedRewardedVideoQueue dequeue];
            
    [rewardAdRef presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnReward");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[rewardAdRef.adUnitID UTF8String]);
        dsMapAddDouble(dsMapIndex, (char*)"reward_amount", [rewardAdRef.adReward.amount doubleValue]);
        dsMapAddString(dsMapIndex, (char*)"reward_type", (char*)[rewardAdRef.adReward.type UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    _rewardAdKeepMe = rewardAdRef;
    _isShowingAd = true;
    
    return 0;
}

-(double) AdMob_RewardedVideo_IsLoaded
{
    return [self AdMob_RewardedVideo_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedVideo_Instances_Count
{
    return [_loadedRewardedVideoQueue size];
}

///// REWARDED INTESTITIAL ////////////////////////////////////////////////////////////////////////

-(void) AdMob_RewardedInterstitial_Set_UnitId:(NSString*) adUnitId
{
    self.rewardedInterstitialAdUnitId = adUnitId;
}

-(void) AdMob_RewardedInterstitial_Free_Loaded_Instances:(double) count
{
    if (count < 0)
    {
        count = [_loadedRewardedInterstitialQueue size];
    }
    
    while (count > 0 && [_loadedRewardedInterstitialQueue size]) {
        [_loadedRewardedInterstitialQueue dequeue];
        count--;
    }
}

-(void) AdMob_RewardedInterstitial_Max_Instances:(double) value
{
    _rewardedInterstitialMaxLoadedInstances = value;
    
    NSUInteger size = [_loadedRewardedInterstitialQueue size];
    if (value >= size) return;
    
    [self AdMob_RewardedInterstitial_Free_Loaded_Instances: size - value];
}

-(double) AdMob_RewardedInterstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:_rewardedInterstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:_loadedRewardedInterstitialQueue maxSize:_rewardedInterstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = _rewardedInterstitialAdUnitId;
    
    [GADRewardedInterstitialAd loadWithAdUnitID:self.rewardedInterstitialAdUnitId request:[GADRequest request] completionHandler:^(GADRewardedInterstitialAd* _Nullable rewardedInterstitialAd, NSError* _Nullable error)
    {
        if (error)
        {
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            return;
        }

        if (![self validateLoadedAdsLimit:_loadedRewardedInterstitialQueue maxSize:_rewardedInterstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return;
        
        [_loadedRewardedInterstitialQueue enqueue:rewardedInterstitialAd];
        
        rewardedInterstitialAd.fullScreenContentDelegate = self;
        
        if(_triggerPaidEventCallback) {
            const GADRewardedInterstitialAd* rewardedInterstitialRef = rewardedInterstitialAd;
            
            rewardedInterstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = rewardedInterstitialRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:rewardedInterstitialRef.adUnitID adType:@"RewardedInterstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:rewardedInterstitialRef.responseInfo.adNetworkClassName];
            };
        }

        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnLoaded");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    return 0;
}

-(double) AdMob_RewardedInterstitial_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdLoaded:_loadedRewardedInterstitialQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    const GADRewardedInterstitialAd *rewardInterstitialAdRef = [_loadedRewardedInterstitialQueue dequeue];
            
    [rewardInterstitialAdRef presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnReward");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[rewardInterstitialAdRef.adUnitID UTF8String]);
        dsMapAddDouble(dsMapIndex, (char*)"reward_amount", [rewardInterstitialAdRef.adReward.amount doubleValue]);
        dsMapAddString(dsMapIndex, (char*)"reward_type", (char*)[rewardInterstitialAdRef.adReward.type UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    _rewardedInterstitialAdKeepMe = rewardInterstitialAdRef;
    _isShowingAd = true;
    
    return 0;
}

-(double) AdMob_RewardedInterstitial_IsLoaded
{
    return [self AdMob_RewardedInterstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedInterstitial_Instances_Count
{
    return [_loadedRewardedInterstitialQueue size];
}

/////////////////App Open Ad/////////////////////////////////////////////////////////////////////////
///

-(void) AdMob_AppOpenAd_Set_UnitId:(NSString*) adUnitId
{
    _appOpenAdUnitId = adUnitId;
}


-(double) AdMob_AppOpenAd_Enable:(double) orientation
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:_appOpenAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    _isAppOpenAdEnabled = true;
    _appOpenAd = nil;
    _appOpenAdOrientation = (orientation == 0) ? UIInterfaceOrientationLandscapeRight : UIInterfaceOrientationPortrait;
    [self loadAppOpenAd];
    
    return 0;
}

-(void) AdMob_AppOpenAd_Disable
{
    _isAppOpenAdEnabled = false;
    _appOpenAd = nil;
}

-(double) AdMob_AppOpenAd_IsEnabled
{
    return _isAppOpenAdEnabled ? 1.0 : 0.0;
}

-(void) onResume
{
    [self AdMob_AppOpenAd_Show];
}

-(void) loadAppOpenAd
{
    if(!_isAppOpenAdEnabled)
        return;
    
    if (![self validateInitializedWithCallingMethod:"__AdMob_AppOpenAd_Load"]) return;
    
    if (![self validateAdId:_appOpenAdUnitId callingMethod:"__AdMob_AppOpenAd_Load"]) return;
    
    const NSString* adUnitId = _appOpenAdUnitId;
    
    _appOpenAd = nil;
    [GADAppOpenAd loadWithAdUnitID: self.appOpenAdUnitId request:[GADRequest request] orientation:_appOpenAdOrientation completionHandler:^(GADAppOpenAd *_Nullable appOpenAd, NSError *_Nullable error) {
        if (error) {
            
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            
            return;
        }
        self.appOpenAd = appOpenAd;
        self.appOpenAd.fullScreenContentDelegate = self;
        self.loadTime = [NSDate date];
        
        if(_triggerPaidEventCallback)
            self.appOpenAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
        {
            GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = _appOpenAd.responseInfo.loadedAdNetworkResponseInfo;
            [self onPaidEventHandler:value adUnitId:adUnitId adType:@"AppOpen" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:_appOpenAd.responseInfo.adNetworkClassName];
        };
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoaded");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_AppOpenAd_Show
{
    if(!_isAppOpenAdEnabled)
        return;

    if([self AdMob_AppOpenAd_IsLoaded]<0.5)
        return;
    
    if (self.appOpenAd)
    {
        self.appOpenAd.fullScreenContentDelegate = self;
        [self.appOpenAd presentFromRootViewController:g_controller];
    }
}

-(double) AdMob_AppOpenAd_IsLoaded
{
    if (self.appOpenAd && [self wasLoadTimeLessThanNHoursAgo:4])
        return 1.0;
    else
        return 0.0;
}

///// TARGETING ///////////////////////////////////////////////////////////////////////////////////

//https://developers.google.com/admob/ios/targeting#child-directed_setting
-(void) AdMob_Targeting_COPPA:(double) COPPA
{
    if(COPPA > 0.5)
        [GADMobileAds.sharedInstance.requestConfiguration tagForChildDirectedTreatment:YES];
    else
        [GADMobileAds.sharedInstance.requestConfiguration tagForChildDirectedTreatment:NO];
}

//https://developers.google.com/admob/ios/targeting#users_under_the_age_of_consent
-(void) AdMob_Targeting_UnderAge:(double) underAge
{
    if(underAge > 0.5)
        [GADMobileAds.sharedInstance.requestConfiguration tagForUnderAgeOfConsent:YES];
    else
        [GADMobileAds.sharedInstance.requestConfiguration tagForUnderAgeOfConsent:NO];
}

//https://developers.google.com/admob/ios/targeting#ad_content_filtering
///[GADMobileAds.sharedInstance.requestConfiguration tagForUnderAgeOfConsent:YES];
-(void) AdMob_Targeting_MaxAdContentRating:(double) maxAdContentRating
{
    switch((int) maxAdContentRating)
    {
        case 0:
            [GADMobileAds.sharedInstance.requestConfiguration setMaxAdContentRating :GADMaxAdContentRatingGeneral];
            break;
            
        case 1:
            [GADMobileAds.sharedInstance.requestConfiguration setMaxAdContentRating:GADMaxAdContentRatingParentalGuidance];
            break;
        case 2:
            [GADMobileAds.sharedInstance.requestConfiguration setMaxAdContentRating:GADMaxAdContentRatingTeen];
            break;
            
        case 3:
            [GADMobileAds.sharedInstance.requestConfiguration setMaxAdContentRating:GADMaxAdContentRatingMatureAudience];
            break;
    }
}

///// UTILS ///////////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_NonPersonalizedAds_Set:(double) value
{
    _NPA = value >= 0.5;
}

BOOL isShowingAd = false;
-(double) AdMob_IsShowingAd
{
    return isShowingAd ? 1.0 : 0.0;
}

///// CONSENT /////////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Consent_RequestInfoUpdate:(double) testing
{
    UMPRequestParameters *parameters = [[UMPRequestParameters alloc] init];
    
    if(testing != 3)
    {
        NSLog(@"Testing the UMP");

        UMPDebugSettings *debugSettings = [[UMPDebugSettings alloc] init];

        debugSettings.testDeviceIdentifiers = @[ [[[UIDevice currentDevice] identifierForVendor] UUIDString] ];
        debugSettings.geography = (UMPDebugGeography)testing;
        parameters.debugSettings = debugSettings;
    }
    
    [UMPConsentInformation.sharedInstance requestConsentInfoUpdateWithParameters:parameters completionHandler:^(NSError *_Nullable error)
    {
        int dsMapIndex = dsMapCreate();

        if (error)
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnRequestInfoUpdateFailed");
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
        }
        else
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnRequestInfoUpdated");
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(double) AdMob_Consent_GetStatus
{
    https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentStatus.html#REQUIRED
    switch(UMPConsentInformation.sharedInstance.consentStatus)
    {
        case 0: //UNKNOWN
        return 0;
        
        case 1: //REQUIRED
        return 2;
        
        case 2: //NOT_REQUIRED
        return 1;
        
        case 3: //OBTAINED
        return 3;
    }
    return 0;
}

-(double) AdMob_Consent_GetType
{
    if (UMPConsentInformation.sharedInstance.consentStatus == UMPConsentStatusObtained)
    {
        // if (!canShowAds()) return 3.0; // DECLINED

        return canShowPersonalizedAds() ? /* PERSONALIZED */2.0 : /* NON-PERSONALIZED */1.0;
    }
    return 0; // UNKNOWN
}

-(double) AdMob_Consent_IsFormAvailable
{
    if(UMPConsentInformation.sharedInstance.formStatus == 1)
        return 1.0;
    else
        return 0.0;
}
                                     
-(void) AdMob_Consent_Load
{
    [UMPConsentForm loadWithCompletionHandler:^(UMPConsentForm *form_, NSError *loadError)
    {
        int dsMapIndex = dsMapCreate();

        if (loadError)
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnLoadFailed");
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", loadError.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[loadError.localizedDescription UTF8String]);
        }
        else
        {
            self.myForm = form_;

            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnLoaded");
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_Consent_Show
{
    [self.myForm presentFromViewController:g_controller completionHandler:^(NSError *_Nullable dismissError)
    {
        int dsMapIndex = dsMapCreate();

        if (UMPConsentInformation.sharedInstance.consentStatus == UMPConsentStatusObtained and !dismissError)
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnShown");
        }
        else
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnShowFailed");
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", dismissError.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[dismissError.localizedDescription UTF8String]);
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_Consent_Reset
{
    [UMPConsentInformation.sharedInstance reset];
}

///// SETTINGS ////////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Settings_SetVolume:(double) value
{
    GADMobileAds.sharedInstance.applicationVolume = value;
}
    
-(void) AdMob_Settings_SetMuted:(double) value
{
    if(value >= 0.5)
        GADMobileAds.sharedInstance.applicationMuted = YES;
    else
        GADMobileAds.sharedInstance.applicationMuted = NO;
}

///// INTERNAL ///////////////////////////////////////////////////////////////////////////////////


double loadTime = 0;
- (Boolean)wasLoadTimeLessThanNHoursAgo:(int)n {
    NSDate *now = [NSDate date];
    NSTimeInterval timeIntervalBetweenNowAndLoadTime = [now timeIntervalSinceDate:self.loadTime];
    double secondsPerHour = 3600.0;
    double intervalInHours = timeIntervalBetweenNowAndLoadTime / secondsPerHour;
    return intervalInHours < n;
}

-(GADRequest*) AdMob_AdRequest
{
    GADRequest *request = [GADRequest request];

    // As instructed by Google
    //request.requestAgent = [NSString stringWithFormat:@"gmext-admob-%s", extGetVersion("AdMob")];

    if(_NPA)
    {
        GADExtras *extras = [[GADExtras alloc] init];
        extras.additionalParameters = @{@"npa": @"1"};
        [request registerAdNetworkExtras: extras];
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
    if (input == nil) return false;
    return input.length >= index && [input characterAtIndex:(NSUInteger)index-1] == '1';
}

Boolean hasConsentFor(int* indexes, int size, NSString* purposeConsent, Boolean hasVendorConsent)
{
    int index;
    for (int i = 0; i < size; i++)
    {
        index = indexes[i];
        if (!hasAttribute(purposeConsent, index)) {
            NSLog(@"hasConsentFor: denied for purpose #%d", index);
            return false;
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
            return false;
        }
    }

    return true;
}

-(void) AdMob_Enable_PaidEvent_Callback:(double) enable
{
    _triggerPaidEventCallback = enable >= 0.5;
}


-(void)onPaidEventHandler:(GADAdValue*) value adUnitId:(const NSString*)adUnitId adType:(NSString*)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_onPaidEvent");
    
    dsMapAddString(dsMapIndex, (char*)"mediation_adapter_class_name", (char*)[mediationAdapterClassName UTF8String]);
    
    dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"ad_type", (char*)[adType UTF8String]);
    
    dsMapAddDouble(dsMapIndex, (char*)"micros", value.value.doubleValue);
    dsMapAddString(dsMapIndex, (char*)"currency_code", (char*)[value.currencyCode UTF8String]);
    dsMapAddDouble(dsMapIndex, (char*)"precision", (double)value.precision);
    
    dsMapAddString(dsMapIndex, (char*)"ad_source_name", (char*)[loadedAdNetworkResponseInfo.adSourceName UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"ad_source_id", (char*)[loadedAdNetworkResponseInfo.adSourceID UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"ad_source_instance_name", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceName UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"ad_source_instance_id", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceID UTF8String]);
    
    createSocialAsyncEventWithDSMap(dsMapIndex);
};

/// VALIDATIONS

- (BOOL)validateNotInitializedWithCallingMethod:(const char *)callingMethod {
    if (_isInitialized) {
        NSLog(@"%s :: Method cannot be called after initialization.", callingMethod);
    }
    return !_isInitialized;
}

- (BOOL)validateInitializedWithCallingMethod:(const char *)callingMethod {
    if (!_isInitialized) {
        NSLog(@"%s :: Extension was not initialized.", callingMethod);
    }
    return _isInitialized;
}

- (BOOL)validateActiveBannerAdWithCallingMethod:(const char *)callingMethod {
    if (_bannerView == nil) {
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
    if ([queue size] >= maxSize) {
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
