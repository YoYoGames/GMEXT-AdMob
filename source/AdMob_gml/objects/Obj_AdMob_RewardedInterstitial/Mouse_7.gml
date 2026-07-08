/// @description Rewarded interstitial load/show

if (admob_rewarded_interstitial_is_loaded())
{
    admob_rewarded_interstitial_show(
        function(_data_json)
        {
            var _data = json_parse(_data_json);

            show_debug_message(
                "Rewarded interstitial callback: "
                + json_stringify(_data)
            );

            if (_data.event_type == AdMobRewardedInterstitialCallbackEvent.Reward)
            {
                show_message_async("User Earned Reward");
            }

            if (_data.event_type == AdMobRewardedInterstitialCallbackEvent.Dismissed
            ||  _data.event_type == AdMobRewardedInterstitialCallbackEvent.ShowFailed)
            {
                admob_rewarded_interstitial_load(Obj_AdMob.admob_log);
            }
        }
    );
}
else
{
    show_message_async(
        "RewardedInterstitialAd still loading, try again soon"
    );

    admob_rewarded_interstitial_load(Obj_AdMob.admob_log);
}
