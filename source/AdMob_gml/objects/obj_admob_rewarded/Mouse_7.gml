/// @description Rewarded load/show

if (admob_rewarded_video_is_loaded())
{
    admob_rewarded_video_show(
        function(_data_json)
        {
            var _data = json_parse(_data_json);

            show_debug_message(
                "Rewarded show callback: "
                + json_stringify(_data)
            );

            if (_data.event_type == AdMobRewardedVideoCallbackEvent.Reward)
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

    admob_rewarded_video_load(Obj_AdMob.admob_log);
}
