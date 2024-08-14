/// @description Handle Orientation Changes

//Enable 'AdMob_Handle_Orientation_Changes' in the "Create" event
//if your game can change of Orientation 

alarm[0] = room_speed

if(orientation != display_get_orientation())
{
	show_debug_message("[AdMob] Orientation Changed!!!!")
	
	orientation = display_get_orientation()
	
	var Interstitial_Instances_Count = AdMob_Interstitial_Instances_Count()
	Admob_Interstitial_Free_Loaded_Instances(Interstitial_Instances_Count)
	repeat(Interstitial_Instances_Count)
		AdMob_Interstitial_Load()
	
	var RewardedInterstitial_Instances_Count = AdMob_RewardedInterstitial_Instances_Count()
	AdMob_RewardedInterstitial_Free_Loaded_Instances(Interstitial_Instances_Count)
	repeat(Interstitial_Instances_Count)
		AdMob_RewardedInterstitial_Load()
	
	var RewardedVideo_Instances_Count = AdMob_RewardedVideo_Instances_Count()
	AdMob_RewardedVideo_Free_Loaded_Instances(Interstitial_Instances_Count)
	repeat(Interstitial_Instances_Count)
		AdMob_RewardedVideo_Load()
		
	AdMob_AppOpenAd_Disable()
	
	switch(orientation)
	{
		case display_landscape:
		case display_landscape_flipped:
			AdMob_AppOpenAd_Enable(display_landscape)
		break
		
		case display_portrait:
		case display_portrait_flipped:
			AdMob_AppOpenAd_Enable(display_portrait)
		break
	}
}
