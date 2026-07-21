/// @description Rewarded load/show

if (admob_rewarded_video_is_loaded())
{
    admob_rewarded_video_show(
        function(data)
        {
            show_debug_message($"Rewarded show callback: {json_stringify(data)}");

            if (data.event_type == AdMobRewardedVideoCallbackEvent.Reward)
            {
                show_message_async("User Earned Reward");
            }
        }
    );
}
else
{
    show_message_async(
        "RewardedVideoAd still loading, try again soon"
    );

    admob_rewarded_video_load(function(data)
		{
		    show_debug_message($"Rewarded Video Load: {json_stringify(data)}");
		});
}
