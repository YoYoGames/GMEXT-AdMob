#import "GoogleMobileAdsGM.h"

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

@interface ThreadSafeQueue : NSObject

@property (nonatomic, strong) NSMutableArray *array;
@property (strong, nonatomic) id retainedObject;

- (void)enqueue:(id)object;
- (id)dequeue;
- (void)dequeueMultiple:(int) count;
- (NSUInteger)size;

@end

@implementation ThreadSafeQueue

- (instancetype)init {
    self = [super init];
    if (self) {
        _array = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void)enqueue:(id)object {
    @synchronized(self) {
        [_array addObject:object];
    }
}

- (id)dequeue {
    self.retainedObject = nil;
    @synchronized(self) {
        if ([_array count] > 0) {
            self.retainedObject = [_array firstObject];
            [_array removeObjectAtIndex:0];
        }
    }
    return self.retainedObject;
}

- (void)dequeueMultiple:(int) count {
    @synchronized (_array) {
        NSRange range;
        if (_array.count < count) {
            range = NSMakeRange(0, _array.count);
        } else {
            range = NSMakeRange(0, count);
        }
        [_array removeObjectsInRange:range];
    }
}

- (NSUInteger)size {
    NSUInteger count;
    @synchronized(self) {
        count = [_array count];
    }
    return count;
}

@end

@implementation GoogleMobileAdsGM

const int ADMOB_ERROR_NOT_INITIALIZED = -1;
const int ADMOB_ERROR_INVALID_AD_ID = -2;
const int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
const int ADMOB_ERROR_NO_ADS_LOADED = -4;
const int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
const int ADMOB_ERROR_ILLEGAL_CALL = -6;

-(id)init {
    if ( self = [super init] ) {
                
        self.isInitialized = false;
        self.isTestDevice = false;
        self.nonPersonalizedAds = false;
        
        self.bannerAdUnitId = @"";
        self.interstitialAdUnitId = @"";
        self.rewardedVideoUnitId = @"";
        self.rewardedInterstitialAdUnitId = @"";
        self.appOpenAdUnitId = @"";
        
        self.interstitialMaxLoadedInstances = 1;
        self.loadedInterstitialQueue = [[ThreadSafeQueue alloc] init];
        
        self.rewardedVideoMaxLoadedInstances = 1;
        self.loadedRewardedVideoQueue = [[ThreadSafeQueue alloc] init];
        
        self.rewardedInterstitialMaxLoadedInstances = 1;
        self.loadedRewardedInterstitialQueue = [[ThreadSafeQueue alloc] init];
        
        self.triggerOnPaidEvent = false;
        
        self.isAppOpenAdEnabled = false;
        self.appOpenAdOrientation = UIInterfaceOrientationLandscapeRight;
        
        return self;
    }
    return NULL;
}

#pragma mark - Setup Methods

-(double) AdMob_Initialize
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    if (self.isTestDevice) {
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
            self.bannerAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_INTERSTITIAL");
        if (temp && strlen(temp) > 0) {
            self.interstitialAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED");
        if (temp && strlen(temp) > 0) {
            self.rewardedVideoUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED_INTERSTITIAL");
        if (temp && strlen(temp) > 0) {
            self.rewardedInterstitialAdUnitId = [NSString stringWithUTF8String: temp];
        }

        temp = extOptGetString((char*)"AdMob", (char*)"iOS_OPENAPPAD");
        if (temp && strlen(temp) > 0) {
            self.appOpenAdUnitId = [NSString stringWithUTF8String: temp];
        }
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_OnInitialized");
        createSocialAsyncEventWithDSMap(dsMapIndex);
        
        self.isInitialized = true;
    }];
    
    return 0;
}

-(double) AdMob_SetTestDeviceId
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    self.isTestDevice = true;
    return 0;
}

-(void) AdMob_Events_OnPaidEvent:(double) enable
{
    self.triggerOnPaidEvent = enable >= 0.5;
}

-(void)onPaidEventHandler:(GADAdValue*) value adUnitId:(const NSString*)adUnitId adType:(NSString*)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_OnPaidEvent");
    
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

-(void) AdMob_NonPersonalizedAds_Set:(double) value
{
    self.nonPersonalizedAds = value >= 0.5;
}

#pragma mark - Delegate Methods

-(void)bannerView:(nonnull GADBannerView *)bannerView didFailToReceiveAdWithError:(nonnull NSError *)error
{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoadFailed");
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView
{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoaded");
    dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[bannerView.adUnitID UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

-(void)ad:(nonnull id<GADFullScreenPresentingAd>)presentingAd didFailToPresentFullScreenContentWithError:(nonnull NSError *)error
{
    self.isShowingAd = false;

    int dsMapIndex = dsMapCreate();
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)presentingAd).adUnitID UTF8String]);

    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)presentingAd).adUnitID UTF8String]);

    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)presentingAd).adUnitID UTF8String]);

    }
    else if([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpen_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[self.appOpenAdUnitId UTF8String]);
        [self loadAppOpenAd];
    }
    
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

-(void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    if([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpen_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[self.appOpenAdUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
        
        [self loadAppOpenAd];
    }
}

-(void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    self.isShowingAd = false;

    if([presentingAd isMemberOfClass:[GADInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADInterstitialAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADRewardedAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[((GADRewardedInterstitialAd*)presentingAd).adUnitID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }
    else if([presentingAd isMemberOfClass:[GADAppOpenAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpen_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[self.appOpenAdUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
        
        [self loadAppOpenAd];
    }

}

#pragma mark - Banner Methods

-(void) AdMob_Banner_Set_AdUnit:(NSString*) adUnitId
{
    self.bannerAdUnitId = adUnitId;
}

-(double) AdMob_Banner_Create:(double) size bottom: (double)bottom
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:self.bannerAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if(self.bannerView != nil)
    {
        [self deleteBannerAdView];
    }

    GADAdSize bannerSize = getBannerSize(size);
    self.bannerView = [[GADBannerView alloc] initWithAdSize:bannerSize];
    
    self.bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    self.bannerView.adUnitID = self.bannerAdUnitId;
    self.bannerView.rootViewController = g_controller;
    self.bannerView.delegate = self;
    [g_glView addSubview:self.bannerView];

    
    if(self.triggerOnPaidEvent) {
        self.bannerView.paidEventHandler = ^void(GADAdValue *_Nonnull value)
        {
            GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = self.bannerView.responseInfo.loadedAdNetworkResponseInfo;
            [self onPaidEventHandler:value adUnitId: self.bannerView.adUnitID adType:@"Banner" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo
           mediationAdapterClassName:self.bannerView.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
        };
    }
    
    [self AdMob_Banner_Move:bottom];
    
    GADRequest *request = [self buildAdRequest];
    [self.bannerView loadRequest:request];
    
    return 0;
}

- (void)addBannerViewToTopView:(UIView *)bannerView
{
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [g_glView addSubview:bannerView];
    [g_glView addConstraints:@[
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeTop
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:g_controller.topLayoutGuide
                                                            attribute:NSLayoutAttributeBottom
                                                           multiplier:1
                                                             constant:0],
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeCenterX
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:g_controller.view
                                                            attribute:NSLayoutAttributeCenterX
                                                           multiplier:1
                                                             constant:0]
                               ]];
}

-(void)addBannerViewToBottomView:(UIView *)bannerView
{
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [g_glView addSubview:bannerView];
    [g_glView addConstraints:@[
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeBottom
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:g_controller.bottomLayoutGuide
                                                            attribute:NSLayoutAttributeTop
                                                           multiplier:1
                                                             constant:0],
                               [NSLayoutConstraint constraintWithItem:bannerView
                                                            attribute:NSLayoutAttributeCenterX
                                                            relatedBy:NSLayoutRelationEqual
                                                               toItem:g_controller.view
                                                            attribute:NSLayoutAttributeCenterX
                                                           multiplier:1
                                                             constant:0]
                               ]];
}

-(double)AdMob_Banner_Move:(double)bottom
{

    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if(self.bannerView != nil) {
        
        [self.bannerView removeFromSuperview];
        
        if(bottom>0.5)
            [self addBannerViewToBottomView:self.bannerView];
        else
            [self addBannerViewToTopView:self.bannerView];

    }
    
    return 0;
}

-(double) AdMob_Banner_GetWidth
{    
    if (!self.bannerView) return 0;
    
    CGSize size = CGSizeFromGADAdSize(self.bannerView.adSize);
    int adW = size.width;

    int dispW = (int)(( adW * g_DeviceWidth ) / g_glView.bounds.size.width);
    return dispW;
}

-(double) AdMob_Banner_GetHeight
{    
    if (!self.bannerView) return 0;
    
    CGSize size = CGSizeFromGADAdSize(self.bannerView.adSize);
    int adH = size.height;

    int dispH = (int)(( adH * g_DeviceHeight ) / g_glView.bounds.size.height);
    return dispH;
}

-(double) AdMob_Banner_Hide
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = true;
    }
    return 0;
}

-(double) AdMob_Banner_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = false;
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

-(void) deleteBannerAdView{
    [self.bannerView removeFromSuperview];
    self.bannerView.delegate = nil;
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

-(void) AdMob_Interstitial_Set_AdUnit:(NSString*) adUnitId
{
    self.interstitialAdUnitId = adUnitId;
}

-(void) Admob_Interstitial_Free_Loaded_Instances:(double) count
{
    [self.loadedInterstitialQueue dequeueMultiple:count];
}

-(void) Admob_Interstitial_Max_Instances:(double) value
{
    self.interstitialMaxLoadedInstances = value;
    
    NSUInteger size = [self.loadedInterstitialQueue size];
    if (value >= size) return;
    
    [self Admob_Interstitial_Free_Loaded_Instances: size - value];
}

-(double) AdMob_Interstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:self.interstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.loadedInterstitialQueue maxSize:self.interstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
        
    const NSString* adUnitId = self.interstitialAdUnitId;
    
    GADRequest* request = [self buildAdRequest];
    [GADInterstitialAd loadWithAdUnitID: self.interstitialAdUnitId request: request completionHandler:^(GADInterstitialAd *interstitialAd, NSError *error)
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
        
        if (![self validateLoadedAdsLimit:self.loadedInterstitialQueue maxSize:self.interstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return;
        
        [self.loadedInterstitialQueue enqueue:interstitialAd];
        
        interstitialAd.fullScreenContentDelegate = self;
        
        if (self.triggerOnPaidEvent) {
            const GADInterstitialAd* interstitialRef = interstitialAd;
            
            interstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = interstitialRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:interstitialRef.adUnitID adType:@"Interstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:interstitialRef.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
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
    
    if (![self validateAdLoaded:self.loadedInterstitialQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    self.interstitialKeepMe = [self.loadedInterstitialQueue dequeue];
    
    [self.interstitialKeepMe presentFromRootViewController:g_controller];
    
    self.isShowingAd = true;

    return 0;
}

-(double) AdMob_Interstitial_IsLoaded
{
    return [self AdMob_Interstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_Interstitial_Instances_Count
{
    return [self.loadedInterstitialQueue size];
}

#pragma mark - Rewarded Video Methods

-(void) AdMob_RewardedVideo_Set_AdUnit:(NSString*) adUnitId
{
    self.rewardedVideoUnitId = adUnitId;
}

-(void) AdMob_RewardedVideo_Free_Loaded_Instances:(double) count
{
    [self.loadedRewardedVideoQueue dequeueMultiple: count];
}

-(void) AdMob_RewardedVideo_Max_Instances:(double) value
{
    self.rewardedVideoMaxLoadedInstances = value;
    
    NSUInteger size = [self.loadedRewardedVideoQueue size];
    if (value >= size) return;
    
    [self AdMob_RewardedVideo_Free_Loaded_Instances: size - value];
}

-(double) AdMob_RewardedVideo_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:self.rewardedVideoUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.loadedRewardedVideoQueue maxSize:self.rewardedVideoMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = self.rewardedVideoUnitId;
    
    GADRequest* request = [self buildAdRequest];
    [GADRewardedAd loadWithAdUnitID: self.rewardedVideoUnitId request: request completionHandler:^(GADRewardedAd *rewardedAd, NSError *error)
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

        if (![self validateLoadedAdsLimit:self.loadedRewardedVideoQueue maxSize:self.rewardedVideoMaxLoadedInstances callingMethod:__FUNCTION__]) return;
        
        [self.loadedRewardedVideoQueue enqueue: rewardedAd];
        
        rewardedAd.fullScreenContentDelegate = self;
        
        if(self.triggerOnPaidEvent) {
            const GADRewardedAd* rewardedRef = rewardedAd;
            
            rewardedAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = rewardedRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:rewardedRef.adUnitID adType:@"Rewarded" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:rewardedRef.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
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
    
    if (![self validateAdLoaded:self.loadedRewardedVideoQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    self.rewardVideoAdKeepMe = [self.loadedRewardedVideoQueue dequeue];
            
    [self.rewardVideoAdKeepMe presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnReward");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[self.rewardVideoAdKeepMe.adUnitID UTF8String]);
        dsMapAddDouble(dsMapIndex, (char*)"reward_amount", [self.rewardVideoAdKeepMe.adReward.amount doubleValue]);
        dsMapAddString(dsMapIndex, (char*)"reward_type", (char*)[self.rewardVideoAdKeepMe.adReward.type UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    self.isShowingAd = true;
    
    return 0;
}

-(double) AdMob_RewardedVideo_IsLoaded
{
    return [self AdMob_RewardedVideo_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedVideo_Instances_Count
{
    return [self.loadedRewardedVideoQueue size];
}

#pragma mark - Rewarded Interstitial Methods

-(void) AdMob_RewardedInterstitial_Set_UnitId:(NSString*) adUnitId
{
    self.rewardedInterstitialAdUnitId = adUnitId;
}

-(void) AdMob_RewardedInterstitial_Free_Loaded_Instances:(double) count
{
    [self.loadedRewardedInterstitialQueue dequeueMultiple: count];
}

-(void) AdMob_RewardedInterstitial_Max_Instances:(double) value
{
    self.rewardedInterstitialMaxLoadedInstances = value;
    
    NSUInteger size = [self.loadedRewardedInterstitialQueue size];
    if (value >= size) return;
    
    [self AdMob_RewardedInterstitial_Free_Loaded_Instances: size - value];
}

-(double) AdMob_RewardedInterstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:self.rewardedInterstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.loadedRewardedInterstitialQueue maxSize:self.rewardedInterstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = self.rewardedInterstitialAdUnitId;
    
    GADRequest* request = [self buildAdRequest];
    [GADRewardedInterstitialAd loadWithAdUnitID:self.rewardedInterstitialAdUnitId request:request completionHandler:^(GADRewardedInterstitialAd* _Nullable rewardedInterstitialAd, NSError* _Nullable error)
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

        if (![self validateLoadedAdsLimit:self.loadedRewardedInterstitialQueue maxSize:self.rewardedInterstitialMaxLoadedInstances callingMethod:__FUNCTION__]) return;
        
        [self.loadedRewardedInterstitialQueue enqueue: rewardedInterstitialAd];
        
        rewardedInterstitialAd.fullScreenContentDelegate = self;
        
        if(self.triggerOnPaidEvent) {
            const GADRewardedInterstitialAd* rewardedInterstitialRef = rewardedInterstitialAd;
            
            rewardedInterstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = rewardedInterstitialRef.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEventHandler:value adUnitId:rewardedInterstitialRef.adUnitID adType:@"RewardedInterstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:rewardedInterstitialRef.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
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
    
    if (![self validateAdLoaded:self.loadedRewardedInterstitialQueue callingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;
    
    self.rewardedInterstitialAdKeepMe = [self.loadedRewardedInterstitialQueue dequeue];
            
    [self.rewardedInterstitialAdKeepMe presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnReward");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[self.rewardedInterstitialAdKeepMe.adUnitID UTF8String]);
        dsMapAddDouble(dsMapIndex, (char*)"reward_amount", [self.rewardedInterstitialAdKeepMe.adReward.amount doubleValue]);
        dsMapAddString(dsMapIndex, (char*)"reward_type", (char*)[self.rewardedInterstitialAdKeepMe.adReward.type UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    self.isShowingAd = true;
    return 0;
}

-(double) AdMob_RewardedInterstitial_IsLoaded
{
    return [self AdMob_RewardedInterstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedInterstitial_Instances_Count
{
    return [self.loadedRewardedInterstitialQueue size];
}

#pragma mark - App Open Methods

-(void) AdMob_AppOpenAd_Set_UnitId:(NSString*) adUnitId
{
    self.appOpenAdUnitId = adUnitId;
}

-(double) AdMob_AppOpenAd_Enable:(double) orientation
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
        
    if (![self validateAdId:self.appOpenAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    self.isAppOpenAdEnabled = true;
    self.appOpenAdInstance = nil;
    self.appOpenAdOrientation = (orientation == 0) ? UIInterfaceOrientationLandscapeRight : UIInterfaceOrientationPortrait;
    [self loadAppOpenAd];
    
    return 0;
}

-(void) AdMob_AppOpenAd_Disable
{
    self.isAppOpenAdEnabled = false;
    self.appOpenAdInstance = nil;
}

-(double) AdMob_AppOpenAd_IsEnabled
{
    return self.isAppOpenAdEnabled ? 1.0 : 0.0;
}

-(void) onResume
{
    [self showAppOpenAd];
}

-(void) loadAppOpenAd
{
    if (!self.isAppOpenAdEnabled) return;
    
    if (![self validateInitializedWithCallingMethod:"__AdMob_AppOpenAd_Load"]) return;
    
    if (![self validateAdId:self.appOpenAdUnitId callingMethod:"__AdMob_AppOpenAd_Load"]) return;
    
    const NSString* adUnitId = self.appOpenAdUnitId;
    
    self.appOpenAdInstance = nil;
    
    GADRequest* request = [self buildAdRequest];
    
    [GADAppOpenAd loadWithAdUnitID: self.appOpenAdUnitId request:request /*orientation:self.appOpenAdOrientation*/ completionHandler:^(GADAppOpenAd *_Nullable appOpenAdInstance, NSError *_Nullable error) {
        if (error) {
            
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            
            return;
        }
        self.appOpenAdInstance = appOpenAdInstance;
        self.appOpenAdInstance.fullScreenContentDelegate = self;
        self.appOpenAdLoadTime = [NSDate date];
        
        if(self.triggerOnPaidEvent)
            self.appOpenAdInstance.paidEventHandler = ^void(GADAdValue *_Nonnull value)
        {
            GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = self.appOpenAdInstance.responseInfo.loadedAdNetworkResponseInfo;
            [self onPaidEventHandler:value adUnitId:adUnitId adType:@"AppOpen" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:self.appOpenAdInstance.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
        };
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoaded");
        dsMapAddString(dsMapIndex, (char*)"unit_id", (char*)[adUnitId UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) showAppOpenAd
{
    if(!self.isAppOpenAdEnabled)
        return;

    if(![self appOpenAdIsValid:4 callingMethod:"__AdMob_AppOpenAd_Show"]) {
        [self loadAppOpenAd];
        return;
    }
    
    self.appOpenAdInstance.fullScreenContentDelegate = self;
    [self.appOpenAdInstance presentFromRootViewController:g_controller];
}

-(BOOL)appOpenAdIsValid:(int) expirationTimeInHours callingMethod:(const char *)callingMethod {
    if (self.appOpenAdInstance == nil) {
        NSLog(@"%s :: There is no app open ad loaded.", callingMethod);
        return NO;
    }

    NSTimeInterval dateDifference = [[NSDate date] timeIntervalSinceDate: self.appOpenAdLoadTime];
    BOOL expired = dateDifference >= (3600 * expirationTimeInHours);

    if (expired) {
        NSLog(@"%s :: The loaded app open ad expired, reloading...", callingMethod);
        return NO;
    }

    return YES;
}

#pragma mark - Targeting Methods

//https://developers.google.com/admob/ios/targeting#child-directed_setting
-(double) AdMob_Targeting_COPPA:(double) COPPA
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
	
	if(COPPA>0.5)
    [GADMobileAds.sharedInstance.requestConfiguration tagForChildDirectedTreatment];
    
    return 0;
}

//https://developers.google.com/admob/ios/targeting#users_under_the_age_of_consent
-(double) AdMob_Targeting_UnderAge:(double) underAge
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;

	if(underAge>0.5)
    [GADMobileAds.sharedInstance.requestConfiguration tagForUnderAgeOfConsent];

    return 0;
}

//https://developers.google.com/admob/ios/targeting#ad_content_filtering
-(double) AdMob_Targeting_MaxAdContentRating:(double) maxAdContentRating
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;

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

    return 0;
}

#pragma mark - Consent Methods

-(void) AdMob_Consent_RequestInfoUpdate:(double) testing
{
    UMPRequestParameters *parameters = [[UMPRequestParameters alloc] init];
    
    if(testing != 3)
    {
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
    // https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentStatus.html#REQUIRED
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
        if (!canShowAds()) return 3.0; // DECLINED

        return canShowPersonalizedAds() ? /* PERSONALIZED */2.0 : /* NON-PERSONALIZED */1.0;
    }
    return 0; // UNKNOWN
}

-(double) AdMob_Consent_IsFormAvailable
{
    return (UMPConsentInformation.sharedInstance.formStatus == 1) ? 1.0 : 0.0;
}
                                     
-(void) AdMob_Consent_Load
{
    [UMPConsentForm loadWithCompletionHandler:^(UMPConsentForm *form, NSError *loadError)
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
            self.consentForm = form;
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Consent_OnLoaded");
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_Consent_Show
{
    [self.consentForm presentFromViewController:g_controller completionHandler:^(NSError *_Nullable dismissError)
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

#pragma mark - Settings Methods

-(void) AdMob_Settings_SetVolume:(double) value
{
    GADMobileAds.sharedInstance.applicationVolume = value;
}
    
-(void) AdMob_Settings_SetMuted:(double) value
{
    GADMobileAds.sharedInstance.applicationMuted = (value >= 0.5) ? YES : NO;
}

///// INTERNAL ///////////////////////////////////////////////////////////////////////////////////

-(GADRequest*) buildAdRequest
{
    GADRequest *request = [GADRequest request];

    // As per Google's request
    request.requestAgent = [NSString stringWithFormat:@"gmext-admob-%s", extGetVersion((char*)"AdMob")];

    // This is deprectated and shouldn't be used keeping it for the sake of compatibility
    if (self.nonPersonalizedAds)
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

/// VALIDATIONS

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
