/// @description Rewarded load/show

if (!is_undefined(rewarded_video_handle))
{
    var _handle = rewarded_video_handle;
    rewarded_video_handle = undefined;

    admob_rewarded_video_show(
        _handle,
        function(_result, _type, _reward)
        {
            show_debug_message($"Rewarded show callback: success={_result.success}, type={_type}, error={_result.error_message}");

            if (!is_undefined(_reward))
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

    admob_rewarded_video_load(function(_result, _handle)
		{
		    show_debug_message($"Rewarded Video Load: success={_result.success}, error={_result.error_message}");

		    if (_result.success)
		        rewarded_video_handle = _handle;
		});
}
