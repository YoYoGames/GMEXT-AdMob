/// @description Initialize variables

//Enable display orientation correction
#macro AdMob_Handle_Orientation_Changes false


if(AdMob_Handle_Orientation_Changes)
{
	orientation = display_get_orientation()
	alarm[0] = room_speed
}

//AdMob_Events_OnPaidEvent(true)

// Debug variables (used in draw event)
status = "UNKNOWN";
type = "UNKNOWN";

/*
	The first step to configure AdMob with your application/game is to get the
	unique string ad block ids from the admob development console for you app.
	In this example we are using all the bellow ad types:
	
		- Banners
		- Interstitial
		- RewardedVideo
		- RewardedInterstitial
		
	In your case you just need the ones you are using, these ids also change from
	Android to iOS so we also provide a code sample that acounts for that.
	You can set them inside the extension using the new extension options features
	these will be used and set by default or optionaly you can use the old method of
	initialization if you need to change them at runtime (see end of the page).
	
*/

// ###############################################
//                 UTILITY METHODS
// ###############################################

// This function is here for debug purposes and uses 'AdMob_Consent_GetType' and
// 'AdMob_Consent_GetStatus' to print the current consent Status/Type to the console.
function showDebugInfo()
{
	var consent_type = AdMob_Consent_GetType();
	switch(consent_type)//https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentType
	{
		// The user gave permission for data to be collected in order to provide personalized ads.
		case AdMob_Consent_Type_PERSONALIZED:
			show_debug_message("GoogleMobilesAds ConsentType: PERSONALIZED")
		break
			
		// The user refused to share data for personalized ads. Ads will be NON PERSONALIZED
		case AdMob_Consent_Type_NON_PERSONALIZED:
			show_debug_message("GoogleMobilesAds ConsentType: NON_PERSONALIZED")
		break			

		// Unable to get the current type of consent provided by the use
		// Note that for EEA users, the type will always be UNKNOWN (known issue) 
		case AdMob_Consent_Type_UNKNOWN:
			show_debug_message("GoogleMobilesAds ConsentType: UNKNOWN")
		break
	}
}

// This function is an helper function used for loading all ads
function loadAllAds() {
	AdMob_Interstitial_Load();
	AdMob_RewardedVideo_Load();
	AdMob_RewardedInterstitial_Load();
	AdMob_AppOpenAd_Enable(display_landscape)
}

// This function updates both consent Status and Type strings
// To avoid calling the logic every frame
function updateConsentStrings() {

	// The function 'AdMob_Consent_GetStatus' allows the developer to know if the
	// GDPR consent request is required or not or if the user already answered to the
	// consent request (OBTAINED).
	switch(AdMob_Consent_GetStatus())
	{
		case AdMob_Consent_Status_UNKNOWN: status = "UNKNOWN"; break;
		case AdMob_Consent_Status_NOT_REQUIRED: status = "NOT_REQUIRED"; break;
		case AdMob_Consent_Status_REQUIRED: status = "REQUIRED"; break;
		case AdMob_Consent_Status_OBTAINED: status = "OBTAINED"; break;
	}

	// The function 'AdMob_Consent_GetType' allows the developer to know what was the
	// type of consent given by the user. Can the ads be personalized (allowed) or not (rejected).
	switch(AdMob_Consent_GetType())
	{
		case AdMob_Consent_Type_UNKNOWN: type = "UNKNOWN"; break;
		case AdMob_Consent_Type_NON_PERSONALIZED: type = "NON_PERSONALIZED"; break;
		case AdMob_Consent_Type_PERSONALIZED: type = "PERSONALIZED"; break;
		case AdMob_Consent_Type_DECLINED: type = "DECLINED"; break;
	}
}

// ###############################################
//                  CONFIGURATION
// ###############################################

// Sets this device as a test device (should be called before AdMob_Initialize)
// NOTE: This is for development only and should not be used when your game enters production.
// ** On iOS devices to use test device you need to include the App Tracking Transparency extension. **
AdMob_SetTestDeviceId();

// On the new version of this extension you are also able to control the max rating of the
// content displayed on the ads, bellow there is an example with all the possible options available.
//AdMob_Targeting_MaxAdContentRating(AdMob_ContentRating_GENERAL);
//AdMob_Targeting_MaxAdContentRating(AdMob_ContentRating_PARENTAL_GUIDANCE);
//AdMob_Targeting_MaxAdContentRating(AdMob_ContentRating_TEEN);
//AdMob_Targeting_MaxAdContentRating(AdMob_ContentRating_MATURE_AUDIENCE);

// Now you can configure targeting, the functions bellow will allow you to enable and disable
// special ad filtering for children and under age users (respectively)
//AdMob_Targeting_COPPA(true);
//AdMob_Targeting_UnderAge(true);

// ###############################################
//                  INITIALIZATION
// ###############################################

// The first function to be called is Initialize, this is demanding that it is called in first
// place to initialize the AdMob Extension API and allow for everything to work properly.
AdMob_Initialize();



// ###############################################
//                       NEW
// ###############################################

// After API initialization the extension will automatically initialize the available
// ads using the unique ad unit id values provided inside the extension options panel.
// So the code below is not required anymore unless you want to change the ad unit ids at runtime. 
//
// Note that after a call to AbMod_*_Init to change the ad unit id you will need to reload
// the respective ad using AdMob_*_Load (or AdMob_Banner_Create() for ads of banner type).
//
// AdMob_Banner_Set_AdUnit(BANNER_ID);
// AdMob_Interstitial_Set_AdUnit(INTERSTITIAL_ID);
// AdMob_RewardedVideo_Set_AdUnit(REWANTED_ID);
// AdMob_RewardedInterstitial_Set_AdUnit(REWANTED_INTERSTITIAL_ID);
//
