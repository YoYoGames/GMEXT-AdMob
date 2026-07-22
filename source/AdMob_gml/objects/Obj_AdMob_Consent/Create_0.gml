
//For production:
//#macro admob_consent_mode AdMobConsentDebugGeography.Disabled

//For debuging
#macro admob_consent_mode AdMobConsentDebugGeography.EEA
//#macro admob_consent_mode AdMobConsentDebugGeography.NotEEA
//#macro admob_consent_mode AdMobConsentDebugGeography.RegulatedUSState


function consent_finished()
{
	owner.start_ads_init_flow();
	
    switch(admob_consent_get_type())
    {
        case AdMobConsentType.Personalized: show_debug_message("GoogleMobileAds ConsentType: PERSONALIZED"); break;
        case AdMobConsentType.NonPersonalized: show_debug_message("GoogleMobileAds ConsentType: NON_PERSONALIZED"); break;
        case AdMobConsentType.Declined: show_debug_message("GoogleMobileAds ConsentType: DECLINED"); break;
        case AdMobConsentType.Unknown: show_debug_message("GoogleMobileAds ConsentType: UNKNOWN"); break;
    }
	
	//In your game it's good idea destroy, but in this demo we draw some consent info
	//instance_destroy()
}

show_debug_message("admob_consent_request_info_update GM");
admob_consent_request_info_update(admob_consent_mode,function(data)
	{
		show_debug_message($"Consent info callback: {json_stringify(data)}");
		show_debug_message($"consent_get_status: {admob_consent_get_status()}")
		
		if (admob_consent_get_status() == AdMobConsentStatus.Required)
		{
			admob_consent_load(function(data)
				{
					show_debug_message("Consent load callback: " + json_stringify(data));
					
					admob_consent_show(function(data)
						{
							show_debug_message($"Consent show callback: {json_stringify(data)}");
							consent_finished()
						});
				});
		}
		else
		{
			consent_finished()
		}
	})

