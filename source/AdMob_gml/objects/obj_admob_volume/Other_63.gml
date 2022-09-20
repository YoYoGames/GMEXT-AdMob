/// @description Apply volume change

// We check if the async id is from the number prompt
if(request_id == async_load[?"id"])
{	
	// Check the the "status" key
	// 1: user pressed OK
	// 0: user pressed CANCEL
	if(async_load[?"status"] == 1)
	{
		// Cache the new volume variable
		var volume = async_load[?"value"];
		
		// Check if we should mute and apply the mute setting
		var shouldMute = volume == 0;
		AdMob_Settings_SetMuted(shouldMute);
	
		// Apply the new volume
		AdMob_Settings_SetVolume(volume);
		
		// NOTE: these two method calls will trigger the reload of the interstitial
		// and rewarded video ads so that the new changes are applied.
	}
}