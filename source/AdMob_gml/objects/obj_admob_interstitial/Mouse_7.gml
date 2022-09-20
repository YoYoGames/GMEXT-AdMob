/// @description Interstitial load/show

// Check if current interstitial ad is loaded
if(AdMob_Interstitial_IsLoaded())
{
	// Loaded: show interstitial ad
    AdMob_Interstitial_Show();
}	
else
{
	// Not Loaded: load interstitial ad
	AdMob_Interstitial_Load();
    show_message_async("Interstitial Still loading, try again soon");
}

