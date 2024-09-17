
/**
 * @func AdMob_Initialize
 * @desc This function initialises the Google AdMob API and should be called at the start of your game.
 * @func_end
 */
function AdMob_Initialize() { }

/**
 * @func AdMob_SetTestDeviceId
 * @desc This function tells the app to use test ads instead of "live" ads, essential for testing whether your ads work without generating potentially fraudulent click-throughs.
 * 
 * [[Note: This function should be called BEFORE calling ${function.AdMob_Initialize}.]]
 * 
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_SetTestDeviceId() { }

///// BANNER
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Banner_Init
 * @desc This function initialises the target identifier for banner functions.
 * 
 * [[Note: Please refer to ${function.AdMob_Banner_Set_AdUnit} for more information.]]
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_Banner_Init(adUnitId) { }

/**
 * @func AdMob_Banner_Set_AdUnit
 * @desc This function sets the target identifier for banner functions, Banner functions don't allow multiple preloaded identifiers.
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Banner_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_Banner_Create
 * @desc This function creates a banner ad.
 * 
 * @param {real} size The type of the banner to be displayed.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type The string `"AdMob_Banner_OnLoaded"`
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_Banner_OnLoadFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Banner_Create(size, bottom) { }

/**
 * @func AdMob_Banner_Create_Ext
 * @desc This function creates a banner ad with extended alignment options.
 * 
 * @param {real} size The type of the banner to be displayed.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @param {constant.AdMobBannerAlignment} alignment The horizontal alignment to be used by the banner.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type The string `"AdMob_Banner_OnLoaded"`
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_Banner_OnLoadFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @version 1.4.0 (+)
 * 
 * @func_end
 */
function AdMob_Banner_Create_Ext(size, bottom, alignment) { }


/**
 * @func AdMob_Banner_GetWidth
 * @desc This function gets the width of the currently loaded banner ad block. The value returned is in pixels.
 * 
 * [[Note: This function returns the width in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.]]
 * 
 * @returns {real}
 * @func_end
 */
function AdMob_Banner_GetWidth() { }

/**
 * @func AdMob_Banner_GetHeight
 * @desc This function gets the height of the currently loaded banner ad block. The value returned is in pixels.
 * 
 * [[Note: This function returns the height in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.]]
 * 
 * @returns {real}
 * @func_end
 */
function AdMob_Banner_GetHeight() { }

/**
 * @func AdMob_Banner_Move
 * @desc This function moves a banner that has been previously added. You supply a boolean that will determine if the banner should be placed at the bottom or at the top of the display.
 * 
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @returns {constant.AdMobErrors}
 * 
 * @func_end
 */
function AdMob_Banner_Move(bottom) { }

/**
 * @func AdMob_Banner_Show
 * @desc This function shows the currently active, but hidden, banner ad block. When called, the banner will be shown to the user again and will be able to receive input.
 * 
 * You can hide the banner again at any time using the ${function.AdMob_Banner_Hide} function.
 * 
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Show() { }

/**
 * @func AdMob_Banner_Hide
 * @desc This function hides the currently active banner ad block. When called, the banner will be removed from the user’s view and will no longer receive input.
 * 
 * You can show the banner again at any time using the ${function.AdMob_Banner_Show} function.
 * 
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Hide() { }

/**
 * @func AdMob_Banner_Remove
 * @desc This function removes the currently active banner from the app. If you call this function then want to show ads again, you must call the ${function.AdMob_Banner_Create} function first to add a new banner to the display.
 * 
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Remove() { }

///// INTERSTITIAL
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Interstitial_Init
 * @desc This function initialises the target identifier for interstitial ad functions.
 * 
 * [[Note: Please refer to ${function.AdMob_Interstitial_Set_AdUnit} for more information.]]
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_Interstitial_Init(adUnitId) { }

/**
 * @func AdMob_Interstitial_Set_AdUnit
 * @desc This function sets the target identifier for interstitial functions, interstitials allow multiple identifiers.
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Interstitial_Set_AdUnit(adUnitId) { }

/**
 * @func Admob_Interstitial_Free_Load_Instances
 * @desc This function releases interstitial load instances (passing -1 will free all the loaded instances).
 * 
 * @param {double} count The number of interstitial ad instances to free
 * @version 1.3.0 (+)
 * @func_end
 */
function Admob_Interstitial_Free_Load_Instances(count) { }

/**
 * @func Admob_Interstitial_Max_Instances
 * @desc This function sets the maximum number of Interstitial load instances, this allows you to present consecutive ads. Default value is 1.
 * 
 * @param {double} count The maximum number of interstitial ads that can be loaded at the same time
 * @version 1.3.0 (+)
 * @func_end
 */
function Admob_Interstitial_Max_Instances(count) { }


/**
 * @func AdMob_Interstitial_Load
 * @desc This function should be called when you want to load an interstitial ad. Calling it will send a request to the ad server to provide an interstitial ad, which will then be loaded into the app for display.
 * 
 * This function does not show the ad, just stores it in memory ready to be shown. If you do not call this function before trying to show an ad, nothing will be shown.
 * 
 * [[Note: You can check whether an interstitial is loaded or not using the function ${function.AdMob_Interstitial_IsLoaded}.]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_Interstitial_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_Interstitial_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Interstitial_Load() { }


/**
 * @func AdMob_Interstitial_Show
 * @desc This function will show the next interstitial ad, if one is available and loaded. You can check whether an ad is available using the function ${function.AdMob_Interstitial_IsLoaded}.
 * 
 * [[Note: While an interstitial is being shown, your app will be put into the background and will effectively be "paused".]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the ad view is closed by the user.
 * @member {string} type `"AdMob_Interstitial_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_Interstitial_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_Interstitial_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * @func_end
 */
function AdMob_Interstitial_Show() { }

/**
 * @func AdMob_Interstitial_IsLoaded
 * @desc This function returns whether an interstitial ad is loaded.
 * 
 * @returns {bool}
 * @func_end
 */
function AdMob_Interstitial_IsLoaded() { }

/**
 * @func AdMob_Interstitial_Instances_Count
 * @desc This function returns the number of Interstitial load instances that are ready.
 * 
 * @returns {real}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Interstitial_Instances_Count() { }

///// SERVER SIDE VERIFICATION
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_ServerSideVerification_Set
 * @desc This function sets the values to be used for server side verification of rewarded ads.
 * 
 * [[Tip: Please refer to [Server-Side Verification](https://support.google.com/admob/answer/9603226) for more information.]]
 * 
 * [[Note: Once set the values are used for future loaded reward video and reward interstitial ads, to clear the data use ${function.AdMob_ServerSideVerification_Clear}]]
 * 
 * @param {string} userId The user ID is a unique identifier given to each user, which lets you know who to reward upon successful ad completion. 
 * @param {string} customData Custom data is received in the callback after a user’s successful ad completion. For example, you can include a parameter to know which level the user is on.
 * @version 1.4.0 (+)
 * @func_end
 */
function AdMob_ServerSideVerification_Set(userId, customData) { }

/**
 * @func AdMob_ServerSideVerification_Clear
 * @desc This function clears the previously set values to be used for server side verification of rewarded ads.
 * 
 * [[Note: Please refer to ${function.AdMob_RewardedVideo_Set_AdUnit} for more information.]]
 * 
 * @version 1.4.0 (+)
 * @func_end
 */
function AdMob_ServerSideVerification_Clear() { }


///// REWARDED VIDEO
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_RewardedVideo_Init
 * @desc This function initialises the target identifier for rewarded video ad functions.
 * 
 * [[Note: Please refer to ${function.AdMob_RewardedVideo_Set_AdUnit} for more information.]]
 * 
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_RewardedVideo_Init(adUnitId) { }

/**
 * @func AdMob_RewardedVideo_Set_AdUnit
 * @desc This function sets the target identifier for rewarded video functions. Rewarded video functions allow multiple identifiers.
 * 
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_RewardedVideo_Free_Load_Instances
 * @desc This function releases the requested number of Rewarded Video load instances.
 * 
 * @param {double} count The number of instances to release (-1 will free all the loaded instances)
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Free_Load_Instances(count) { }

/**
 * @func AdMob_RewardedVideo_Max_Instances
 * @desc This function sets the max number of Rewarded Video load instances, this allows you to present consecutive ads. Default value is 1.
 * 
 * @param {double} count The maximum number of instances
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Max_Instances(count) { }

/**
 * @func AdMob_RewardedVideo_Load
 * @desc This function should be called when you want to load a rewarded video ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display.
 * 
 * This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown.
 * 
 * [[Note: You can check whether a rewarded video is loaded or not using the function ${function.AdMob_RewardedVideo_IsLoaded}.]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_RewardedVideo_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedVideo_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisement
 * 
 * @event_end
 * @func_end
 */
function AdMob_RewardedVideo_Load() { }

/**
 * @func AdMob_RewardedVideo_Show
 * @desc This function will show the next rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function ${function.AdMob_RewardedVideo_IsLoaded}.
 * 
 * [[Note: While a rewarded video ad is being shown, your app will be put into the background and will effectively be "paused".]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_RewardedVideo_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_RewardedVideo_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedVideo_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the user should be rewarded.
 * @member {string} type `"AdMob_RewardedVideo_OnReward"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedVideo_Show() { }

/**
 * @func AdMob_RewardedVideo_IsLoaded
 * @desc This function returns whether a rewarded video ad has been loaded or not.
 * 
 * @returns {bool}
 * @func_end
 */
function AdMob_RewardedVideo_IsLoaded() { }


/**
 * @func AdMob_RewardedVideo_Instances_Count
 * @desc This function returns the number of Rewarded video load instances that are ready.
 * 
 * @returns {real}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Instances_Count() { }


///// REWARDED INTESTITIAL
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_RewardedInterstitial_Init
 * @desc This function initialises the target identifier for rewarded interstitial ad functions.
 * 
 * [[Note: Please refer to ${function.AdMob_RewardedInterstitial_Set_AdUnit} for more information.]]
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_RewardedInterstitial_Init(adUnitId) { }

/**
 * @func AdMob_RewardedInterstitial_Set_AdUnit
 * @desc This function sets the target identifier for rewarded interstitial functions. Rewarded interstitial functions allow multiple identifiers.
 * 
 * @param {string} adUnitId The ad unit ID
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_RewardedInterstitial_Free_Load_Instances
 * @desc This function releases the requested number of Rewarded Interstitial load instances.
 * 
 * @param {double} count The number of instances to release (-1 will free all the loaded instances)
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Free_Load_Instances(count) { }


/**
 * @func AdMob_RewardedInterstitial_Max_Instances
 * @desc This function sets the maximum number of Rewarded Insterstitials load instances, this allows you to present consecutive ads. Default value is 1.
 * 
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Max_Instances(count) { }

/**
 * @func AdMob_RewardedInterstitial_Load
 * @desc This function should be called when you want to load a rewarded interstitial ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display.
 * 
 * This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown.
 * 
 * [[Note: You can check whether a rewarded interstitial is loaded or not using the function ${function.AdMob_RewardedInterstitial_IsLoaded}.]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_RewardedInterstitial_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedInterstitial_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedInterstitial_Load() { }


/**
 * @func AdMob_RewardedInterstitial_Show
 * @desc This function will show the next rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function ${function.AdMob_RewardedInterstitial_IsLoaded}.
 * 
 * [[Note: While a rewarded interstitial ad is being shown, your app will be put into the background and will effectively be "paused".]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_RewardedInterstitial_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_RewardedInterstitial_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedInterstitial_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the user should be rewarded.
 * @member {string} type `"AdMob_RewardedInterstitial_OnReward"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedInterstitial_Show() { }


/**
 * @func AdMob_RewardedInterstitial_IsLoaded
 * @desc This function returns whether a rewarded interstitial ad has been loaded or not.
 * 
 * @returns {bool}
 * @func_end
 */
function AdMob_RewardedInterstitial_IsLoaded() { }


/**
 * @func AdMob_RewardedInterstitial_Instances_Count
 * @desc This function returns the number of Rewarded Interstitial load instances that are ready.
 * 
 * @returns {real}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Instances_Count() { }


///// APP OPEN
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_AppOpenAd_Init
 * @desc This function initialises the target identifier for app open ads functions.
 * 
 * [[Note: Please refer to ${function.AdMob_AppOpenAd_Set_AdUnit} for more information.]]
 * 
 * @param {string} adUnitId The ID of the ad unit
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_AppOpenAd_Init(adUnitId) { }

/**
 * @func AdMob_AppOpenAd_Set_AdUnit
 * @desc This function sets the target identifier for app open ads functions. App open ad functions don't allow multiple pre-loaded identifiers.
 * 
 * @param {string} adUnitId The ID of the ad unit
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_AppOpenAd_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_AppOpenAd_Enable
 * 
 * @desc This function enables show App Open Ads when the game resumes from background. 
 * 
 * [[Note: This is part of the automatic management of the App Open Ad lifecycle, if you with to manually handle it you can turn it of and use the functions ${function.AdMob_AppOpenAd_Load} and ${function.AdMob_AppOpenAd_Show}.
 * 
 * @param {double} orientation [DEPRECATED] Required but not used.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_AppOpenAd_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_AppOpenAd_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_AppOpenAd_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @version 1.3.0 (+)
 * 
 * @func_end
 */
function AdMob_AppOpenAd_Enable(orientation) { }

/**
 * @func AdMob_AppOpenAd_Disable
 * @desc This function disables showing of App Open Ads when the game resumes.
 * 
 * [[Note: This is part of the automatic management of the App Open Ad lifecycle, if you with to manually handle it you can turn it of and use the functions ${function.AdMob_AppOpenAd_Load} and ${function.AdMob_AppOpenAd_Show}.
 * 
 * @returns {constant.AdMobErrors}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_AppOpenAd_Disable() { }

/**
 * @func AdMob_AppOpenAd_IsEnabled
 * @desc This function returns `true` if the automatic management of the App Open Ad lifecycle is enabled, `false` otherwise.
 * 
 * @returns {bool}
 * @version 1.4.0 (+)
 * @func_end
 */
function AdMob_AppOpenAd_IsEnabled() { }

/**
 * @func AdMob_AppOpenAd_Load
 * @desc This function should be called when you want to load an app open ad. Calling it will send a request to the ad server to provide an app open ad, which will then be loaded into the app for display.
 * 
 * This function does not show the ad, just stores it in memory ready to be shown. If you do not call this function before trying to show an ad, nothing will be shown.
 * 
 * [[Note: You can check whether the app open ad is loaded or not using the function ${function.AdMob_AppOpenAd_IsLoaded}.]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_AppOpenAd_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_AppOpenAd_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisement
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @version 1.4.0 (+)
 * 
 * @func_end
 */
function AdMob_AppOpenAd_Load() { }

/**
 * @func AdMob_AppOpenAd_Show
 * @desc This function will show the app open ad, if one is available and loaded. You can check whether an ad is available using the function ${function.AdMob_AppOpenAd_IsLoaded}.
 * 
 * [[Note: While an app open ad is being shown, your app will be put into the background and will effectively be "paused".]]
 * 
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the ad view is closed by the user.
 * @member {string} type `"AdMob_AppOpenAd_OnDismissed"`
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task fails.
 * @member {string} type `"AdMob_AppOpenAd_OnShowFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the awaited task succeeds.
 * @member {string} type `"AdMob_AppOpenAd_OnFullyShown"`
 * @event_end
 * 
 * @version 1.4.0 (+)
 * 
 * @func_end
 */
function AdMob_AppOpenAd_Show() { }

/**
 * @func AdMob_AppOpenAd_IsLoaded
 * @desc This function returns whether an app open ad is loaded.
 * 
 * @returns {bool}
 * 
 * @version 1.4.0 (+)
 * 
 * @func_end
 */
function AdMob_AppOpenAd_IsLoaded() { }


///// TARGETING
///// ///////////////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Targeting_COPPA
 * @desc This function toggles on/off ads for children.
 * 
 * [[Warning: This function should be called before ${function.AdMob_Initialize}.]]
 * 
 * @param {bool} COPPA Whether COPPA targeted advertising should be enabled
 * @func_end
 */
function AdMob_Targeting_COPPA(COPPA) { }

/**
 * @func AdMob_Targeting_UnderAge
 * @desc This function toggles on/off ads for under aged users.
 * 
 * [[Warning: This function should be called before ${function.AdMob_Initialize}.]]
 * 
 * @param {bool} underAge Whether under-age ads should be enabled
 * @func_end
 */
function AdMob_Targeting_UnderAge(underAge) { }

/**
 * @func AdMob_Targeting_MaxAdContentRating
 * @desc This function allows setting the maximum content rating of the ads to be displayed.
 * 
 * [[Warning: This function should be called before ${function.AdMob_Initialize}.]]
 * 
 * @param {constant.AdMobContentRating} contentRating The maximum content rating
 * @func_end
 */
function AdMob_Targeting_MaxAdContentRating(contentRating) { }

///// CONSENT
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Consent_RequestInfoUpdate
 * @desc This function requests a consent information update (this needs to be called prior to ${function.AdMob_Consent_Load}).
 * 
 * @param {constant.AdMobConsentMode} mode The consent mode
 * 
 * @event social
 * @member {string} type The string `"AdMob_Consent_OnRequestInfoUpdated"`
 * @event_end
 * 
 * @event social
 * @member {string} type The string `"AdMob_Consent_OnRequestInfoUpdateFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Consent_RequestInfoUpdate(mode) { }

/**
 * @func AdMob_Consent_GetStatus
 * @desc This function allows to set the mode of the consent request being used. This function allows you to debug different regions and EEA and NON-EEA and should be passed in as a ${constant.AdMobConsentMode} constant.
 * 
 * This function should be called before ${function.AdMob_Consent_GetStatus} and ${function.AdMob_Consent_GetType} in order to get the correct output from both functions.
 * 
 * [[Note: This function requires a previous call to ${function.AdMob_Consent_RequestInfoUpdate}.]]
 * 
 * @returns {constant.AdMobConsentStatus}
 * @func_end
 */
function AdMob_Consent_GetStatus() { }

/**
 * @func AdMob_Consent_GetType
 * @desc This function returns the answer given by the user to a previous GDPR consent request.
 * 
 * @returns {constant.AdMobConsentType}
 * @func_end
 */
function AdMob_Consent_GetType() { }


/**
 * @func AdMob_Consent_IsFormAvailable
 * @desc This function checks whether or not the GDPR consent form is available on this device.
 * 
 * @returns {bool}
 * @func_end
 */
function AdMob_Consent_IsFormAvailable() { }

/**
 * @func AdMob_Consent_Load
 * @desc This function loads the consent form into memory so it can be displayed to the user. If you do not call this function before trying to show the GDPR consent, nothing will be shown.
 * 
 * [[Note: This function requires a previous call to ${function.AdMob_Consent_RequestInfoUpdate}.]]
 * 
 * @event social
 * @member {string} type The string `"AdMob_Consent_OnLoaded"`
 * @event_end
 * 
 * @event social
 * @member {string} type The string `"AdMob_Consent_OnLoadFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Consent_Load() { }


/**
 * @func AdMob_Consent_Show
 * @desc This function shows the consent form to the user. If you do not call the ${function.AdMob_Consent_Load} function before trying to show the GDPR consent, nothing will be shown.
 * 
 * [[Note: This function requires a previous call to ${function.AdMob_Consent_Load}.]]
 * 
 * @event social
 * @member {string} type The string `"AdMob_Consent_OnShown"`
 * @event_end
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnShowFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Consent_Show() { }

/**
 * @func AdMob_Consent_Reset
 * @desc This function resets the consent status flag.
 * @func_end
 */
function AdMob_Consent_Reset() { }

/**
 * @func AdMob_Consent_Set_RDP
 * @desc Enables or disables Restricted Data Processing (RDP) to comply with privacy regulations in specific U.S. states.
 * 
 * [[Note: Developers must determine the appropriate timing to activate RDP based on their application's compliance requirements.]]
 *  
 * @func_end
 */
function AdMob_Consent_Set_RDP() { }

///// SETTINGS
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Settings_SetVolume
 * @desc This method provides control over the sound's loudness when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * 
 * @param {real} value The amount to set the volume to (a value from 0 (muted) to 1.0 (full media volume). Defaults to 1.0)
 * @func_end
 */
function AdMob_Settings_SetVolume(value) { }

/**
 * @func AdMob_Settings_SetMuted
 * @desc This method provides control over muting the sound when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * 
 * @param {boolean} value Whether to mute the sound
 * @func_end
 */
function AdMob_Settings_SetMuted(value) { }

/**
 * @func AdMob_Events_OnPaidEvent
 * @desc This function enables the paid load callbacks.
 * 
 * [[Note: You should enable this feature in your console too https://support.google.com/admob/answer/11322405.]]
 * 
 * @param {bool} enable Whether to enable paid load callbacks.
 * 
 * @event social
 * @member {string} type The string `"AdMob_OnPaidEvent"`
 * @member {string} mediation_adapter_class_name The mediation adapter class name of the ad network that loaded the ad.
 * @member {string} unit_id identifier of the ad
 * @member {string} ad_type `'Banner"`, `"Interstitial"`, `"Rewarded"`, `"RewardedInterstitial"` or `"AppOpen"`
 * @member {real} micros The ad's value in micro-units, where 1,000,000 micro-units equal one unit of the currency.
 * @member {string} currency_code The value's ISO 4217 currency code.
 * @member {constant.AdMobAdValuePrecision} precision The precision type of the reported ad value.
 * @member {string} ad_source_name Gets the ad source representing the specific ad network that serves the impression. For campaigns, Mediated House Ads is returned for a mediated ads campaign goal type, and Reservation Campaign is returned for impression and click goal types. See Ad sources for the list of possible ad source names when an ad network serves the ad.
 * @member {string} ad_source_id Gets the ad source ID associated with this adapter response. For campaigns, 6060308706800320801 is returned for a mediated ads campaign goal type, and 7068401028668408324 is returned for impression and click goal types. See Ad sources for the list of possible ad source IDs when an ad network serves the ad.
 * @member {string} ad_source_instance_name Gets the ad source instance name associated with this adapter response.
 * @member {string} ad_source_instance_id Gets the ad source instance ID associated with this adapter response.
 * @event_end
 * 
 * @func_end
 */
function AdMob_Events_OnPaidEvent(enable) { }

/**
 * @const AdMobErrors
 * @desc This set of constants represents the error values that can be returned from the AdMob function calls.
 * @member ADMOB_OK There were no errors.
 * @member ADMOB_ERROR_NOT_INITIALIZED The AdMob extension needs to be initialized prior to this call
 * @member ADMOB_ERROR_INVALID_AD_ID The provided ad unit ID is not valid.
 * @member ADMOB_ERROR_AD_LIMIT_REACHED The limit of loaded ads for this specific type was reached.
 * @member ADMOB_ERROR_NO_ADS_LOADED There are no loaded ads to be shown for this specific type.
 * @member ADMOB_ERROR_NO_ACTIVE_BANNER_AD There is no active banner ad.
 * @member ADMOB_ERROR_ILLEGAL_CALL The call you are trying to execute is illegal (used for functions that need to be called prior to initialization).
 * @member ADMOB_ERROR_NULL_VIEW_HANDLER The view handler responsible for rendering the ads is not available (only available on Android).
 * @const_end
 */

/**
 * @const AdMobAdValuePrecision
 * @desc This set of constants represents the precision type of the reported ad value.
 * @member ADMOB_ADVALUE_PRECISION_UNKNOWN An unknown precision type.
 * @member ADMOB_ADVALUE_PRECISION_ESTIMATED An ad value estimated from aggregated data.
 * @member ADMOB_ADVALUE_PRECISION_PRECISE The precise value paid for this ad.
 * @member ADMOB_ADVALUE_PRECISION_PUBLISHER_PROVIDED A publisher-provided ad value, such as manual CPMs in a mediation group.
 * @const_end
 */

/**
 * @const AdMobBanner
 * @desc This set of constants represents the various types of available banner.
 * @member AdMob_Banner_NORMAL Normal sized banner (320x50 dp)
 * @member AdMob_Banner_LARGE Large sized banner (320x100 dp)
 * @member AdMob_Banner_MEDIUM IAB medium rectangle (300x250 dp)
 * @member AdMob_Banner_FULL IAB full-size banner (468x60 dp - tablets only)
 * @member AdMob_Banner_LEADERBOARD IAB leaderboard (728x90 dp - tablets only)
 * @member AdMob_Banner_SMART A dynamic size banner (deprecated, see `AdMob_Banner_ADAPTIVE`)
 * @member AdMob_Banner_ADAPTIVE A dynamically sized banner
 * @const_end
 */

/**
 * @const AdMobBannerAlignment
 * @desc This set of constants represents the banner alignment style.
 * @member ADMOB_BANNER_ALIGNMENT_LEFT Left aligns the banner being created.
 * @member ADMOB_BANNER_ALIGNMENT_CENTER Center aligns the banner being created
 * @member ADMOB_BANNER_ALIGNMENT_RIGHT Right aligns the banner being created
 * @const_end
 */

/**
 * @const AdMobContentRating
 * @desc This set of constants represents the various types of possible content ratings for ads.
 * @member AdMob_ContentRating_GENERAL Content suitable for general audiences.
 * @member AdMob_ContentRating_PARENTAL_GUIDANCE Content suitable for most audiences with parental guidance.
 * @member AdMob_ContentRating_TEEN Content suitable for teen and older audiences.
 * @member AdMob_ContentRating_MATURE_AUDIENCE Content suitable only for mature audiences.
 * @const_end
 */

/**
 * @const AdMobConsentStatus
 * @desc This set of constants represents the various consent status.
 * @member AdMob_Consent_Status_UNKNOWN Consent status is unknown.
 * @member AdMob_Consent_Status_NOT_REQUIRED User consent not required.
 * @member AdMob_Consent_Status_REQUIRED User consent required but not yet obtained.
 * @member AdMob_Consent_Status_OBTAINED User consent obtained. Personalized vs non-personalized undefined.
 * @const_end
 */

/**
 * @const AdMobConsentType
 * @desc This set of constants represents the given consent type.
 * @member AdMob_Consent_Type_UNKNOWN Consent type is unknown (before consent was requested).
 * @member AdMob_Consent_Type_NON_PERSONALIZED Consent was given for non-personalized ads.
 * @member AdMob_Consent_Type_PERSONALIZED Consent was given for personalized ads.
 * @member AdMob_Consent_Type_DECLINED Consent was declined for any kind of ads.
 * @const_end
 */

/**
 * @const AdMobConsentMode
 * @desc This set of constants represents the consent mode (these are used for testing porpuses).
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_DISABLED Debug geography disabled.
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_EEA Geography appears as in EEA for debug devices.
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_NOT_EEA Geography appears as not in EEA for debug devices.
 * @member AdMob_Consent_Mode_PRODUCTION Same as `AdMob_Consent_Mode_DEBUG_GEOGRAPHY_DISABLED`, used for production.
 * @const_end
 */

/**
 * @module general
 * @title General
 * @desc This module contains general functions for working with AdMob.
 * 
 * @section_func
 * @ref AdMob_Initialize
 * @ref AdMob_SetTestDeviceId
 * @ref AdMob_Events_OnPaidEvent
 * @section_end
 * 
 * @module_end
 */
 
/**
 * @module consent
 * @title Consent
 * @desc This module contains functions related to requesting the user's consent.
 * 
 * @section_func
 * @ref AdMob_Consent_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module targeting
 * @title Targeting
 * @desc This module contains functions for targeting ads to specific audiences.
 * 
 * @section_func
 * @ref AdMob_Targeting_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module banner
 * @title Banner
 * @desc This module contains functions for banner ads.
 * 
 * @section_func
 * @ref AdMob_Banner_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module interstitial
 * @title Interstitial
 * @desc This module contains functions for interstitial ads.
 * 
 * @section_func
 * @ref AdMob_Interstitial_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module reward_video
 * @title Rewarded Video
 * @desc This module contains functions for rewarded video ads.
 * 
 * @section_func
 * @ref AdMob_RewardedVideo_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module reward_interstitial
 * @title Rewarded Interstitial
 * @desc This module contains functions for rewarded interstitial ads.
 * 
 * @section_func
 * @ref AdMob_RewardedInterstitial_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module app_open
 * @title App Open Ads
 * @desc This module contains functions for App Open Ads.
 * 
 * App open ads are a special ad format intended for publishers wishing to monetize their app load screens. App open ads can be closed at any time, and are designed to be shown when your users bring your app to the foreground.
 * 
 * @section_func
 * @ref AdMob_AppOpenAd_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module settings
 * @title Settings
 * @desc This module contains functions related to AdMob settings.
 * 
 * @section_func
 * @ref AdMob_Settings_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module constants
 * @title Constants
 * @desc The following are the available constants to use with the AdMob API.
 * 
 * @section_const
 * @ref AdMobErrors
 * @ref AdMobBanner
 * @ref AdMobBannerAlignment
 * @ref AdMobContentRating
 * @ref AdMobConsentStatus
 * @ref AdMobConsentType
 * @ref AdMobConsentMode
 * @ref AdMobAdValuePrecision
 * @section_end
 * 
 * @module_end
 */

/**
 * @module home 
 * @title AdMob
 * @desc This is the AdMob extension which provides functionality to developers to add Google Ads to their game. In this wiki you can find the full available API documentation and guides necessary to get started.
 * 
 * ## Extension's Features
 * 
 * * Enable test mode (development)
 * * Request GDPR Consent
 * * Target ads to your specific audience
 *   * Under-age
 *   * Children
 *   * Max Rating system
 * * Use 5 distinct types of ads:
 *   * Banners (7 different banner types)
 *   * Interstitial
 *   * RewardedVideos
 *   * RewardedInterstitial
 *   * AppOpenAd
 * * Allows ad volume control (including mute toggle)
 * 
 * @section Guides
 * @desc 
 * Before you start using this extension make sure to follow our ${page.setup} guide that will get you up and running.
 *
 * To get started using this extension, follow the ${page.quick_start_guide}.
 * 
 * For the recommended workflow see the ${page.workflow} page.
 * @section_end
 * 
 * @section Modules
 * @desc The following are the available modules from the AdMob API:
 * @ref module.general
 * @ref module.settings
 * @ref module.consent
 * @ref module.targeting
 * @ref module.banner
 * @ref module.interstitial
 * @ref module.reward_video
 * @ref module.reward_interstitial
 * @ref module.app_open
 * @ref module.constants
 * @section_end
 * 
 * @module_end
 */
