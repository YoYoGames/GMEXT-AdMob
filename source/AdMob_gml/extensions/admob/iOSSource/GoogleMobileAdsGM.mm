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

@implementation GoogleMobileAdsGM

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
        
        self.triggerAppOpenAd = NO;
        self.appOpenAdOrientation = UIInterfaceOrientationUnknown;
        self.appOpenAdExpirationTime = 4;
        
        return self;
    }
    return NULL;
}

#pragma mark - Setup Methods

-(double) AdMob_Initialize
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) {
        return ADMOB_ERROR_ILLEGAL_CALL;
    }
    
    // Configure test devices for simulation or real devices
    if (self.isTestDevice) {
#if TARGET_OS_SIMULATOR
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = @[GADSimulatorID];
        NSLog(@"Testing on Simulator: %@", GADSimulatorID);
#else
        NSString *device = [NSString stringWithCString:getDeviceId() encoding:NSUTF8StringEncoding];
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = @[device];
        NSLog(@"Testing on Real Device: %@", device);
#endif
    }
    
    GADMobileAds *ads = [GADMobileAds sharedInstance];
    [ads startWithCompletionHandler:^(GADInitializationStatus *status)
     {
        // Log adapter statuses
        NSDictionary *adapterStatuses = [status adapterStatusesByClassName];
        for (NSString *adapter in adapterStatuses) {
            GADAdapterStatus *adapterStatus = adapterStatuses[adapter];
            NSLog(@"Adapter Name: %@, Description: %@, Latency: %f", adapter, adapterStatus.description, adapterStatus.latency);
        }
            
        // Initialize ad units using extension options
        [self initializeAdUnits];
        
        // Send initialization event
        [self sendAsyncEvent:"AdMob_OnInitialized" eventData:nil];
        
        // Mark the SDK as initialized
        self.isInitialized = YES;
    }];
    
    return ADMOB_OK;
}

-(double) AdMob_SetTestDeviceId
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    self.isTestDevice = YES;
    return ADMOB_OK;
}

-(void) AdMob_Events_OnPaidEvent:(double) enable
{
    self.triggerOnPaidEvent = enable >= 0.5;
}

- (void)initializeAdUnits
{
    NSDictionary *adUnitKeys = @{
        @"iOS_BANNER": @"bannerAdUnitId",
        @"iOS_INTERSTITIAL": @"interstitialAdUnitId",
        @"iOS_REWARDED": @"rewardedUnitId",
        @"iOS_REWARDED_INTERSTITIAL": @"rewardedInterstitialAdUnitId",
        @"iOS_OPENAPPAD": @"appOpenAdUnitId"
    };
    
    for (NSString *key in adUnitKeys) {
        const char *temp = extOptGetString((char*)"AdMob", (char*)[key UTF8String]);
        if (temp && strlen(temp) > 0) {
            NSString *adUnit = [NSString stringWithUTF8String:temp];
            [self setValue:adUnit forKey:adUnitKeys[key]];
        }
    }
}

#pragma mark - Delegate Methods

-(void)bannerView:(nonnull GADBannerView *)bannerView didFailToReceiveAdWithError:(nonnull NSError *)error
{
    // Create a dictionary with error details
    NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
    eventData[@"errorCode"] = @(error.code);
    eventData[@"errorMessage"] = [error.localizedDescription copy];
    
    // Trigger the event using sendAsyncEvent
    [self sendAsyncEvent:"AdMob_Banner_OnLoadFailed" eventData:eventData];
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView
{
    // Create a dictionary with the banner ad details
    NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
    eventData[@"unit_id"] = bannerView.adUnitID;
    
    // Trigger the event using sendAsyncEvent
    [self sendAsyncEvent:"AdMob_Banner_OnLoaded" eventData:eventData];
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
            [self AdMob_AppOpenAd_Load];
        }
    }
    
    if (eventType && adUnitID) {
        // Create a dictionary with the event data
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = adUnitID;
        eventData[@"errorCode"] = @(error.code);
        eventData[@"errorMessage"] = [error.localizedDescription copy];
        
        // Trigger the event using sendAsyncEvent
        [self sendAsyncEvent:[eventType UTF8String] eventData:eventData];
    }
}

-(void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{
    NSString *eventType = nil;
    NSString *adUnitID = nil;
    
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]]) {
        eventType = @"AdMob_Interstitial_OnFullyShown";
        adUnitID = [(GADInterstitialAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]]) {
        eventType = @"AdMob_RewardedVideo_OnFullyShown";
        adUnitID = [(GADRewardedAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]]) {
        eventType = @"AdMob_RewardedInterstitial_OnFullyShown";
        adUnitID = [(GADRewardedInterstitialAd *)presentingAd adUnitID];
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]]) {
        eventType = @"AdMob_AppOpenAd_OnFullyShown";
        adUnitID = [(GADAppOpenAd *)presentingAd adUnitID];
        
        // If AppOpenAd is being automatically managed
        if (self.triggerAppOpenAd) {
            // Reload the App Open Ad after failure
            [self AdMob_AppOpenAd_Load];
        }
    }
    
    // If eventType and adUnitID are set, send the event
    if (eventType && adUnitID) {
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = adUnitID; // Add the unit ID
        [self sendAsyncEvent:[eventType UTF8String] eventData:eventData]; // Trigger the event using sendAsyncEvent
    }
}

-(void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)presentingAd
{    
    NSString *eventType = nil;
    NSString *adUnitID = nil;
    
    if ([presentingAd isMemberOfClass:[GADInterstitialAd class]]) {
        eventType = @"AdMob_Interstitial_OnDismissed";
        adUnitID = [(GADInterstitialAd *)presentingAd adUnitID];
        
        // Clean up the delegate and event handler for the interstitial ad
        [self cleanAd:(GADInterstitialAd *)presentingAd withCleaner:^(id ad){
            [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
        }];
        self.interstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedAd class]]) {
        eventType = @"AdMob_RewardedVideo_OnDismissed";
        adUnitID = [(GADRewardedAd *)presentingAd adUnitID];
        
        // Clean up the delegate and event handler for the rewarded ad
        [self cleanAd:(GADRewardedAd *)presentingAd withCleaner:^(id ad){
            [self cleanUpRewardedAd:(GADRewardedAd *)ad];
        }];
        self.rewardedAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADRewardedInterstitialAd class]]) {
        eventType = @"AdMob_RewardedInterstitial_OnDismissed";
        adUnitID = [(GADRewardedInterstitialAd *)presentingAd adUnitID];
        
        // Clean up the delegate and event handler for the rewarded interstitial ad
        [self cleanAd:(GADRewardedInterstitialAd *)presentingAd withCleaner:^(id ad){
            [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
        }];
        self.rewardedInterstitialAd = nil;
    }
    else if ([presentingAd isMemberOfClass:[GADAppOpenAd class]]) {
        eventType = @"AdMob_AppOpenAd_OnDismissed";
        adUnitID = self.appOpenAdUnitId;
        
        // Clean up the delegate and event handler for the app open ad
        [self cleanAd:(GADAppOpenAd *)presentingAd withCleaner:^(id ad){
            [self cleanUpAppOpenAd:(GADAppOpenAd *)ad];
        }];
        self.appOpenAd = nil;

        // If AppOpenAd is being automatically managed
        if (self.triggerAppOpenAd) {
            // Load the App Open Ad again
            [self AdMob_AppOpenAd_Load];
        }
    }
    
    // If eventType and adUnitID are set, send the event
    if (eventType && adUnitID) {
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = adUnitID; // Add the unit ID
        [self sendAsyncEvent:[eventType UTF8String] eventData:eventData];
    }
}

#pragma mark - Banner Methods

-(void) AdMob_Banner_Set_AdUnit:(NSString*) adUnitId
{
    self.bannerAdUnitId = adUnitId;
}

-(double) AdMob_Banner_Create:(double)size bottom:(double)bottom
{
    // Default alignment is center
    return [self createBannerAdViewWithSize:size
                                     bottom:bottom
                                  alignment:ADMOB_BANNER_ALIGNMENT_CENTER
                              callingMethod:__FUNCTION__];
}

- (double)AdMob_Banner_Create_Ext:(double)size bottom:(double)bottom alignment:(double)alignment
{
    return [self createBannerAdViewWithSize:size
                                     bottom:bottom
                                  alignment:(int)alignment
                              callingMethod:__FUNCTION__];
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

-(double)AdMob_Banner_Move:(double)bottom
{
    
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if (self.bannerView != nil) {
        // Remove the banner from its current superview
        [self.bannerView removeFromSuperview];
        
        // Determine the new position based on the 'bottom' parameter
        NSLayoutAttribute newPosition = (bottom > 0.5) ? NSLayoutAttributeBottom : NSLayoutAttributeTop;
        
        // Re-add the banner with the stored alignment
        [self addBannerView:self.bannerView
                 toPosition:newPosition
                  alignment:self.currentBannerAlignment];
    }
    
    return ADMOB_OK;
}

-(double) AdMob_Banner_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = NO;
    }
    return ADMOB_OK;
}

-(double) AdMob_Banner_Hide
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = YES;
    }
    return ADMOB_OK;
}

-(double) AdMob_Banner_Remove
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateActiveBannerAdWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
    [self deleteBannerAdView];
    
    return ADMOB_OK;
}

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
        __weak GoogleMobileAdsGM *weakSelf = self;
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

-(void) AdMob_Interstitial_Set_AdUnit:(NSString*) adUnitId
{
    self.interstitialAdUnitId = adUnitId;
}

-(void) Admob_Interstitial_Free_Loaded_Instances:(double) count
{
    [self freeLoadedInstances:self.interstitialAdQueue count:count withCleaner:^(id ad){
        [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
    }];
}

-(void) Admob_Interstitial_Max_Instances:(double) value
{
    self.interstitialAdQueueCapacity = value;
    [self trimLoadedAdsQueue:self.interstitialAdQueue maxSize:value withCleaner:^(id ad){
        [self cleanUpInterstitialAd:(GADInterstitialAd *)ad];
    }];
}

-(double) AdMob_Interstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdId:self.interstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.interstitialAdQueue maxSize:self.interstitialAdQueueCapacity callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = self.interstitialAdUnitId;
    
    GADRequest* request = [self buildAdRequest];
    
    // Load interstitial ad
    [GADInterstitialAd loadWithAdUnitID:self.interstitialAdUnitId request:request completionHandler:^(GADInterstitialAd *interstitialAd, NSError *error)
     {
        if (error)
        {
            // Create a dictionary with error details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = [adUnitId copy]; // Ad unit ID
            eventData[@"errorMessage"] = [error.localizedDescription copy]; // Error message
            eventData[@"errorCode"] = @(error.code); // Error code
            
            // Use sendAsyncEvent for failure event
            [self sendAsyncEvent:"AdMob_Interstitial_OnLoadFailed" eventData:eventData];
            return;
        }
        
        // Validate the loaded ads limit
        if (![self validateLoadedAdsLimit:self.interstitialAdQueue maxSize:self.interstitialAdQueueCapacity callingMethod:__FUNCTION__]) return;
        
        // Enqueue the loaded interstitial ad
        [self.interstitialAdQueue enqueue:interstitialAd];
        
        // Setup paid event handler if triggerOnPaidEvent is enabled
        if (self.triggerOnPaidEvent) {
            __weak GoogleMobileAdsGM *weakSelf = self;
            __weak GADInterstitialAd *weakInterstitialAd = interstitialAd;
            interstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = weakInterstitialAd.responseInfo.loadedAdNetworkResponseInfo;
                [weakSelf onPaidEventHandler:value adUnitId:weakInterstitialAd.adUnitID adType:@"Interstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:weakInterstitialAd.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
            };
        }
        
        // Create a dictionary for success event
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = [adUnitId copy]; // Ad unit ID
        
        // Use sendAsyncEvent for success event
        [self sendAsyncEvent:"AdMob_Interstitial_OnLoaded" eventData:eventData];
    }];
    
    return ADMOB_OK;
}

-(double) AdMob_Interstitial_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    // Dequeue the interstitial ad from the queue
    GADInterstitialAd *interstitialAd = [self.interstitialAdQueue dequeue];
    if (interstitialAd == nil) return ADMOB_ERROR_NO_ADS_LOADED;
    
    // Set the delegate before presenting the ad
    interstitialAd.fullScreenContentDelegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        // Present the interstitial ad
        [interstitialAd presentFromRootViewController:g_controller];
    });
    
    // Retain the ad to prevent it from being deallocated
    self.interstitialAd = interstitialAd;
    // Mark that an ad is currently being shown
    self.isShowingAd = YES;
    
    return ADMOB_OK;
}

-(double) AdMob_Interstitial_IsLoaded
{
    return [self AdMob_Interstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_Interstitial_Instances_Count
{
    return [self.interstitialAdQueue size];
}

#pragma mark - Server Side Verification

-(double) AdMob_ServerSideVerification_Set:(NSString*)userId customData:(NSString*)customData
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    self.serverSideVerificationUserId = userId;
    self.serverSideVerificationCustomData = customData;
    
    return ADMOB_OK;
}

-(double) AdMob_ServerSideVerification_Clear
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    self.serverSideVerificationUserId = nil;
    self.serverSideVerificationCustomData = nil;
    
    return ADMOB_OK;
}

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

-(void) AdMob_RewardedVideo_Set_AdUnit:(NSString*) adUnitId
{
    self.rewardedUnitId = adUnitId;
}

-(void) AdMob_RewardedVideo_Free_Loaded_Instances:(double) count
{
    [self freeLoadedInstances:self.rewardedAdQueue count:count withCleaner:^(id ad){
        [self cleanUpRewardedAd:(GADRewardedAd *)ad];
    }];
}

-(void) AdMob_RewardedVideo_Max_Instances:(double) value
{
    self.rewardedAdQueueCapacity = value;
    [self trimLoadedAdsQueue:self.rewardedAdQueue maxSize:value withCleaner:^(id ad){
        [self cleanUpRewardedAd:(GADRewardedAd *)ad];
    }];
}

-(double) AdMob_RewardedVideo_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdId:self.rewardedUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.rewardedAdQueue maxSize:self.rewardedAdQueueCapacity callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = self.rewardedUnitId;
    
    GADRequest* request = [self buildAdRequest];
    
    // Retrieve UserId and CustomData
    NSString *userId = self.serverSideVerificationUserId;
    NSString *customData = self.serverSideVerificationCustomData;
    
    // Configure the request with UserId and CustomData using the helper method
    [self configureServerSideVerification:request withUserId:userId customData:customData];
    
    // Loading the rewarded video ad
    [GADRewardedAd loadWithAdUnitID:self.rewardedUnitId request:request completionHandler:^(GADRewardedAd *rewardedAd, NSError *error)
     {
        if (error)
        {
            // Create a dictionary with error details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = [adUnitId copy]; // Ad unit ID
            eventData[@"errorCode"] = @(error.code); // Error code
            eventData[@"errorMessage"] = [error.localizedDescription copy]; // Error message
            
            // Trigger failure event using sendAsyncEvent
            [self sendAsyncEvent:"AdMob_RewardedVideo_OnLoadFailed" eventData:eventData];
            return;
        }
        
        // Validate the loaded ads limit
        if (![self validateLoadedAdsLimit:self.rewardedAdQueue maxSize:self.rewardedAdQueueCapacity callingMethod:__FUNCTION__]) return;
        
        // Enqueue the loaded rewarded video ad
        [self.rewardedAdQueue enqueue:rewardedAd];
        
        // Setup the paid event handler if triggerOnPaidEvent is enabled
        if (self.triggerOnPaidEvent) {
            __weak GoogleMobileAdsGM *weakSelf = self;
            __weak GADRewardedAd *weakRewardedAd = rewardedAd;
            rewardedAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = weakRewardedAd.responseInfo.loadedAdNetworkResponseInfo;
                [weakSelf onPaidEventHandler:value adUnitId:weakRewardedAd.adUnitID adType:@"Rewarded" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:weakRewardedAd.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
            };
        }
        
        // Create a dictionary for the success event
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = [adUnitId copy]; // Ad unit ID
        
        // Trigger success event using sendAsyncEvent
        [self sendAsyncEvent:"AdMob_RewardedVideo_OnLoaded" eventData:eventData];
    }];
    
    return ADMOB_OK;
}

-(double) AdMob_RewardedVideo_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    // Dequeue the rewarded video ad
    GADRewardedAd *rewardedAd = [self.rewardedAdQueue dequeue];
    if (rewardedAd == nil) return ADMOB_ERROR_NO_ADS_LOADED;
    
    // Set the delegate before presenting the ad
    rewardedAd.fullScreenContentDelegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        // Present the rewarded video ad
        [rewardedAd presentFromRootViewController:g_controller userDidEarnRewardHandler:^ {
            // Log to verify the handler is triggered
            
            // Create a dictionary with the reward details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = self.rewardedAd.adUnitID; // Ad unit ID
            eventData[@"reward_amount"] = @(self.rewardedAd.adReward.amount.doubleValue); // Reward amount as NSNumber
            eventData[@"reward_type"] = self.rewardedAd.adReward.type; // Reward type as NSString
            
            // Use the sendAsyncEvent function to trigger the event
            [self sendAsyncEvent:"AdMob_RewardedVideo_OnReward" eventData:eventData];
        }];
    });
    
    // Retain the ad to prevent it from being deallocated
    self.rewardedAd = rewardedAd;
    // Mark that an ad is currently being shown
    self.isShowingAd = YES;
    
    return ADMOB_OK;
}

-(double) AdMob_RewardedVideo_IsLoaded
{
    return [self AdMob_RewardedVideo_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedVideo_Instances_Count
{
    return [self.rewardedAdQueue size];
}

#pragma mark - Rewarded Interstitial Methods

-(void) AdMob_RewardedInterstitial_Set_AdUnit:(NSString*) adUnitId
{
    self.rewardedInterstitialAdUnitId = adUnitId;
}

-(void) AdMob_RewardedInterstitial_Free_Loaded_Instances:(double) count
{
    [self freeLoadedInstances:self.rewardedInterstitialAdQueue count:count withCleaner:^(id ad){
        [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
    }];
}

-(void) AdMob_RewardedInterstitial_Max_Instances:(double) value
{
    self.rewardedAdInterstitialQueueCapacity = value;
    [self trimLoadedAdsQueue:self.rewardedInterstitialAdQueue maxSize:value withCleaner:^(id ad){
        [self cleanUpRewardedInterstitialAd:(GADRewardedInterstitialAd *)ad];
    }];
}

-(double) AdMob_RewardedInterstitial_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdId:self.rewardedInterstitialAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if (![self validateLoadedAdsLimit:self.rewardedInterstitialAdQueue maxSize:self.rewardedAdInterstitialQueueCapacity callingMethod:__FUNCTION__]) return ADMOB_ERROR_AD_LIMIT_REACHED;
    
    const NSString* adUnitId = self.rewardedInterstitialAdUnitId;
    
    GADRequest* request = [self buildAdRequest];
    
    // Retrieve UserId and CustomData
    NSString *userId = self.serverSideVerificationUserId;
    NSString *customData = self.serverSideVerificationCustomData;
    
    // Configure the request with UserId and CustomData using the helper method
    [self configureServerSideVerification:request withUserId:userId customData:customData];
    
    // Loading the rewarded interstitial ad
    [GADRewardedInterstitialAd loadWithAdUnitID:self.rewardedInterstitialAdUnitId request:request completionHandler:^(GADRewardedInterstitialAd* _Nullable rewardedInterstitialAd, NSError* _Nullable error)
     {
        if (error)
        {
            // Create a dictionary with error details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = [adUnitId copy]; // Add the unit ID
            eventData[@"errorCode"] = @(error.code); // Add the error code
            eventData[@"errorMessage"] = [error.localizedDescription copy]; // Add the error message
            
            // Use the sendAsyncEvent function for failure event
            [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnLoadFailed" eventData:eventData];
            return;
        }
        
        // Validate if loaded ads limit is not exceeded
        if (![self validateLoadedAdsLimit:self.rewardedInterstitialAdQueue maxSize:self.rewardedAdInterstitialQueueCapacity callingMethod:__FUNCTION__]) return;
        
        // Enqueue the successfully loaded ad
        [self.rewardedInterstitialAdQueue enqueue:rewardedInterstitialAd];
        
        // Setup paid event handler if enabled
        if (self.triggerOnPaidEvent) {
            __weak GoogleMobileAdsGM *weakSelf = self;
            __weak GADRewardedInterstitialAd *weakRewardedInterstitialAd = rewardedInterstitialAd;
            rewardedInterstitialAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = weakRewardedInterstitialAd.responseInfo.loadedAdNetworkResponseInfo;
                [weakSelf onPaidEventHandler:value adUnitId:weakRewardedInterstitialAd.adUnitID adType:@"RewardedInterstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:weakRewardedInterstitialAd.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
            };
        }
        
        // Create a dictionary for success event
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = [adUnitId copy]; // Add the unit ID
        
        // Use the sendAsyncEvent function for success event
        [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnLoaded" eventData:eventData];
    }];
    
    return ADMOB_OK;
}

-(double) AdMob_RewardedInterstitial_Show
{
    // Validate that the SDK is initialized
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    // Dequeue the next loaded rewarded interstitial ad
    GADRewardedInterstitialAd *rewardedInterstitialAd = [self.rewardedInterstitialAdQueue dequeue];
    if (rewardedInterstitialAd == nil) return ADMOB_ERROR_NO_ADS_LOADED;
    
    // Set the delegate before presenting the ad
    rewardedInterstitialAd.fullScreenContentDelegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        // Present the rewarded interstitial ad
        [rewardedInterstitialAd presentFromRootViewController:g_controller userDidEarnRewardHandler:^
         {
            // Create a dictionary with reward details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = rewardedInterstitialAd.adUnitID;
            eventData[@"reward_amount"] = rewardedInterstitialAd.adReward.amount;
            eventData[@"reward_type"] = rewardedInterstitialAd.adReward.type;
            
            // Send the event
            [self sendAsyncEvent:"AdMob_RewardedInterstitial_OnReward" eventData:eventData];
        }];
    });
    
    // Retain the ad to prevent it from being deallocated
    self.rewardedInterstitialAd = rewardedInterstitialAd;
    // Mark that an ad is currently being shown
    self.isShowingAd = YES;
    
    return ADMOB_OK;
}

-(double) AdMob_RewardedInterstitial_IsLoaded
{
    return [self AdMob_RewardedInterstitial_Instances_Count] > 0 ? 1.0 : 0.0;
}

-(double) AdMob_RewardedInterstitial_Instances_Count
{
    return [self.rewardedInterstitialAdQueue size];
}

#pragma mark - App Open Methods

-(void) AdMob_AppOpenAd_Set_AdUnit:(NSString*) adUnitId
{
    self.appOpenAdUnitId = adUnitId;
}

-(double) AdMob_AppOpenAd_Enable:(double) orientation
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdId:self.appOpenAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    self.triggerAppOpenAd = YES;
    
    if(![self appOpenAdIsValid:__FUNCTION__]) {
        [self AdMob_AppOpenAd_Load];
    }
    
    return ADMOB_OK;
}

-(void) AdMob_AppOpenAd_Disable
{
    self.triggerAppOpenAd = NO;
}

-(double) AdMob_AppOpenAd_IsEnabled
{
    return self.triggerAppOpenAd ? 1.0 : 0.0;
}

-(double) AdMob_AppOpenAd_IsLoaded
{
    return [self appOpenAdIsValid:__FUNCTION__];
}

-(double) AdMob_AppOpenAd_Load
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self validateAdId:self.appOpenAdUnitId callingMethod:__FUNCTION__]) return ADMOB_ERROR_INVALID_AD_ID;
    
    if ([self appOpenAdIsValid:__FUNCTION__]) return ADMOB_OK;

    NSString* adUnitId = self.appOpenAdUnitId;
    
    self.appOpenAd = nil;
    
    GADRequest* request = [self buildAdRequest];
    
    // Store current orientation when loading the ad is necessary for validation
    self.appOpenAdOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    [GADAppOpenAd loadWithAdUnitID: self.appOpenAdUnitId request:request completionHandler:^(GADAppOpenAd *_Nullable appOpenAd, NSError *_Nullable error) {
        if (error) {
            // Create a dictionary with error details
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"unit_id"] = [adUnitId copy]; // Add the unit ID
            eventData[@"errorCode"] = @(error.code); // Add error code
            eventData[@"errorMessage"] = [error.localizedDescription copy]; // Add error message
            
            // Use the sendAsyncEvent function for failure
            [self sendAsyncEvent:"AdMob_AppOpenAd_OnLoadFailed" eventData:eventData];
            return;
        }
        
        self.appOpenAd = appOpenAd;
        self.appOpenAdLoadTime = [NSDate date];
        
        // Paid event handler setup
        if (self.triggerOnPaidEvent) {
            __weak GoogleMobileAdsGM *weakSelf = self;
            self.appOpenAd.paidEventHandler = ^void(GADAdValue *_Nonnull value) {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = weakSelf.appOpenAd.responseInfo.loadedAdNetworkResponseInfo;
                [weakSelf onPaidEventHandler:value adUnitId:adUnitId adType:@"AppOpen" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:weakSelf.appOpenAd.responseInfo.adNetworkInfoArray[0].adNetworkClassName];
            };
        }
        
        // Create a dictionary for success event
        NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
        eventData[@"unit_id"] = [adUnitId copy]; // Add the unit ID
        
        // Use the sendAsyncEvent function for success
        [self sendAsyncEvent:"AdMob_AppOpenAd_OnLoaded" eventData:eventData];
    }];
    
    return ADMOB_OK;
}

-(double) AdMob_AppOpenAd_Show
{
    if (![self validateInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_NOT_INITIALIZED;
    
    if (![self appOpenAdIsValid:__FUNCTION__]) return ADMOB_ERROR_NO_ADS_LOADED;

    self.appOpenAd.fullScreenContentDelegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        // Present the app open ad
        [self.appOpenAd presentFromRootViewController:g_controller];
    });
    
    // Mark that an ad is currently being shown
    self.isShowingAd = YES;
    
    return ADMOB_OK;
}

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
-(double) AdMob_Targeting_COPPA:(double) COPPA
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    if(COPPA>0.5)
        [GADMobileAds.sharedInstance.requestConfiguration tagForChildDirectedTreatment];
    
    return ADMOB_OK;
}

//https://developers.google.com/admob/ios/targeting#users_under_the_age_of_consent
-(double) AdMob_Targeting_UnderAge:(double) underAge
{
    if (![self validateNotInitializedWithCallingMethod:__FUNCTION__]) return ADMOB_ERROR_ILLEGAL_CALL;
    
    if(underAge>0.5)
        [GADMobileAds.sharedInstance.requestConfiguration tagForUnderAgeOfConsent];
    
    return ADMOB_OK;
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
    
    return ADMOB_OK;
}

#pragma mark - Consent Management

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
        if (error)
        {
            // Create a dictionary with error information
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"errorCode"] = @(error.code); // Add error code
            eventData[@"errorMessage"] = [error.localizedDescription copy]; // Add error message
            
            // Use the sendAsyncEvent function
            [self sendAsyncEvent:"AdMob_Consent_OnRequestInfoUpdateFailed" eventData:eventData];
        }
        else
        {
            // Send event without extra data
            [self sendAsyncEvent:"AdMob_Consent_OnRequestInfoUpdated" eventData:nil];
        }
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

- (void)AdMob_Consent_Load
{
    [UMPConsentForm loadWithCompletionHandler:^(UMPConsentForm *form, NSError *loadError) {
        if (loadError)
        {
            // Create event data dictionary with error details
            NSDictionary *eventData = @{
                @"errorCode": @(loadError.code), // Box NSInteger into NSNumber
                @"errorMessage": loadError.localizedDescription // NSString directly
            };
            
            // Send failure event
            [self sendAsyncEvent:"AdMob_Consent_OnLoadFailed" eventData:eventData];
        }
        else
        {
            // Assign the loaded consent form
            self.consentForm = form;
            
            // Send success event with no additional data
            [self sendAsyncEvent:"AdMob_Consent_OnLoaded" eventData:nil];
        }
    }];
}

-(void) AdMob_Consent_Show
{
    [self.consentForm presentFromViewController:g_controller completionHandler:^(NSError *_Nullable dismissError)
     {
        if (UMPConsentInformation.sharedInstance.consentStatus == UMPConsentStatusObtained && !dismissError)
        {
            [self sendAsyncEvent:"AdMob_Consent_OnShown" eventData:nil];
        }
        else
        {
            NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
            eventData[@"errorMessage"] = @(dismissError.code);
            eventData[@"reward_type"] = dismissError.localizedDescription;
            [self sendAsyncEvent:"AdMob_Consent_OnShown" eventData:eventData];
        }
    }];
}

-(void) AdMob_Consent_Reset
{
    [UMPConsentInformation.sharedInstance reset];
}

-(void) AdMob_Consent_Set_RDP:(double)enabled
{
    self.isRdpEnabled = enabled > 0.5;
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
    if (input == nil) return NO;
    if (index <= 0 || index > input.length) return NO;
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

-(void) AdMob_Settings_SetVolume:(double) value
{
    GADMobileAds.sharedInstance.applicationVolume = value;
}

-(void) AdMob_Settings_SetMuted:(double) value
{
    GADMobileAds.sharedInstance.applicationMuted = (value >= 0.5) ? YES : NO;
}

#pragma mark - Activity Lifecycle Methods

-(void) onResume
{
    if (self.triggerAppOpenAd) {
        if(![self appOpenAdIsValid:"onResume"]) {
            [self AdMob_AppOpenAd_Load];
            self.isShowingAd = NO;
            return;
        }
        
        if (!self.isShowingAd) {
            [self AdMob_AppOpenAd_Show];
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

- (void)sendAsyncEvent:(const char *)eventType eventData:(NSDictionary *)eventData {
    dispatch_async(dispatch_get_main_queue(), ^{
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char *)"type", (char *)eventType);

        for (NSString *key in eventData) {
            id value = eventData[key];
            const char *cKey = [key UTF8String];

            // Check if value is NSDictionary or NSArray and serialize to JSON string
            if ([value isKindOfClass:[NSDictionary class]] || [value isKindOfClass:[NSArray class]]) {
                NSError *error = nil;
                NSData *jsonData = [NSJSONSerialization dataWithJSONObject:value
                                                                   options:0 // NSJSONWritingPrettyPrinted can be used if formatting is desired
                                                                     error:&error];
                NSString *jsonString;
                if (error == nil && jsonData) {
                    jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                } else {
                    NSLog(@"FirebaseUtils: JSON serialization failed for key '%@' with error: %@", key, error.localizedDescription);
                    jsonString = [value isKindOfClass:[NSDictionary class]] ? @"{}" : @"[]"; // Default to empty JSON container on failure
                }
                dsMapAddString(dsMapIndex, (char *)cKey, (char *)[jsonString UTF8String]);
            } else if ([value isKindOfClass:[NSString class]]) {
                dsMapAddString(dsMapIndex, (char *)cKey, (char *)[value UTF8String]);
            } else if ([value isKindOfClass:[NSNumber class]]) {
                NSNumber *numberValue = (NSNumber *)value;
                const char *type = [numberValue objCType];

                // Handle BOOL
                if (strcmp(type, @encode(BOOL)) == 0 || strcmp(type, @encode(bool)) == 0 || strcmp(type, @encode(char)) == 0) {
                    int boolValue = [numberValue boolValue] ? 1 : 0;
                    dsMapAddInt(dsMapIndex, (char *)cKey, boolValue);
                }
                // Handle integer types within int range
                else if (strcmp(type, @encode(int)) == 0 ||
                         strcmp(type, @encode(short)) == 0 ||
                         strcmp(type, @encode(unsigned int)) == 0 ||
                         strcmp(type, @encode(unsigned short)) == 0) {

                    int intValue = [numberValue intValue];
                    dsMapAddInt(dsMapIndex, (char *)cKey, intValue);
                }
                // Handle floating-point numbers
                else if (strcmp(type, @encode(float)) == 0 ||
                         strcmp(type, @encode(double)) == 0) {

                    double doubleValue = [numberValue doubleValue];
                    dsMapAddDouble(dsMapIndex, (char *)cKey, doubleValue);
                }
                // Handle signed long and long long
                else if (strcmp(type, @encode(long)) == 0 ||
                         strcmp(type, @encode(long long)) == 0) {

                    long long longValue = [numberValue longLongValue];
                    if (longValue >= INT_MIN && longValue <= INT_MAX) {
                        dsMapAddInt(dsMapIndex, (char *)cKey, (int)longValue);
                    } else if (llabs(longValue) <= (1LL << 53)) {
                        dsMapAddDouble(dsMapIndex, (char *)cKey, (double)longValue);
                    } else {
                        // Represent as special string format
                        NSString *formattedString = [NSString stringWithFormat:@"@i64@%llx$i64$", longValue];
                        dsMapAddString(dsMapIndex, (char *)cKey, (char *)[formattedString UTF8String]);
                    }
                }
                // Handle unsigned long and unsigned long long
                else if (strcmp(type, @encode(unsigned long)) == 0 ||
                         strcmp(type, @encode(unsigned long long)) == 0) {

                    unsigned long long ulongValue = [numberValue unsignedLongLongValue];
                    if (ulongValue <= (unsigned long long)INT_MAX) {
                        dsMapAddInt(dsMapIndex, (char *)cKey, (int)ulongValue);
                    } else if (ulongValue <= (1ULL << 53)) {
                        dsMapAddDouble(dsMapIndex, (char *)cKey, (double)ulongValue);
                    } else {
                        // Represent as special string format
                        NSString *formattedString = [NSString stringWithFormat:@"@i64@%llx$i64$", ulongValue];
                        dsMapAddString(dsMapIndex, (char *)cKey, (char *)[formattedString UTF8String]);
                    }
                } else {
                    // For other numeric types, default to adding as double
                    double doubleValue = [numberValue doubleValue];
                    dsMapAddDouble(dsMapIndex, (char *)cKey, doubleValue);
                }
            } else {
                // For other types, convert to string
                NSString *stringValue = [value description];
                dsMapAddString(dsMapIndex, (char *)cKey, (char *)[stringValue UTF8String]);
            }
        }
        createSocialAsyncEventWithDSMap(dsMapIndex);
    });
}

-(void)onPaidEventHandler:(GADAdValue*) value adUnitId:(NSString*)adUnitId adType:(NSString*)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
    // Create a dictionary with all the relevant event data
    NSMutableDictionary *eventData = [NSMutableDictionary dictionary];
    eventData[@"mediation_adapter_class_name"] = mediationAdapterClassName;
    eventData[@"unit_id"] = adUnitId;
    eventData[@"ad_type"] = adType;
    eventData[@"micros"] = @(value.value.doubleValue * 1000000.0); // Convert micros
    eventData[@"currency_code"] = value.currencyCode;
    eventData[@"precision"] = @(value.precision);
    eventData[@"ad_source_name"] = loadedAdNetworkResponseInfo.adSourceName;
    eventData[@"ad_source_id"] = loadedAdNetworkResponseInfo.adSourceID;
    eventData[@"ad_source_instance_name"] = loadedAdNetworkResponseInfo.adSourceInstanceName;
    eventData[@"ad_source_instance_id"] = loadedAdNetworkResponseInfo.adSourceInstanceID;
    
    // Trigger the event using sendAsyncEvent
    [self sendAsyncEvent:"AdMob_OnPaidEvent" eventData:eventData];
}

- (GADRequest*) buildAdRequest
{
    GADRequest *request = [GADRequest request];
    
    // Set the request agent as per Google's requirement
    request.requestAgent = [NSString stringWithFormat:@"gmext-admob-%s", extGetVersion((char*)"AdMob")];
    
    // Initialize a mutable dictionary to hold additional parameters
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
