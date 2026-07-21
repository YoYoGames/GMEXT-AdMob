/// @description Rewarded interstitial load/show

if (admob_rewarded_interstitial_is_loaded())
{
    admob_rewarded_interstitial_show(
        function(data)
        {
            show_debug_message($"Rewarded interstitial callback: {json_stringify(data)}");

            if (data.event_type == AdMobRewardedInterstitialCallbackEvent.Reward)
            {
                show_message_async("User Earned Reward");
            }

            if (data.event_type == AdMobRewardedInterstitialCallbackEvent.Dismissed
            ||  data.event_type == AdMobRewardedInterstitialCallbackEvent.ShowFailed)
            {
                admob_rewarded_interstitial_load(function(data)
					{
					    show_debug_message($"AdMob Interstitial Load: {json_stringify(data)}");
					});
            }
        }
    );
}
else
{
    show_message_async(
        "RewardedInterstitialAd still loading, try again soon"
    );

    admob_rewarded_interstitial_load(function(data)
		{
		    show_debug_message($"Rewarded Interstitial load: {json_stringify(data)}");
		});
}
