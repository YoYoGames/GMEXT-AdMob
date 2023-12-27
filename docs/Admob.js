
/**
 * @func AdMob_Initialize
 * @desc This function initializes the Google AdMob API and should be called at the start of your game.
 * @func_end
 */
function AdMob_Initialize() { }

/**
 * @func AdMob_SetTestDeviceId
 * @desc This function tells the app to use test ads instead of “live” ads, essential for testing whether your ads work without generating potentially fraudulent click-throughs. This function should be called BEFORE calling AdMob_Initialize.
 * @func_end
 */
function AdMob_SetTestDeviceId() { }

///// BANNER
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Banner_Init
 * @desc Initializes the target identifier for banner functions.
 * > [!NOTE]
 * > Please refer to ${function.AdMob_Banner_Set_AdUnit} for more information.
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_Banner_Init(adUnitId) { }

/**
 * @func AdMob_Banner_Set_AdUnit
 * @desc Set the target identifier for banner functions, Banner funcitons DOESN'T allow multiple preloaded identifiers.
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Banner_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_Banner_Create
 * @desc
 * @param {real} size The type of the banner to be displayed.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_Banner_OnLoaded"`
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_Banner_OnLoadFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Banner_Create(size, bottom) { }


/**
 * @func AdMob_Banner_GetWidth
 * @desc This function can be used to get the width of the currently loaded banner ad block. The value returned is in pixels. NOTE: This function returns the width in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.
 * @returns {real}
 * @func_end
 */
function AdMob_Banner_GetWidth() { }

/**
 * @func AdMob_Banner_GetHeight
 * @desc This function can be used to get the height of the currently loaded banner ad block. The value returned is in pixels. NOTE: This function returns the height in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.
 * @returns {real}
 * @func_end
 */
function AdMob_Banner_GetHeight() { }

/**
 * @func AdMob_Banner_Move
 * @desc This function can be used to move a banner that has been previously added. You supply a boolean that will determine if the banner should be placed at the bottom or at the top of the display.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @returns {constant.AdMobErrors}
 * 
 * @func_end
 */
function AdMob_Banner_Move(bottom) { }

/**
 * @func AdMob_Banner_Show
 * @desc This function can be used to show the currently active, but hidden, banner ad block. When called, the banner will be shown to the user again and will be able to receive input. You can hide the banner again at any time using the AdMob_Banner_Hide function.
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Show() { }

/**
 * @func AdMob_Banner_Hide
 * @desc This function can be used to hide the currently active banner ad block. When called, the banner will be removed from the user’s view and will no longer receive input. You can show the banner again at any time using the AdMob_Banner_Show function.
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Hide() { }

/**
 * @func AdMob_Banner_Remove
 * @desc This function will remove the currently active banner from the app. If you call this function then want to show ads again, you must call the AdMob_Banner_Create function first to add a new banner to the display.
 * @returns {constant.AdMobErrors}
 * @func_end
 */
function AdMob_Banner_Remove() { }

///// INTERSTITIAL
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Interstitial_Init
 * @desc Initializes the target identifier for interstitial ad functions.
 * > [!NOTE]
 * > Please refer to ${function.AdMob_Interstitial_Set_AdUnit} for more information.
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_Interstitial_Init(adUnitId) { }

/**
 * @func AdMob_Interstitial_Set_AdUnit
 * @desc Set the target identifier for interstitial functions, Interstitials functions allow multiple identifiers
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Interstitial_Set_AdUnit(adUnitId) { }

/**
 * @func Admob_Interstitial_Free_Load_Instances
 * @desc Release Interstitial load instances (passing -1 will free all the loaded instances).
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function Admob_Interstitial_Free_Load_Instances(count) { }

/**
 * @func Admob_Interstitial_Max_Instances
 * @desc Set the max number of Interstitial load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function Admob_Interstitial_Max_Instances(value) { }


/**
 * @func AdMob_Interstitial_Load
 * @desc This function should be called when you want to load an interstitial ad. Calling it will send a request to the ad server to provide an interstitial ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for being shown. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether an interstitial is loaded or not using the function AdMob_Interstitial_IsLoaded.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_Interstitial_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_Interstitial_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Interstitial_Load() { }


/**
 * @func AdMob_Interstitial_Show
 * @desc This function will show the interstitial ad, if one is available and loaded. You can check whether an ad is available using the function AdMob_Interstitial_IsLoaded. Note that while an interstitial is being shown, your app will be put into the background and will effectively be “paused”.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the ad view is closed by the user.
 * @member {string} type `"AdMob_Interstitial_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_Interstitial_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_Interstitial_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * @func_end
 */
function AdMob_Interstitial_Show() { }

/**
 * @func AdMob_Interstitial_IsLoaded
 * @desc This function will return whether or not the interstitial ad is loaded.
 * @returns {bool}
 * @func_end
 */
function AdMob_Interstitial_IsLoaded() { }

/**
 * @func AdMob_Interstitial_Instances_Count
 * @desc Return the number of Interstitial load instances are ready.
 * @returns {bool}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_Interstitial_Instances_Count() { }

///// REWARDED VIDEO
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_RewardedVideo_Init
 * @desc Initializes the target identifier for rewarded video ad functions.
 * > [!NOTE]
 * > Please refer to ${function.AdMob_RewardedVideo_Set_AdUnit} for more information.
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_RewardedVideo_Init(adUnitId) { }

/**
 * @func AdMob_RewardedVideo_Set_AdUnit
 * @desc Set the target identifier for rewarded video functions, Rewarded video funcitons allow multiple identifiers
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_RewardedVideo_Free_Load_Instances
 * @desc Release Rewarded Video load instances (passing -1 will free all the loaded instances).
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Free_Load_Instances(count) { }

/**
 * @func AdMob_RewardedVideo_Max_Instances
 * @desc Set the max number of Rewarded Video load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Max_Instances(count) { }


/**
 * @func AdMob_RewardedVideo_Load
 * @desc This function should be called when you want to load a rewarded video ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether a rewarded video is loaded or not using the function AdMob_RewardedVideo_IsLoaded.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_RewardedVideo_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedVideo_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisment
 * 
 * @event_end
 * @func_end
 */
function AdMob_RewardedVideo_Load() { }


/**
 * @func AdMob_RewardedVideo_Show
 * @desc This function will show the rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function AdMob_RewardedVideo_IsLoaded. Note that while a rewarded video ad is being shown, your app will be put into the background and will effectively be “paused”.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_RewardedVideo_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_RewardedVideo_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedVideo_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the user should be rewarded.
 * @member {string} type `"AdMob_RewardedVideo_OnReward"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedVideo_Show() { }

/**
 * @func AdMob_RewardedVideo_IsLoaded
 * @desc This function will return whether the rewarded video ad has been loaded or not.
 * @returns {bool}
 * @func_end
 */
function AdMob_RewardedVideo_IsLoaded() { }


/**
 * @func AdMob_RewardedVideo_Instances_Count
 * @desc Return the number of Rewarded video load instances are ready.
 * @returns {real}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedVideo_Instances_Count() { }


///// REWARDED INTESTITIAL
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_RewardedInterstitial_Init
 * @desc Initializes the target identifier for rewarded interstitial ad functions.
 * > [!NOTE]
 * > Please refer to ${function.AdMob_RewardedInterstitial_Set_AdUnit} for more information.
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_RewardedInterstitialo_Init(adUnitId) { }

/**
 * @func AdMob_RewardedInterstitial_Set_AdUnit
 * @desc Set the target identifier for rewarded interstitial functions, Rewarded interstitial funcitons allow multiple identifiers
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_RewardedInterstitial_Free_Load_Instances
 * @desc Release Rewarded Interstitial load instances (passing -1 will free all the loaded instances).
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Free_Load_Instances(count) { }


/**
 * @func AdMob_RewardedInterstitial_Max_Instances
 * @desc Set the max number of Rewarded Insterstitials load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Max_Instances(count) { }

/**
 * @func AdMob_RewardedInterstitial_Load
 * @desc This function should be called when you want to load a rewarded interstitial ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether a rewarded interstitial is loaded or not using the function AdMob_RewardedInterstitial_IsLoaded.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_RewardedInterstitial_OnLoadFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedInterstitial_OnLoaded"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedInterstitial_Load() { }


/**
 * @func AdMob_RewardedInterstitial_Show
 * @desc This function will show the rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function AdMob_RewardedInterstitial_IsLoaded. Note that while a rewarded interstitial ad is being shown, your app will be put into the background and will effectively be “paused”.
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_RewardedInterstitial_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_RewardedInterstitial_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_RewardedInterstitial_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered if the user should be rewarded.
 * @member {string} type `"AdMob_RewardedInterstitial_OnReward"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @func_end
 */
function AdMob_RewardedInterstitial_Show() { }


/**
 * @func AdMob_RewardedInterstitial_IsLoaded
 * @desc This function will return whether the rewarded interstitial ad has been loaded or not.
 * @returns {bool}
 * @func_end
 */
function AdMob_RewardedInterstitial_IsLoaded() { }


/**
 * @func AdMob_RewardedInterstitial_Instances_Count
 * @desc Return the number of Rewarded Interstitial load instances are ready.
 * @returns {real}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_RewardedInterstitial_Instances_Count() { }


///// APP OPEN
///// ////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_AppOpenAd_Init
 * @desc Initializes the target identifier for app open ads functions.
 * > [!NOTE]
 * > Please refer to ${function.AdMob_AppOpenAd_Set_AdUnit} for more information.
 * @param {string} adUnitId
 * @version 1.3.0 (-)
 * @func_end
 */
function AdMob_AppOpenAd_Init(adUnitId) { }

/**
 * @func AdMob_AppOpenAd_Set_AdUnit
 * @desc Set the target identifier for app open ads functions, app open ads funcitons doesn't allow multiple pre-loaded identifiers.
 * @param {string} adUnitId
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_AppOpenAd_Set_AdUnit(adUnitId) { }

/**
 * @func AdMob_AppOpenAd_Enable
 * @desc Enable show App Open Ads when game resumes from background.
 * @param {double} orientation
 * @returns {constant.AdMobErrors}
 * 
 * @event social
 * @desc This event is triggered when the ad view is closed by the user.
 * @member {string} type `"AdMob_AppOpenAd_OnDismissed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task fails.
 * @member {string} type `"AdMob_AppOpenAd_OnShowFailed"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @event social
 * @desc This event is triggered is the awaited task succeeds.
 * @member {string} type `"AdMob_AppOpenAd_OnFullyShown"`
 * @member {string} unit_id Unit identifier of the advertisment
 * @event_end
 * 
 * @version 1.3.0 (+)
 * 
 * @func_end
 */
function AdMob_AppOpenAd_Enable(orientation) { }

/**
 * @func AdMob_AppOpenAd_Disable
 * @desc Disable show App Open Ads when game resume
 * @returns {constant.AdMobErrors}
 * @version 1.3.0 (+)
 * @func_end
 */
function AdMob_AppOpenAd_Disable() { }

/**
 * @func AdMob_AppOpenAd_IsEnabled
 * @returns {constant.AdMobErrors}
 * @desc Return the true if app open ads are enabled. Otherwise return false.
 * @returns {bool}
 * 
 * @func_end
 */
function AdMob_AppOpenAd_IsEnabled() { }


///// TARGETING
///// ///////////////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Targeting_COPPA
 * @desc Toggles on/off ads for children. This function should be called BEFORE calling AdMob_Initialize.
 * 
 * > [!WARNING]
 * > Should be called before ${function.AdMob_Initialize}.
 * 
 * @param {bool} COPPA
 * @func_end
 */
function AdMob_Targeting_COPPA(COPPA) { }

/**
 * @func AdMob_Targeting_UnderAge
 * @desc Toggles on/off ads for under aged users. This function should be called BEFORE calling AdMob_Initialize.
 * 
 * > [!WARNING]
 * > Should be called before ${function.AdMob_Initialize}.
 * 
 * @param {bool} underAge
 * @func_end
 */
function AdMob_Targeting_UnderAge(underAge) { }

/**
 * @func AdMob_Targeting_MaxAdContentRating
 * @desc Allows for setting the maximum content rating of the ads to be displayed. This function should be called BEFORE calling AdMob_Initialize.
 * 
 * > [!WARNING]
 * > Should be called before ${function.AdMob_Initialize}.
 * 
 * @param {constant.AdMobContentRating} contentRating
 * @func_end
 */
function AdMob_Targeting_MaxAdContentRating(contentRating) { }

/**
 * @func AdMob_Consent_RequestInfoUpdate
 * @desc Requests a consent information update (this needs to be called prior to ${function.AdMob_Consent_Load})
 * @param {constant.AdMobConsentMode} mode
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnRequestInfoUpdated"`
 * @event_end
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnRequestInfoUpdateFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Consent_RequestInfoUpdate(mode) { }

/**
 * @func AdMob_Consent_GetStatus
 * @desc Allows to set the mode of the consent request being used. This function allows you to debug different regions and EEA and NON-EEA and should be passed in as a 'AdMob_Consent_Mode_*' constant. This function should be called before AdMob_Consent_GetStatus and AdMob_Consent_GetType in order to get the correct output from both functions.
 * 
 * > [!INFO]
 * > Requires a previous call to ${function.AdMob_Consent_RequestInfoUpdate}
 * 
 * @returns {constant.AdMobConsentStatus}
 * @func_end
 */
function AdMob_Consent_GetStatus() { }

/**
 * @func AdMob_Consent_GetType
 * @desc Returns the answer given by the user to a previous GDPR consent request.
 * @returns {constant.AdMobConsentType}
 * @func_end
 */
function AdMob_Consent_GetType() { }


/**
 * @func AdMob_Consent_IsFormAvailable
 * @desc Checks whether or not the GDPR consent form is available on this device.
 * @func_end
 */
function AdMob_Consent_IsFormAvailable() { }

/**
 * @func AdMob_Consent_Load
 * @desc Loads the consent form into memory so it can be displayed to the user. If you do not call this function before trying to show the GDPR consent, nothing will be shown.
 * 
 * > [!INFO]
 * > Requires a previous call to ${function.AdMob_Consent_RequestInfoUpdate}
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnLoaded"`
 * @event_end
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnLoadFailed"`
 * @member {string} errorMessage the error code responsible for the failure
 * @member {real} errorCode the error message of the error code
 * @event_end
 * 
 * @func_end
 */
function AdMob_Consent_Load() { }


/**
 * @func AdMob_Consent_Show
 * @desc Shows the consent form to the user. If you do not call the AdMob_Consent_Load function before trying to show the GDPR consent, nothing will be shown.
 * 
 * > [!INFO]
 * > Requires a previous call to ${function.AdMob_Consent_Load}
 * 
 * @event social
 * @member {string} type `"AdMob_Consent_OnShown"`
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
 * @func AdMob_Settings_SetVolume
 * @desc This method provides control over the sound’s loudness when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * @param {real} value The amount to set the volume to.
 * @func_end
 */
function AdMob_Settings_SetVolume(value) { }

/**
 * @func AdMob_Settings_SetMuted
 * @desc This method provides control over muting the sound when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * @param {real} value
 * @func_end
 */
function AdMob_Settings_SetMuted(value) { }

/**
 * @func AdMob_Events_OnPaidEvent
 * @desc Enable the paid load callbacks, NOTE: You should enable this feature in your console too https://support.google.com/admob/answer/11322405
 * @param {real} enable
 * 
 * @event social
 * @member {string} type `"AdMob_OnPaidEvent"`
 * @member {string} mediation_adapter_class_name The mediation adapter class name of the ad network that loaded the ad.
 * @member {string} unit_id identifier of the ad
 * @member {string} ad_type 'Banner","Interstitial","Rewarded","RewardedInterstitial" or "AppOpen"
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
 * @desc These set of constants represent the values errors values that can return from the AdMob function calls.
 * @member ADMOB_OK There were no errors.
 * @member ADMOB_ERROR_NOT_INITIALIZED The AdMob extension needs to be initialized prior to this call
 * @member ADMOB_ERROR_INVALID_AD_ID The provided ad unit id is not valid.
 * @member ADMOB_ERROR_AD_LIMIT_REACHED The limit of loaded ads for this specific type was reached.
 * @member ADMOB_ERROR_NO_ADS_LOADED There are no loaded ads to be shown for this specific type.
 * @member ADMOB_ERROR_NO_ACTIVE_BANNER_AD There is no active banner ad.
 * @member ADMOB_ERROR_ILLEGAL_CALL The call you are trying to execute is illegal (used for functions that need to be called prior to initialization).
 * @const_end
 */

/**
 * @const AdMobAdValuePrecision
 * @desc These set of constants represent precision type of the reported ad value.
 * @member ADMOB_ADVALUE_PRECISION_UNKNOWN An unknown precision type.
 * @member ADMOB_ADVALUE_PRECISION_ESTIMATED An ad value estimated from aggregated data.
 * @member ADMOB_ADVALUE_PRECISION_PRECISE The precise value paid for this ad.
 * @member ADMOB_ADVALUE_PRECISION_PUBLISHER_PROVIDED A publisher-provided ad value, such as manual CPMs in a mediation group.
 * @const_end
 */

/**
 * @const AdMobBanner
 * @desc These set of constants represent the various types of available banners.
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
 * @const AdMobContentRating
 * @desc These set of constants represent the various types of possible content ratings for ads.
 * @member AdMob_ContentRating_GENERAL Content suitable for general audiences.
 * @member AdMob_ContentRating_PARENTAL_GUIDANCE Content suitable for most audiences with parental guidance.
 * @member AdMob_ContentRating_TEEN Content suitable for teen and older audiences.
 * @member AdMob_ContentRating_MATURE_AUDIENCE Content suitable only for mature audiences.
 * @const_end
 */

/**
 * @const AdMobConsentStatus
 * @desc These set of constants represent the various consent status.
 * @member AdMob_Consent_Status_UNKNOWN Consent status is unknown.
 * @member AdMob_Consent_Status_NOT_REQUIRED User consent not required.
 * @member AdMob_Consent_Status_REQUIRED User consent required but not yet obtained.
 * @member AdMob_Consent_Status_OBTAINED User consent obtained. Personalized vs non-personalized undefined.
 * @const_end
 */

/**
 * @const AdMobConsentType
 * @desc These set of constants represent the given consent type.
 * @member AdMob_Consent_Type_UNKNOWN Consent type is unknown (before consent was requested).
 * @member AdMob_Consent_Type_NON_PERSONALIZED Consent was given for non-personalized ads.
 * @member AdMob_Consent_Type_PERSONALIZED Consent was given for personalized ads.
 * @member AdMob_Consent_Type_DECLINED Consent was declined for any kind of ads.
 * @const_end
 */

/**
 * @const AdMobConsentMode
 * @desc These set of constants represent the consent mode (these are used for testing porpuses).
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_DISABLED Debug geography disabled.
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_EEA Geography appears as in EEA for debug devices.
 * @member AdMob_Consent_Mode_DEBUG_GEOGRAPHY_NOT_EEA Geography appears as not in EEA for debug devices.
 * @member AdMob_Consent_Mode_PRODUCTION Same as `AdMob_Consent_Mode_DEBUG_GEOGRAPHY_DISABLED`, used for production.
 * @const_end
 */


/**
 * @module general
 * @title General
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
 * 
 * @section_func
 * @ref AdMob_RewardedInterstitial_*
 * @section_end
 * 
 * @module_end
 */

/**
 * @module app_open
 * @title App Open
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
 * @desc The following are the available constanst to use with the AdMob API.
 * 
 * @section_const
 * @ref AdMobErrors
 * @ref AdMobBanner
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
 * ## Extension’s Features
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
 * Before you start using this extension make sure to follow our [Setup](Setup) guide. Which will get you up and running.
 *
 * To get started using this extension, follow the [Quick Start Guide](quick_start_guide).
 * 
 * For the recommended workflow see the [Workflow](Workflow) page.
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