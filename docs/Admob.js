


/**
 * @func AdMob_Initialize
 * @desc This function initializes the Google AdMob API and should be called at the start of your game.
 * @func_end
*/
public void AdMob_Initialize() {
	

/**
 * @func AdMob_SetTestDeviceId
 * @desc This function tells the app to use test ads instead of “live” ads, essential for testing whether your ads work without generating potentially fraudulent click-throughs. This function should be called BEFORE calling AdMob_Initialize.
 * @func_end
*/
public void AdMob_SetTestDeviceId() {

/**
 * @func AdMob_Banner_Target
 * @desc Set the target identifier for banner functions, Banner funcitons DOESN'T allow multiple identifiers 
 * @param {string} adUnitId 
 * @func_end
*/
public void AdMob_Banner_Target(String adUnitId) {

/**
 * @func AdMob_Banner_Create
 * @desc 
 * @param {real} size The type of the banner to be displayed.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @func_end
 
* @event social
* @member {string} type  `"AdMob_Banner_OnLoaded"`
* @event_end

* @event social
* @member {string} type  `"AdMob_Banner_OnLoadFailed"`
* @member {string} errorMessage the error code responsible for the failure 
* @member real} errorCode the error message of the error code
* @event_end
*/
public void AdMob_Banner_Create(final double size, final double bottom) {


/**
 * @func AdMob_Banner_GetWidth
 * @desc This function can be used to get the width of the currently loaded banner ad block. The value returned is in pixels. NOTE: This function returns the width in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.
 * @returns {real}
 * @func_end
*/
public double AdMob_Banner_GetWidth() {

/**
 * @func AdMob_Banner_GetHeight
 * @desc This function can be used to get the height of the currently loaded banner ad block. The value returned is in pixels. NOTE: This function returns the height in screen pixels, it’s up to the developer to convert the value to the correct scale according to the render target being used.
 * @returns {real}
 * @func_end
*/
public double AdMob_Banner_GetHeight() {

/**
 * @func AdMob_Banner_Move
 * @desc This function can be used to move a banner that has been previously added. You supply a boolean that will determine if the banner should be placed at the bottom or at the top of the display.
 * @param {bool} bottom Whether the banner should be placed at the bottom of the display.
 * @func_end
*/
public void AdMob_Banner_Move(final double bottom) {

/**
 * @func AdMob_Banner_Show
 * @desc This function can be used to show the currently active, but hidden, banner ad block. When called, the banner will be shown to the user again and will be able to receive input. You can hide the banner again at any time using the AdMob_Banner_Hide function.
 * @func_end
*/
public void AdMob_Banner_Show() {

/**
 * @func AdMob_Banner_Hide
 * @desc This function can be used to hide the currently active banner ad block. When called, the banner will be removed from the user’s view and will no longer receive input. You can show the banner again at any time using the AdMob_Banner_Show function.
 * @func_end
*/
public void AdMob_Banner_Hide() {

/**
 * @func AdMob_Banner_Remove
 * @desc This function will remove the currently active banner from the app. If you call this function then want to show ads again, you must call the AdMob_Banner_Create function first to add a new banner to the display.
 * @func_end
*/
public void AdMob_Banner_Remove() {

/**
 * @func AdMob_Interstitial_Target
 * @desc Set the target identifier for interstitial functions, Interstitials functions allow multiple identifiers 
 * @param {bool} adUnitId
 * @func_end
*/
public void AdMob_Interstitial_Target(String adUnitId) {

/**
 * @func Admob_Interstitial_Free_Load_Instances
 * @desc Release Interstitial load instancez.
 * @param {double} count
 * @func_end
*/
public void Admob_Interstitial_Free_Load_Instances(double count)

/**
 * @func Admob_Interstitial_Max_Instances
 * @desc Set the max number of Interstitial load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @func_end
*/
public void Admob_Interstitial_Max_Instances(double value)


/**
 * @func AdMob_Interstitial_Load
 * @desc This function should be called when you want to load an interstitial ad. Calling it will send a request to the ad server to provide an interstitial ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for being shown. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether an interstitial is loaded or not using the function AdMob_Interstitial_IsLoaded.
 
  * @event social
* @member {string} type  `"AdMob_Interstitial_OnLoaded"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_Interstitial_OnLoadFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
 * @event_end
 
 * @func_end
*/
public void AdMob_Interstitial_Load() {


/**
 * @func AdMob_Interstitial_Show
 * @desc This function will show the interstitial ad, if one is available and loaded. You can check whether an ad is available using the function AdMob_Interstitial_IsLoaded. Note that while an interstitial is being shown, your app will be put into the background and will effectively be “paused”.
 
  * @event social
* @member {string} type  `"AdMob_Interstitial_OnDismissed"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_Interstitial_OnShowFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type  `"AdMob_Interstitial_OnFullyShown"`
* @member {string} id Identifier of the advertisment
 * @event_end
 * @func_end
*/
public void AdMob_Interstitial_Show() {


/**
 * @func AdMob_Interstitial_IsLoaded
 * @desc This function will return whether or not the interstitial ad is loaded.
 * @returns {bool}
 * @func_end
*/
public double AdMob_Interstitial_IsLoaded() {

/**
 * @func AdMob_Interstitial_Instances_Count
 * @desc Return the number of Interstitial load instances are ready.
 * @returns {bool}
 * @func_end
*/
public double AdMob_Interstitial_Instances_Count() {

/**
 * @func AdMob_RewardedVideo_Target
 * @desc Set the target identifier for rewarded video functions, Rewarded video funcitons allow multiple identifiers 
 * @func_end
*/
public void AdMob_RewardedVideo_Target(String adUnitId) {

/**
 * @func AdMob_RewardedVideo_Free_Load_Instances
 * @desc Release Rewarded Video load instancez.
 * @param {double} count
 * @func_end
*/
public void AdMob_RewardedVideo_Free_Load_Instances(double count)

/**
 * @func AdMob_RewardedVideo_Max_Instances
 * @desc Set the max number of Rewarded Video load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @func_end
*/
public void AdMob_RewardedVideo_Max_Instances(double count)


/**
 * @func AdMob_RewardedVideo_Load
 * @desc This function should be called when you want to load a rewarded video ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether a rewarded video is loaded or not using the function AdMob_RewardedVideo_IsLoaded.
 
  * @event social
* @member {string} type  `"AdMob_RewardedVideo_OnLoadFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedVideo_OnLoaded"`
* @member {string} id Identifier of the advertisment

 * @event_end
 * @func_end
*/
public void AdMob_RewardedVideo_Load() {


/**
 * @func AdMob_RewardedVideo_Show
 * @desc This function will show the rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function AdMob_RewardedVideo_IsLoaded. Note that while a rewarded video ad is being shown, your app will be put into the background and will effectively be “paused”.
 
  * @event social
* @member {string} type  `"AdMob_RewardedVideo_OnDismissed"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedVideo_OnShowFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedVideo_OnFullyShown"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedVideo_OnReward"`
* @member {string} id Identifier of the advertisment
 * @event_end
 
 * @func_end
*/
public void AdMob_RewardedVideo_Show() {

/**
 * @func AdMob_RewardedVideo_IsLoaded
 * @desc This function will return whether the rewarded video ad has been loaded or not.
 * @returns {bool}
 * @func_end
*/
public double AdMob_RewardedVideo_IsLoaded() {


/**
 * @func AdMob_RewardedVideo_Instances_Count
 * @desc Return the number of Rewarded video load instances are ready.
 * @returns {real}
 * @func_end
*/
public double AdMob_RewardedVideo_Instances_Count() {


///// REWARDED INTESTITIAL
///// ////////////////////////////////////////////////////////////////////////


/**
 * @func AdMob_RewardedInterstitial_Target
 * @desc Set the target identifier for rewarded interstitial functions, Rewarded interstitial funcitons allow multiple identifiers 
 * @func_end
*/
public void AdMob_RewardedInterstitial_Target(String adUnitId) {

/**
 * @func AdMob_RewardedInterstitial_Free_Load_Instances
 * @desc Release Rewarded Interstitial load instancez.
 * @param {double} count
 * @func_end
*/
public void AdMob_RewardedInterstitial_Free_Load_Instances(double count)


/**
 * @func AdMob_RewardedInterstitial_Max_Instances
 * @desc Set the max number of Rewarded Insterstitials load instances, this allow present consecutiva ads. Default value is 1.
 * @param {double} count
 * @func_end
*/
public void AdMob_RewardedInterstitial_Max_Instances(double count)

/**
 * @func AdMob_RewardedInterstitial_Load
 * @desc This function should be called when you want to load a rewarded interstitial ad. Calling it will send a request to the ad server to provide a rewarded ad, which will then be loaded into the app for display. This function does not show the ad, just stores it in memory ready for showing. If you do not call this function before trying to show an ad, nothing will be shown. Note that you can check whether a rewarded interstitial is loaded or not using the function AdMob_RewardedInterstitial_IsLoaded.
 
  * @event social
* @member {string} type  `"AdMob_RewardedInterstitial_OnLoadFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type `"AdMob_RewardedInterstitial_OnLoaded"`
* @member {string} id Identifier of the advertisment
 * @event_end
 
 * @func_end
*/
public void AdMob_RewardedInterstitial_Load() 


/**
 * @func AdMob_RewardedInterstitial_Show
 * @desc This function will show the rewarded video ad, if one is available and loaded. You can check whether an ad has previously been loaded using the function AdMob_RewardedInterstitial_IsLoaded. Note that while a rewarded interstitial ad is being shown, your app will be put into the background and will effectively be “paused”.
  * @event social
* @member {string} type  `"AdMob_RewardedInterstitial_OnDismissed"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedInterstitial_OnShowFailed"`
* @member {string} id Identifier of the advertisment
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedInterstitial_OnFullyShown"`
* @member {string} id Identifier of the advertisment
* @event_end

* @event social
* @member {string} type  `"AdMob_RewardedInterstitial_OnReward"`
* @member {string} id Identifier of the advertisment
 * @event_end
 
 * @func_end
*/
public void AdMob_RewardedInterstitial_Show() {
					

/**
 * @func AdMob_RewardedInterstitial_IsLoaded
 * @desc This function will return whether the rewarded interstitial ad has been loaded or not.
 * @returns {bool}
 * @func_end
*/
public double AdMob_RewardedInterstitial_IsLoaded() {


/**
 * @func AdMob_RewardedInterstitial_Instances_Count
 * @desc Return the number of Rewarded Interstitial load instances are ready.
 * @returns {real}
 * @func_end
*/
public double AdMob_RewardedInterstitial_Instances_Count() {


/**
 * @func AdMob_AppOpenAd_Target
 * @desc Set the target identifier for app open ads functions, app open ads funcitons DOESN'T allow multiple identifiers 
 * @param {String} count
 * @func_end
*/
public void AdMob_AppOpenAd_Target(String adUnitId)

/**
 * @func AdMob_AppOpenAd_Enable
 * @desc Enable show App Open Ads when game resume
 * @param {double} orientation
 * @func_end
*/
public void AdMob_AppOpenAd_Enable(double orientation)

/**
 * @func AdMob_AppOpenAd_Enable
 * @desc Disable show App Open Ads when game resume
 * @func_end
*/
public void AdMob_AppOpenAd_Disable()

/**
 * @func AdMob_AppOpenAd_Enable
 * @desc Return the true if app open ads are enabled. Otherwise return false
 * @returns {bool}

  * @event social
* @member {string} type  `"AdMob_AppOpenAd_OnDismissed"`
* @event_end

* @event social
* @member {string} type  `"AdMob_AppOpenAd_OnShowFailed"`
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
* @event_end

* @event social
* @member {string} type  `"AdMob_AppOpenAd_OnFullyShown"`

 * @event_end
 
 * @func_end
*/
public double AdMob_AppOpenAd_IsEnabled()					


///// TARGETING
///// ///////////////////////////////////////////////////////////////////////////////////

/**
 * @func AdMob_Targeting_COPPA
 * @desc Toggles on/off ads for children. This function should be called BEFORE calling AdMob_Initialize.
 * @param {real} COPPA
 * @func_end
*/
public void AdMob_Targeting_COPPA(double COPPA) {

/**
 * @func AdMob_Targeting_UnderAge
 * @desc Toggles on/off ads for under aged users. This function should be called BEFORE calling AdMob_Initialize.
 * @param {real} underAge
 * @func_end
*/
public void AdMob_Targeting_UnderAge(double underAge) {

/**
 * @func AdMob_Targeting_MaxAdContentRating
 * @desc Allows for setting the maximum content rating of the ads to be displayed. This function should be called BEFORE calling AdMob_Initialize.
 * @param {real} contentRating
 * @func_end
*/
public void AdMob_Targeting_MaxAdContentRating(double contentRating) {

/**
 * @func AdMob_NonPersonalizedAds_Set
 * @desc 
 * @param {real} value
 * @func_end
*/
public void AdMob_NonPersonalizedAds_Set(double value) {

/**
 * @func AdMob_Consent_RequestInfoUpdate
 * @desc 
 * @param {real} mode
 
  * @event social
* @member {string} type  `"AdMob_Consent_OnRequestInfoUpdated"`
* @event_end

* @event social
* @member {string} type  `"AdMob_Consent_OnRequestInfoUpdateFailed"`
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
 * @event_end
 
 * @func_end
*/
public void AdMob_Consent_RequestInfoUpdate(double mode) {


/**
 * @func AdMob_Consent_GetStatus
 * @desc Allows to set the mode of the consent request being used. This function allows you to debug different regions and EEA and NON-EEA and should be passed in as a 'AdMob_Consent_Mode_*' constant. This function should be called before AdMob_Consent_GetStatus and AdMob_Consent_GetType in order to get the correct output from both functions.
 * @func_end
*/
public double AdMob_Consent_GetStatus() {

/**
 * @func AdMob_Consent_GetType
 * @desc Returns the answer given by the user to a previous GDPR consent request.
 * @func_end
*/
public double AdMob_Consent_GetType() {


/**
 * @func AdMob_Consent_IsFormAvailable
 * @desc Checks whether or not the GDPR consent form is available on this device.
 * @func_end
*/
public double AdMob_Consent_IsFormAvailable() {


/**
 * @func AdMob_Consent_Load
 * @desc Loads the consent form into memory so it can be displayed to the user. If you do not call this function before trying to show the GDPR consent, nothing will be shown.
 
  * @event social
* @member {string} type  `"AdMob_Consent_OnLoaded"`
* @event_end

* @event social
* @member {string} type  `"AdMob_Consent_OnLoadFailed"`
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
 * @event_end
 
 * @func_end
*/
public void AdMob_Consent_Load() 



/**
 * @func AdMob_Consent_Show
 * @desc Shows the consent form to the user. If you do not call the AdMob_Consent_Load function before trying to show the GDPR consent, nothing will be shown.
 
  * @event social
* @member {string} type  `"AdMob_Consent_OnShown"`
* @event_end

* @event social
* @member {string} type  `"AdMob_Consent_OnShowFailed"`
* @member {string} errorMessage the error code responsible for the failure
* @member real} errorCode the error message of the error code
 * @event_end
 
 * @func_end
*/
public void AdMob_Consent_Show() {


/**
 * @func AdMob_Consent_Reset
 * @desc This function resets the consent status flag.
 * @func_end
*/
public void AdMob_Consent_Reset() {


/**
 * @func AdMob_Settings_SetVolume
 * @desc This method provides control over the sound’s loudness when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * @param {real} value
 * @func_end
*/
public void AdMob_Settings_SetVolume(double value) 

/**
 * @func AdMob_Settings_SetMuted
 * @desc This method provides control over muting the sound when playing rewarded video ads. This method will trigger a reload of the current Interstitial and RewardedVideo ads.
 * @param {real} value
 * @func_end
*/
public void AdMob_Settings_SetMuted(double value) 

/**
 * @func AdMob_Enable_Paid_Event
 * @desc Enable the paid load callbacks, NOTE: You should enable this feature in your console too https://support.google.com/admob/answer/11322405
 * @param {real} value
 
* @event social
* @member {string} type  `"AdMob_onPaidEvent"`
* @member {string} mediationAdapterClassName Returns the mediation adapter class name of the ad network that loaded the ad.
* @member {string} adUnitId identifier of the ad
* @member {string} adType 'Banner","Interstitial","Rewarded","RewardedInterstitial" or "AppOpen"
* @member real} micros The ad's value in micro-units, where 1,000,000 micro-units equal one unit of the currency.
* @member {string} currencyCode The value's ISO 4217 currency code.
* @member real} precision The precision type of the reported ad value.
* @member {string} adSourceName Gets the ad source representing the specific ad network that serves the impression. For campaigns, Mediated House Ads is returned for a mediated ads campaign goal type, and Reservation Campaign is returned for impression and click goal types. See Ad sources for the list of possible ad source names when an ad network serves the ad.
* @member {string} adSourceId Gets the ad source ID associated with this adapter response. For campaigns, 6060308706800320801 is returned for a mediated ads campaign goal type, and 7068401028668408324 is returned for impression and click goal types. See Ad sources for the list of possible ad source IDs when an ad network serves the ad.
* @member {string} adSourceInstanceName Gets the ad source instance name associated with this adapter response.
* @member {string} adSourceInstanceId Gets the ad source instance ID associated with this adapter response.
* @event_end
 
 * @func_end
*/
public void AdMob_Enable_Paid_Event()
