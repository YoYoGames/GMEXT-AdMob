/// @description Rewarded load/show

// Check if current rewarded video ad is loaded
if(AdMob_RewardedVideo_IsLoaded())
{
	// Loaded: show rewarded video ad
    AdMob_RewardedVideo_Show()
}
else
{
	// Not Loaded: load rewarded video ad
    show_message_async("RewardedVideoAd Still loading, try again soon")
	AdMob_RewardedVideo_Load()
}
