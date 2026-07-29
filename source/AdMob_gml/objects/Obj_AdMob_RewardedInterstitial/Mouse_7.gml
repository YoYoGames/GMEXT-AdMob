/// @description Rewarded interstitial load/show

if (!is_undefined(rewarded_interstitial_handle))
{
    var _handle = rewarded_interstitial_handle;
    rewarded_interstitial_handle = undefined;

    admob_rewarded_interstitial_show(
        _handle,
        function(_result, _type, _reward)
        {
            show_debug_message($"Rewarded interstitial callback: success={_result.success}, type={_type}, error={_result.error_message}");

            if (!is_undefined(_reward))
            {
                show_message_async("User Earned Reward");
            }

            if (!_result.success || _type == AdMobRewardedInterstitialShowEvent.Dismissed)
            {
                admob_rewarded_interstitial_load(function(_result, _handle)
					{
					    show_debug_message($"AdMob Interstitial Load: success={_result.success}, error={_result.error_message}");

					    if (_result.success)
					        rewarded_interstitial_handle = _handle;
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

    admob_rewarded_interstitial_load(function(_result, _handle)
		{
		    show_debug_message($"Rewarded Interstitial load: success={_result.success}, error={_result.error_message}");

		    if (_result.success)
		        rewarded_interstitial_handle = _handle;
		});
}
