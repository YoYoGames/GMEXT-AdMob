/// @description AdMob event handlers

// We do an early exit if the 'async_load' map doesn't contain a "type" key.
if(!ds_map_exists(async_load, "type")) exit;

show_debug_message("AdMob: " + json_encode(async_load));

// We switch on the type of the event being fired
switch(async_load[?"type"])
{

	// AdMob_Initialize finished
	case ADMOB_EV_INITIALIZED:
		// At this point the AdMob API succeeded to initialize.
		// We will now request a consent information update.	

		// Regarding consent request first of all we should select the mode we want to use for.
		// You can use one of the following constants:
		// 
		// AdMob_Consent_Mode_DEBUG_GEOGRAPHY_EEA (debug only)
		// AdMob_Consent_Mode_DEBUG_GEOGRAPHY_DISABLED (debug only)
		// AdMob_Consent_Mode_DEBUG_GEOGRAPHY_NOT_EEA (debug only)
		// AdMob_Consent_Mode_PRODUCTION (used for release)
		//
		AdMob_Consent_RequestInfoUpdate(AdMob_Consent_Mode_DEBUG_GEOGRAPHY_EEA);
		break;

	// ###############################################
	//                   CONSENT
	// ###############################################

	// AdMob_Consent_RequestInfoUpdate succeeded
	case ADMOB_EV_CONSENT_INFO_UPDATED:
		// At this point the extension succeeded on selecting consent mode.
		
		// We wil update the debug strings (used in draw event)
		updateConsentStrings();
		
		// We use this event to query the current consent status, if GDPR consent
		// is required (AdMob_Consent_Status_REQUIRED) then we start loading the consent form.
		if(AdMob_Consent_GetStatus() == AdMob_Consent_Status_REQUIRED) //https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentStatus.html#REQUIRED
			AdMob_Consent_Load();
		else
		{
			// We are not required to request consent
			// from the user so we can not load the ads
			loadAllAds();
		}
		break
	
	// AdMob_Consent_RequestInfoUpdate failed
	case ADMOB_EV_CONSENT_INFO_UPDATE_FAILED:
	
		// We wil update the debug strings (used in draw event)
		updateConsentStrings();
		
		// At this point the extension failed to obtain a consent update
		// Since we don't know the user consent response we need to assume
		// that the ads MUST be non-personalized
		
		// We can also now load the ads
		// NOTE: Ads should only be loaded after the consent is answered
		loadAllAds();
		break
	
	// AdMob_Consent_Load succeeded
	case ADMOB_EV_CONSENT_LOADED:
		// At this point the consent form loaded successfully.
		// We uses this event to show the consent to the user.
		AdMob_Consent_Show()
		break
	
	// AdMob_Consent_Load failed
	case ADMOB_EV_CONSENT_LOAD_FAILED:
		// At this point there was a problem loading the consent form.
		// Since we don't know the user consent response we need to assume
		// that the ads MUST be non-personalized
		
		// We can also now load the ads
		// NOTE: Ads should only be loaded after the consent is answered
		loadAllAds();
		break
	
	// AdMob_Consent_Show succeeded & user answered
	case ADMOB_EV_CONSENT_SHOWED:
		// At this point the user already saw and answered the
		// consent request so we can process the results.
		showDebugInfo();
		updateConsentStrings();
		
		// We can also now load the ads
		// NOTE: Ads should only be loaded after the consent is answered
		loadAllAds();
		break
	
	case ADMOB_EV_CONSENT_SHOW_FAILED:
		// At this point there was a problem showing the consent form.
		// Since we don't know the user consent response ads will be 
		// non-personalized (this is handled internally by AdMob).
		
		// We can also now load the ads
		// NOTE: Ads should only be loaded after the consent is answered
		loadAllAds();
		break

	// ###############################################
	//                     ADS
	// ###############################################

	// AdMob_Banner_Create succeeded
	case ADMOB_EV_BANNER_LOADED: 
		// At this point the banner ad succeeded to be created.
		break;
	
	// AdMob_Banner_Create failed
	case ADMOB_EV_BANNER_LOAD_FAILED:
		// At this point the banner ad failed to be created.
		break;
	
	// ########### INTERSTITIAL  ###########
	
	// AdMob_Interstitial_Load succeeded
	case ADMOB_EV_INTERSTITIAL_LOADED:
		// At this point the interstitial ad succeeded to load.
		break;
	
	// AdMob_Interstitial_Load failed
	case ADMOB_EV_INTERSTITIAL_LOAD_FAILED:
		// At this point the interstitial ad failed to load.
		//AdMob_Interstitial_load() // This can create an infinite loop if load always fails!!
		break;
	
	// AdMob_Interstitial_Show succeeded
	case ADMOB_EV_INTERSTITIAL_SHOWED:
		// At this point the interstitial ad succeeded to show.
		break;

	// AdMob_Interstitial_Show failed
	case ADMOB_EV_INTERSTITIAL_SHOW_FAILED:
		// At this point the interstitial ad failed to show.
		// Here we use this event to load the interstitial ad again (it could be a load problem).
		AdMob_Interstitial_Load();
		break;
	
	// AdMob_Interstitial got dismissed
	case ADMOB_EV_INTERSTITIAL_DISMISSED:
		// At this point the interstitial just got dismissed.
		// Here we use this event to load the next interstitial ad.
		AdMob_Interstitial_Load();
		break;

	// ########### REWARDED VIDEO ###########

	// AdMob_RewardedVideo_Load succeeded
	case ADMOB_EV_REWARDED_LOADED:
		// At this point the rewarded video succeeded to load.
		break;

	// AdMob_RewardedVideo_Load failed
	case ADMOB_EV_REWARDED_LOAD_FAILED:
		// At this point the rewarded video failed to load.
		//AdMob_RewardedVideo_Load() // This can create an infinite loop if load always fails!!
		break;
	
	// AdMob_RewardedVideo_Show succeeded
	case ADMOB_EV_REWARDED_SHOWED:
		// At this point the rewarded video succeeded to show.
		break;
	
	// AdMob_RewardedVideo_Show failed
	case ADMOB_EV_REWARDED_SHOW_FAILED:
		// At this point the rewarded video failed to show.
		// Here we use this event to load the rewarded video again (it could be a load problem).
		//AdMob_RewardedVideo_Load();
		break;
	
	// AdMob_RewardedVideo got dismissed
	case ADMOB_EV_REWARDED_DISMISSED:
		// At this point the rewarded video just got dismissed.
		// Here we use this event to load the next rewarded video.
		//AdMob_RewardedVideo_Load();
		break;
	
	// AdMob_RewardedVideo triggered reward event
	case ADMOB_EV_REWARDED_REWARD:
		// At this point you can reward the user.
		show_message_async("User Earned Reward");
		break;
	
	
	// ########### REWARDED INTERSTITIAL ###########
	
	// AdMob_RewardedInterstitial_Load succeeded
	case ADMOB_EV_REWARDED_INTERSTITIAL_LOADED: 
		// At this point the rewarded interstitial ad succeeded to load.
		break;
	
	case ADMOB_EV_REWARDED_INTERSTITIAL_LOAD_FAILED:
		// At this point the rewarded interstitial ad failed to load.
		// AdMob_RewardedInterstitial__load() // This can create an infinite loop if load always fails!!
		break;
	
	case ADMOB_EV_REWARDED_INTERSTITIAL_SHOWED:
		// At this point the rewarded interstitial ad succeeded to show.
		break;	
	
	// AdMob_RewardedInterstitial_Show failed
	case ADMOB_EV_REWARDED_INTERSTITIAL_SHOW_FAILED:
		// At this point the rewarded interstitial ad failed to show.
		// Here we use this event to load the rewarded interstitial ad again (it could be a load problem).
		AdMob_RewardedInterstitial_Load();
		break;
	
	// AdMob_RewardedInterstitial got dismissed
	case ADMOB_EV_REWARDED_INTERSTITIAL_DISMISSED:
		// At this point the rewarded interstitial just got dismissed.
		// Here we use this event to load the next rewarded interstitial ad.
		AdMob_RewardedInterstitial_Load();
		break;
	
	// AdMob_RewardedInsterstitial triggered reward event
	case ADMOB_EV_REWARDED_INTERSTITIAL_REWARD:
		// At this point you can reward the user.
		show_message_async("User Earned Reward");
		break;
		
		
	// ########### APP OPEN ###########
	
	// AdMob AppOpenAd got loaded
	case ADMOB_EV_APP_OPEN_LOADED:
		break;

	// AdMob AppOpenAd failed to load
	case ADMOB_EV_APP_OPEN_LOAD_FAILED:
		break;
	
	// AdMob AppOpenAd showed
	case ADMOB_EV_APP_OPEN_SHOWED:
		// At this point the app open ad succeeded to show.
		break;
	
	// AdMob_AppOpenAd_Show failed
	case ADMOB_EV_APP_OPEN_SHOW_FAILED:
		// At this point the app open ad failed to show.
		break;
	
	// AdMob_AppOpenAd got dismissed
	case ADMOB_EV_APP_OPEN_DISMISSED:
		break;
		
		
	case ADMOB_EV_PAID:

	    var _unit_id = async_load[?"unit_id"]
	    var _ad_type = async_load[?"ad_type"]
	    var _micros = async_load[?"micros"]
	    var _currency_code = async_load[?"currency_code"]
	    var _precision = async_load[?"precision"]
	    var _ad_source_name = async_load[?"ad_source_name"]
	    var _ad_source_id = async_load[?"ad_source_id"]
	    var _ad_source_instance_name = async_load[?"ad_source_instance_name"]
	    var _ad_source_instance_id = async_load[?"ad_source_instance_id"]
	    var _mediation_adapter_class_name = async_load[?"mediation_adapter_class_name"]

		//Do something with this data...
		//...
		//...

	    break;
}

