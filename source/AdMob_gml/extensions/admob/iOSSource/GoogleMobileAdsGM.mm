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

@implementation GoogleMobileAdsGM

-(id)init {
    if ( self = [super init] ) {
        
        testingAds = false;
        NPA = false;
        
        self.BannerAdID = @"";
        self.interstitialAdID = @"";
        self.rewardAd_ID = @"";
        self.rewardInterstitialAd_ID = @"";
        self.appOpenAdID = @"";
        
        Interstitial_Max_Instances = 1;
        RewardedVideo_Max_Instances = 1;
        RewardedInterstitial_Max_Instances = 1;
        
        Paid_Event = false;
        
        AppOpenAd_Enable = false;
        AppOpenAd_orientation = 0;
        
        self.loads = [[NSMutableArray alloc] init];

        return self;
    }
    return NULL;
}

/////////////////////////////////////////////////////GoogleMobileAds

-(void) AdMob_Initialize
{
    if (testingAds) {
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
        self.BannerAdID = [NSString stringWithUTF8String: extOptGetString((char*)"AdMob", (char*)"iOS_BANNER")];
        self.interstitialAdID = [NSString stringWithUTF8String: extOptGetString((char*)"AdMob", (char*)"iOS_INTERSTITIAL")];
        self.rewardAd_ID = [NSString stringWithUTF8String: extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED")];
        self.rewardInterstitialAd_ID = [NSString stringWithUTF8String: extOptGetString((char*)"AdMob", (char*)"iOS_REWARDED_INTERSTITIAL")];
        self.appOpenAdID = [NSString stringWithUTF8String: extOptGetString((char*)"AdMob", (char*)"iOS_OPENAPPAD")];
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_OnInitialized");
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_SetTestDeviceId
{
    testingAds = true;
}

- (void)bannerView:(nonnull GADBannerView *)bannerView
didFailToReceiveAdWithError:(nonnull NSError *)error{
        
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoadFailed");
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

-(void)bannerViewDidReceiveAd:(nonnull GADBannerView *)bannerView{
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Banner_OnLoaded");
    dsMapAddString(dsMapIndex, (char*)"id", (char*)[bannerView.adUnitID UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad failed to present full screen content.
- (void)ad:(nonnull id<GADFullScreenPresentingAd>)ad didFailToPresentFullScreenContentWithError:(nonnull NSError *)error
{
    showing_ad = false;

    NSLog(@"Ad did fail to present full screen content.");
    int dsMapIndex = dsMapCreate();

    if ([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if ([ad isMemberOfClass:[GADRewardedAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if ([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnShowFailed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        // dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnShowFailed");
        [self AdMob_AppOpenAd_Load:AppOpenAd_orientation];
    }
    
    dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
    dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad presented full screen content.
- (void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad
{
    NSLog(@"Ad did present full screen content.");
    int dsMapIndex = dsMapCreate();

    if([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnFullyShown");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        // int dsMapIndex = dsMapCreate();
        // dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnFullyShown");
        [self AdMob_AppOpenAd_Load:AppOpenAd_orientation];
    }

    createSocialAsyncEventWithDSMap(dsMapIndex);
}

/// Tells the delegate that the ad dismissed full screen content.
- (void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad
{
    showing_ad = false;

    NSLog(@"Ad did dismiss full screen content.");
    int dsMapIndex = dsMapCreate();

    if([ad isMemberOfClass:[GADInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADRewardedInterstitialAd class]])
    {
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnDismissed");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[((GADRewardedInterstitialAd*)ad).adUnitID UTF8String]);

    }
    else if([ad isMemberOfClass:[GADAppOpenAd class]])
    {
        // dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnReward");
        [self AdMob_AppOpenAd_Load:AppOpenAd_orientation];
    }

    createSocialAsyncEventWithDSMap(dsMapIndex);
}

///// BANNER //////////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Banner_Target:(NSString*) bannerID
{
    self.BannerAdID = bannerID;
}

-(void) AdMob_Banner_Create:(double) size bottom: (double)bottom
{
    if ([self.BannerAdID isEqualToString:@""])
        return;
        
    if(self.bannerView != nil)
    {
        [self.bannerView removeFromSuperview];
        self.bannerView.delegate = nil;
        //[self.bannerView release];
        self.bannerView = nil;
    }
    
    self.bannerView = [[GADBannerView alloc]
                       initWithAdSize:GADAdSizeBanner];
    
    self.bannerView.delegate = self;
    
    if(Paid_Event)
    self.bannerView.paidEventHandler = ^void(GADAdValue *_Nonnull value)
    {
        GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = self.bannerView.responseInfo.loadedAdNetworkResponseInfo;
        [self onPaidEvent_Handler:value adUnitId:self.bannerView.adUnitID adType:@"Banner" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:self.bannerView.responseInfo.adNetworkClassName];
    };
    
    self.bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [g_glView addSubview:self.bannerView];
    
    GADAdSize bannerSize;
    switch((int)size)
    {
        case 0: {bannerSize = GADAdSizeBanner; break;}
        case 1: {bannerSize = GADAdSizeLargeBanner; break;}
        case 2: {bannerSize = GADAdSizeMediumRectangle; break;}
        case 3: {bannerSize = GADAdSizeFullBanner; break;}
        case 4: {bannerSize = GADAdSizeLeaderboard; break;}
        case 5: {
            
            UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
            if(orientation == UIInterfaceOrientationPortrait or orientation == 0)
            {
                NSLog(@"Orientation: isPortail");
                bannerSize = kGADAdSizeSmartBannerPortrait;
            }
            else
            {
                NSLog(@"Orientation: isLandscape");
                bannerSize = kGADAdSizeSmartBannerLandscape;
            }
        break;}
        
        case 6:{
            NSLog(@"Adaptative");
            CGRect frame = g_controller.view.frame;
            if (@available(iOS 11.0, *)) {
              frame = UIEdgeInsetsInsetRect(g_controller.view.frame, g_controller.view.safeAreaInsets);
            }
            CGFloat viewWidth = frame.size.width;
            bannerSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth);
        break;}
            
        default: {NSLog(@"AddBanner illegal banner size type"); break;}//return;
    }
    
    self.bannerView = [[GADBannerView alloc] initWithAdSize:bannerSize];
    self.bannerView.adUnitID = self.BannerAdID;
    self.bannerView.rootViewController = g_controller;
    self.bannerView.delegate = self;
    [g_glView addSubview:self.bannerView];
    
    
    [self AdMob_Banner_Move:bottom];
    
    GADRequest *request = [self AdMob_AdRequest];
    
    [self.bannerView loadRequest:request];
    
}

-(void) AdMob_Banner_Move: (double)bottom
{
    int x_ = 1;
    int y_;
    if(bottom)
        y_ = 2;
    else
        y_ = 0;
    
    if(self.bannerView != nil)
    {
        
        CGSize size = CGSizeFromGADAdSize( self.bannerView.adSize );
        int adW = size.width;
        int adH = size.height;
        
        //display -> view coords
        int x = -1;
        int y = -1;
        
        switch((int)x_)
        {
            case 0:
                x = 0;
            break;
                
            case 1:
                x = (int)(g_glView.bounds.size.width -  adW) / 2;
            break;
                
            case 2:
                x = (int)(g_glView.bounds.size.width) - adW;
            break;
                
        }
        
        switch((int)y_)
        {
            case 0:
                y = 0;
            break;
                
            case 1:
                y = (int)(g_glView.bounds.size.height - adH) / 2;
            break;
                
            case 2:
                y = (int)(1.0 * g_glView.bounds.size.height) - adH;
            break;
                
        }
        
        CGRect frame = self.bannerView.frame;
        frame.origin.x = x;
        frame.origin.y = y;
        self.bannerView.frame = frame;
        
    }
}

-(double) AdMob_Banner_GetWidth
{
    if(self.bannerView == nil)
        return 0;
    
    CGSize size = CGSizeFromGADAdSize(self.bannerView.adSize);
    int adW = size.width;
    //->display width
    int dispW = (int)(( adW * g_DeviceWidth ) / g_glView.bounds.size.width);
    return dispW;
    
}

-(double) AdMob_Banner_GetHeight
{
    if(self.bannerView == nil)
        return 0;
    
    CGSize size = CGSizeFromGADAdSize(self.bannerView.adSize);
    int adH = size.height;
    //->display height
    int dispH = (int)(( adH * g_DeviceHeight ) / g_glView.bounds.size.height);
    return dispH;
}

-(void) AdMob_Banner_Hide
{
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = true;
    }

}

-(void) AdMob_Banner_Show
{
    if( self.bannerView != nil )
    {
        self.bannerView.hidden = false;
    }

}

-(void) AdMob_Banner_Remove
{
    if( self.bannerView != nil )
    {
        [self.bannerView removeFromSuperview];
        self.bannerView.delegate = nil;
        //[self.bannerView release];
        self.bannerView = nil;
    }
}

///// INTERSTITIAL ////////////////////////////////////////////////////////////////////////////////

-(void) AdMob_Interstitial_Target:(NSString*) interstitialID
{
    self.interstitialAdID = interstitialID;
}

-(int) interstitial_search:(NSString*) _id
{
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADInterstitialAd class]])
    if([[(GADInterstitialAd*)[self.loads objectAtIndex:i] adUnitID] compare:_id] == NSOrderedSame)
        return i;
    return -1;
}

-(int) interstitial_count:(NSString*) _id
{
    int count = 0;
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADInterstitialAd class]])
    if([[(GADInterstitialAd*)[self.loads objectAtIndex:i] adUnitID] compare:_id] == NSOrderedSame)
        count++;
    return count;
}

-(void) Admob_Interstitial_Free_Loaded_Instances:(double) count
{
    for(int i = (int)[self.loads count]-1 ; i >= 0 && count>0 ; i--)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADInterstitialAd class]])
    if([[(GADInterstitialAd*)[self.loads objectAtIndex:i] adUnitID] compare:self.interstitialAdID] == NSOrderedSame)
    {
        count--;
        [self.loads removeObjectAtIndex:i];
    }
}

-(void) Admob_Interstitial_Max_Instances:(double) value
{
    Interstitial_Max_Instances = value;
}


-(void) AdMob_Interstitial_Load
{
    if ([self.interstitialAdID isEqualToString:@""])
        return;
    
    const NSString* contextID = self.interstitialAdID;
    self.request_interstitial = [GADRequest request];
    [GADInterstitialAd loadWithAdUnitID: self.interstitialAdID request:self.request_interstitial completionHandler:^(GADInterstitialAd *ad, NSError *error)
    {
        
        if (error)
        {
            int dsMapIndex = dsMapCreate();
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
            createSocialAsyncEventWithDSMap(dsMapIndex);
            
            return;
        }
        
        if([self interreward_count:self.interstitialAdID] < Interstitial_Max_Instances)
            [self.loads addObject:ad];
        ad.fullScreenContentDelegate = self;
        
        const GADInterstitialAd* interstitial = ad;
        
        if(Paid_Event)
        ad.paidEventHandler = ^void(GADAdValue *_Nonnull value)
        {
            GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = interstitial.responseInfo.loadedAdNetworkResponseInfo;
            [self onPaidEvent_Handler:value adUnitId:interstitial.adUnitID adType:@"Interstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:interstitial.responseInfo.adNetworkClassName];
        };
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_Interstitial_OnLoaded");
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_Interstitial_Show
{
    if([self interstitial_search: self.interstitialAdID] == -1)
        return;
    
    GADInterstitialAd *interstitial = [self.loads objectAtIndex:[self interstitial_search: self.interstitialAdID]];
        
        [interstitial presentFromRootViewController:g_controller];
        self.interstitial_keepMe = interstitial;
    [self.loads removeObjectAtIndex:[self interstitial_search: self.interstitialAdID]];
        showing_ad = true;
}

-(double) AdMob_Interstitial_IsLoaded
{
    return [self interstitial_count:self.interstitialAdID]>0?1.0:0.0;
}

-(double) AdMob_Interstitial_Instances_Count
{
    return [self interstitial_count:self.interstitialAdID];
}

///// REWARDED VIDEO //////////////////////////////////////////////////////////////////////////////

-(void) AdMob_RewardedVideo_Target:(NSString*) AdId
{
    self.rewardAd_ID = AdId;
}

-(int) reward_search:(NSString*) _id
{
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedAd class]])
    if([[(GADRewardedAd*)[self.loads objectAtIndex:i] adUnitID]compare:_id] == NSOrderedSame)
        return i;
    return -1;
}

-(int) reward_count:(NSString*) _id
{
    int count = 0;
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedAd class]])
    if([[(GADRewardedAd*)[self.loads objectAtIndex:i] adUnitID]compare:_id] == NSOrderedSame)
        count++;
    return count;
}

-(void) AdMob_RewardedVideo_Free_Loaded_Instances:(double) count
{
    for(int i = (int)[self.loads count]-1 ; i >= 0 && count>0 ; i--)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedAd class]])
    if([[(GADRewardedAd*)[self.loads objectAtIndex:i] adUnitID] compare:self.rewardAd_ID] == NSOrderedSame)
    {
        count--;
        [self.loads removeObjectAtIndex:i];
    }
}

-(void) AdMob_RewardedVideo_Max_Instances:(double) value
{
    RewardedVideo_Max_Instances = value;
}


-(void) AdMob_RewardedVideo_Load
{
    if ([self.rewardAd_ID isEqualToString:@""])
        return;
    
    self.request_rewarded = [GADRequest request];
    const NSString* contextID = self.rewardAd_ID;
    [GADRewardedAd loadWithAdUnitID: self.rewardAd_ID request: self.request_rewarded completionHandler:^(GADRewardedAd *ad, NSError *error)
    {
        int dsMapIndex = dsMapCreate();

        if (error)
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
        }
        else
        {
            if([self reward_count:self.rewardAd_ID] < RewardedVideo_Max_Instances)
                [self.loads addObject:ad];
            
            ad.fullScreenContentDelegate = self;
            
            if(Paid_Event)
            ad.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = ad.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEvent_Handler:value adUnitId:ad.adUnitID adType:@"Rewarded" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:ad.responseInfo.adNetworkClassName];
            };

            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnLoaded");
            dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_RewardedVideo_Show
{
    if([self reward_search:self.rewardAd_ID] == -1)
        return;
    
    GADRewardedAd *rewardAd = [self.loads objectAtIndex:[self reward_search:self.rewardAd_ID]];
    
    const NSString* contextID = rewardAd.adUnitID;
    
    [rewardAd presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        //NSDecimalNumber *amount = self.rewardAd.adReward.amount;
        
        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedVideo_OnReward");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);

    }];
    
    self.rewardAd_keepMe = rewardAd;
    showing_ad = true;
    

    [self.loads removeObjectAtIndex:[self reward_search: self.rewardAd_ID]];
    
}

-(double) AdMob_RewardedVideo_IsLoaded
{
    return [self reward_count: self.rewardAd_ID]>0?1.0:0.0;
}

-(double) AdMob_RewardedVideo_Instances_Count
{
    return [self reward_count: self.rewardAd_ID];
}

///// REWARDED INTESTITIAL ////////////////////////////////////////////////////////////////////////

-(void) AdMob_RewardedInterstitial_Target:(NSString*) AdId
{
    self.rewardInterstitialAd_ID = AdId;
}

-(int) interreward_search:(NSString*) _id
{
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedInterstitialAd class]])
    if([[(GADRewardedInterstitialAd*)[self.loads objectAtIndex:i] adUnitID]compare:_id] == NSOrderedSame)
        return i;
    return -1;
}

-(int) interreward_count:(NSString*) _id
{
    int count = 0;
    for(int i = 0 ; i < [self.loads count] ; i++)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedInterstitialAd class]])
    if([[(GADRewardedInterstitialAd*)[self.loads objectAtIndex:i] adUnitID]compare:_id] == NSOrderedSame)
        count++;
    return count;
}

-(void) AdMob_RewardedInterstitial_Free_Loaded_Instances:(double) count
{
    for(int i = (int)[self.loads count]-1 ; i >= 0 && count>0 ; i--)
    if([[self.loads objectAtIndex:i] isMemberOfClass:[GADRewardedInterstitialAd class]])
    if([[(GADRewardedInterstitialAd*)[self.loads objectAtIndex:i] adUnitID] compare:self.rewardInterstitialAd_ID] == NSOrderedSame)
    {
        count--;
        [self.loads removeObjectAtIndex:i];
    }
}

-(void) AdMob_RewardedInterstitial_Max_Instances:(double) value
{
    RewardedInterstitial_Max_Instances = value;
}


-(void) AdMob_RewardedInterstitial_Load
{
    if ([self.rewardInterstitialAd_ID isEqualToString:@""])
        return;
    
    const NSString* contextID = self.rewardInterstitialAd_ID;
    
    [GADRewardedInterstitialAd loadWithAdUnitID:self.rewardInterstitialAd_ID request:[GADRequest request] completionHandler:^(GADRewardedInterstitialAd* _Nullable ad, NSError* _Nullable error)
    {
        int dsMapIndex = dsMapCreate();

        if (error)
        {
            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnLoadFailed");
            dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
            dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
            dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
        }
        else
        {
            if([self interreward_count:self.rewardInterstitialAd_ID] < RewardedInterstitial_Max_Instances)
                [self.loads addObject:ad];
            
            ad.fullScreenContentDelegate = self;
            
            if(Paid_Event)
            ad.paidEventHandler = ^void(GADAdValue *_Nonnull value)
            {
                GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = ad.responseInfo.loadedAdNetworkResponseInfo;
                [self onPaidEvent_Handler:value adUnitId:ad.adUnitID adType:@"RewardedInterstitial" loadedAdNetworkResponseInfo:loadedAdNetworkResponseInfo mediationAdapterClassName:ad.responseInfo.adNetworkClassName];
            };

            dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnLoaded");
            dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
        }
        
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
}

-(void) AdMob_RewardedInterstitial_Show
{
    if([self interreward_search:self.rewardInterstitialAd_ID] == -1)
        return;
    
    GADRewardedInterstitialAd *rewardedInterstitialAd = [self.loads objectAtIndex:[self interreward_search:self.rewardInterstitialAd_ID]];
    
    const NSString* contextID = rewardedInterstitialAd.adUnitID;

    
    [rewardedInterstitialAd presentFromRootViewController:g_controller userDidEarnRewardHandler:^
    {
        //GADAdReward *reward = self.rewardedInterstitialAd.adReward;

        int dsMapIndex = dsMapCreate();
        dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_RewardedInterstitial_OnReward");
        dsMapAddString(dsMapIndex, (char*)"id", (char*)[contextID UTF8String]);
        createSocialAsyncEventWithDSMap(dsMapIndex);
    }];
    
    self.rewardedInterstitialAd_keepMe = rewardedInterstitialAd;
    showing_ad = true;
    
    [self.loads removeObjectAtIndex:[self interreward_search: self.rewardInterstitialAd_ID]];
}

-(double) AdMob_RewardedInterstitial_IsLoaded
{
    return [self interreward_count:self.rewardInterstitialAd_ID]>0?1.0:0.0;
}

-(double) AdMob_RewardedInterstitial_Instances_Count
{
    return [self interreward_count:self.rewardInterstitialAd_ID];
}

/////////////////App Open Ad/////////////////////////////////////////////////////////////////////////
-(void) AdMob_AppOpenAd_Target:(NSString*) adUnitId
{
    self.appOpenAdID = adUnitId;
}

-(void) onResume
{
    [self AdMob_AppOpenAd_Show];
}

-(void) AdMob_AppOpenAd_Enable:(double) orientation
{
    AppOpenAd_Enable = true;
    self.appOpenAd = nil;
    AppOpenAd_orientation = orientation;
    [self AdMob_AppOpenAd_Load:AppOpenAd_orientation];
}

-(void) AdMob_AppOpenAd_Disable
{
    AppOpenAd_Enable = false;
    self.appOpenAd = nil;
}

-(double) AdMob_AppOpenAd_IsEnabled
{
    return AppOpenAd_Enable ? 1.0:0.0;
}

-(void) AdMob_AppOpenAd_Load:(double) orientation
{
    if(!AppOpenAd_Enable)
        return;
        
    if ([self.appOpenAdID isEqualToString:@""])
        return;
    
      self.appOpenAd = nil;
      [GADAppOpenAd loadWithAdUnitID: self.appOpenAdID request:[GADRequest request] orientation:(orientation==0)?UIInterfaceOrientationLandscapeRight:UIInterfaceOrientationPortrait completionHandler:^(GADAppOpenAd *_Nullable appOpenAd, NSError *_Nullable error) {
                if (error) {
                    NSLog(@"Failed to load app open ad: %@", error);
                    
                    // int dsMapIndex = dsMapCreate();
                    // dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoadFailed");
                    // dsMapAddDouble(dsMapIndex, (char*)"errorCode", error.code);
                    // dsMapAddString(dsMapIndex, (char*)"errorMessage", (char*)[error.localizedDescription UTF8String]);
                    // createSocialAsyncEventWithDSMap(dsMapIndex);
                    
                    return;
                }
                self.appOpenAd = appOpenAd;
                self.appOpenAd.fullScreenContentDelegate = self;
                self.loadTime = [NSDate date];
                
                if(Paid_Event)
                self.appOpenAd.paidEventHandler = ^void(GADAdValue *_Nonnull value)
                {
                    GADAdNetworkResponseInfo *loadedAdNetworkResponseInfo = self.appOpenAd.responseInfo.loadedAdNetworkResponseInfo;

                    // NSDictionary<NSString *, id> *extras = strongSelf.rewardedAd.responseInfo.extrasDictionary;
                    // NSString *mediationGroupName = extras["mediation_group_name"];
                    // NSString *mediationABTestName = extras["mediation_ab_test_name"];
                    // NSString *mediationABTestVariant = extras["mediation_ab_test_variant"];

                    int dsMapIndex = dsMapCreate();
                    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_onPaidEvent");

                    dsMapAddString(dsMapIndex, (char*)"mediationAdapterClassName", (char*)[self.appOpenAd.responseInfo.adNetworkClassName UTF8String]);

                    dsMapAddString(dsMapIndex, (char*)"adUnitId", (char*)[/*self.appOpenAd.adUnitID*/ self.appOpenAdID UTF8String]);

                    dsMapAddDouble(dsMapIndex, (char*)"micros", value.value.doubleValue);
                    dsMapAddString(dsMapIndex, (char*)"currencyCode", (char*)[value.currencyCode UTF8String]);
                    dsMapAddDouble(dsMapIndex, (char*)"precision", (double)value.precision);

                    dsMapAddString(dsMapIndex, (char*)"adSourceName", (char*)[loadedAdNetworkResponseInfo.adSourceName UTF8String]);
                    dsMapAddString(dsMapIndex, (char*)"adSourceId", (char*)[loadedAdNetworkResponseInfo.adSourceID UTF8String]);
                    dsMapAddString(dsMapIndex, (char*)"adSourceInstanceName", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceName UTF8String]);
                    dsMapAddString(dsMapIndex, (char*)"adSourceInstanceId", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceID UTF8String]);

                    createSocialAsyncEventWithDSMap(dsMapIndex);
                };
              
              // int dsMapIndex = dsMapCreate();
              // dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_AppOpenAd_OnLoaded");
              // createSocialAsyncEventWithDSMap(dsMapIndex);
           }];
}

-(void) AdMob_AppOpenAd_Show
{
    if(!AppOpenAd_Enable)
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
    self->NPA = value >= 0.5;
}

BOOL showing_ad = false;
-(double) AdMob_IsShowingAd
{
    return showing_ad ? 1.0 : 0.0;
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

    if(self->NPA)
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

-(void) AdMob_Enable_Paid_Event
{
    Paid_Event = true;
}


-(void)onPaidEvent_Handler:(GADAdValue*) value adUnitId:(NSString*)adUnitId adType:(NSString*)adType loadedAdNetworkResponseInfo:(GADAdNetworkResponseInfo*)loadedAdNetworkResponseInfo mediationAdapterClassName:(NSString*)mediationAdapterClassName
{
    // NSDictionary<NSString *, id> *extras = strongSelf.rewardedAd.responseInfo.extrasDictionary;
    // NSString *mediationGroupName = extras["mediation_group_name"];
    // NSString *mediationABTestName = extras["mediation_ab_test_name"];
    // NSString *mediationABTestVariant = extras["mediation_ab_test_variant"];
    
    int dsMapIndex = dsMapCreate();
    dsMapAddString(dsMapIndex, (char*)"type", (char*)"AdMob_onPaidEvent");
    
    dsMapAddString(dsMapIndex, (char*)"mediationAdapterClassName", (char*)[mediationAdapterClassName UTF8String]);
    
    dsMapAddString(dsMapIndex, (char*)"adUnitId", (char*)[adUnitId UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"adType", (char*)[adType UTF8String]);
    
    dsMapAddDouble(dsMapIndex, (char*)"micros", value.value.doubleValue);
    dsMapAddString(dsMapIndex, (char*)"currencyCode", (char*)[value.currencyCode UTF8String]);
    dsMapAddDouble(dsMapIndex, (char*)"precision", (double)value.precision);
    
    dsMapAddString(dsMapIndex, (char*)"adSourceName", (char*)[loadedAdNetworkResponseInfo.adSourceName UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"adSourceId", (char*)[loadedAdNetworkResponseInfo.adSourceID UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"adSourceInstanceName", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceName UTF8String]);
    dsMapAddString(dsMapIndex, (char*)"adSourceInstanceId", (char*)[loadedAdNetworkResponseInfo.adSourceInstanceID UTF8String]);
    
    createSocialAsyncEventWithDSMap(dsMapIndex);
};

@end
