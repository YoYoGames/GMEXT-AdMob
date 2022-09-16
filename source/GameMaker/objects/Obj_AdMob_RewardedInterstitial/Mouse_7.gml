/// @description Rewarded load/show

// Check if current rewarded interstitial ad is loaded
if(AdMob_RewardedInterstitial_IsLoaded())
{
	// Loaded: show rewarded interstitial ad
    AdMob_RewardedInterstitial_Show()
}
else
{
	// Not Loaded: load rewarded interstitial ad
    show_message_async("RewardedInterstitialAd Still loading, try again soon")
	AdMob_RewardedInterstitial_Load()
}
